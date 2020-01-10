package org.example;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map.Entry;

public class SQSConsumer implements Runnable {
  private final AmazonSQS sqs;
  private final String queueUrl;
  private final int pollingDelay;
  boolean run = true;

  public SQSConsumer(AmazonSQS sqs, String queueUrl, int pollingDelay) {
    this.sqs = sqs;
    this.queueUrl = queueUrl;
    this.pollingDelay = pollingDelay;
  }

  @Override
  public void run() {
    ObjectMapper objectMapper = new ObjectMapper();
    System.out.println(
        String.format("Receiving messages from %s, delay %d seconds", queueUrl, pollingDelay));

    while (run) {
      // Receive messages.
      final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
      receiveMessageRequest.setMaxNumberOfMessages(1);
      receiveMessageRequest.setWaitTimeSeconds(pollingDelay); // Use long polling if > 0
      final List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();

      if (messages.size() == 0) {
        System.out.println(String.format("[%s] Got an empty response", Instant.now()));
      }

      Instant receivedTime = Instant.now();

      for (final Message message : messages) {
        try {
          SampleMessage jsonBody = objectMapper.readValue(message.getBody(), SampleMessage.class);
          Instant sentTime = Instant.parse(jsonBody.publishTime());

          Duration deliveryDelay = Duration.between(sentTime, receivedTime);

          System.out.println(String.format("[%s] Message", receivedTime));
          System.out.println("  MessageId:       " + message.getMessageId());
          System.out.println("  ReceiptHandle:   " + message.getReceiptHandle());
          System.out.println("  MD5OfBody:       " + message.getMD5OfBody());
          System.out.println("  Body:            " + message.getBody());
          System.out.println("  DeliverDelay:    " + deliveryDelay.toMillis());
          System.out.println("  Scheduled Delay: " + jsonBody.delay() * 1000);

          for (final Entry<String, String> entry : message.getAttributes().entrySet()) {
            System.out.println("Attribute");
            System.out.println("  Name:  " + entry.getKey());
            System.out.println("  Value: " + entry.getValue());
          }

          recordSub(message.getMessageId(), deliveryDelay.toMillis(), jsonBody.delay() * 1000);

          // Delete the message.
          System.out.println(String.format("Deleting message %s.\n", message.getMessageId()));
          final String messageReceiptHandle = message.getReceiptHandle();
          sqs.deleteMessage(new DeleteMessageRequest(queueUrl, messageReceiptHandle));
        } catch (IOException ex) {
          System.err.println(ex);
        }
      }
    }
  }

  private void recordSub(String messageId, long deliveryDelay, int scheduledDelay) {
    final String fileName = "./sub-" + ProcessHandle.current().pid();
    try (BufferedWriter w =
        Files.newBufferedWriter(
            Paths.get(fileName),
            Charset.defaultCharset(),
            StandardOpenOption.APPEND,
            StandardOpenOption.CREATE)) {
      w.write(String.format("%s,%d,%d\n", messageId, deliveryDelay, scheduledDelay));
    } catch (IOException ioex) {
      System.err.println(ioex);
    }
  }
}
