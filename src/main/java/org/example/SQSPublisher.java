package org.example;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Random;

public class SQSPublisher implements Runnable {
  private final AmazonSQS sqs;
  private final String queueUrl;
  boolean run = true;

  public SQSPublisher(AmazonSQS sqs, String queueUrl) {
    this.sqs = sqs;
    this.queueUrl = queueUrl;
  }

  @Override
  public void run() {
    Random random = new Random();
    ObjectMapper objectMapper = new ObjectMapper();

    while (run) {
      try {
        final int delay = random.nextInt(11);
        final Instant now = Instant.now();

        // Send a message.
        System.out.println(
            String.format("Sending a message to the queue with a delay of %d seconds.", delay));

        SampleMessage sampleMessage =
            SampleMessage.builder().delay(delay).publishTime(now.toString()).build();

        SendMessageRequest request =
            new SendMessageRequest(queueUrl, objectMapper.writeValueAsString(sampleMessage));
        request.setDelaySeconds(delay);
        SendMessageResult sendMessageResult = sqs.sendMessage(request);
        System.out.println(String.format("Sent message %s", sendMessageResult.getMessageId()));

        recordPub(sendMessageResult.getMessageId());

        try {
          Thread.sleep(200);
        } catch (InterruptedException ex) {

        }
      } catch (JsonProcessingException jpex) {
        System.err.println(jpex);
      }
    }
  }

  private void recordPub(String messageId) {
    final String fileName = "./pub-" + ProcessHandle.current().pid();
    try (BufferedWriter w =
        Files.newBufferedWriter(
            Paths.get(fileName),
            Charset.defaultCharset(),
            StandardOpenOption.APPEND,
            StandardOpenOption.CREATE)) {
      w.write(String.format("%s\n", messageId));
    } catch (IOException ioex) {
      System.err.println(ioex);
    }
  }
}
