package org.example;

/*
 * Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  https://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.apache.commons.lang3.StringUtils;

/**
 * This sample demonstrates how to make basic requests to Amazon SQS using the AWS SDK for Java.
 *
 * <p>Prerequisites: You must have a valid Amazon Web Services developer account, and be signed up
 * to use Amazon SQS. For more information about Amazon SQS, see https://aws.amazon.com/sqs
 *
 * <p>Make sure that your credentials are located in ~/.aws/credentials
 */
public class SQSSimpleJavaClientExample {
  public static void main(String[] args) {
    if (args.length != 3) {
      System.err.println("Usage SQSSimpleJavaClientExample [pub/sub] QueueUrl polldelay");
      return;
    }

    final boolean publisher = StringUtils.compareIgnoreCase(args[0], "pub") == 0;

    // "https://sqs.us-east-2.amazonaws.com/490273447617/SQS-tutorial";
    final String queueUrl = args[1];

    final int pollingDelay = Integer.parseInt(args[2]);

    System.out.println("==============================================");
    System.out.println(
        String.format("%s to queue %s", publisher ? "Publishing" : "Subscribing", queueUrl));
    System.out.println("==============================================");

    /*
     * Create a new instance of the builder with all defaults (credentials
     * and region) set automatically. For more information, see
     * Creating Service Clients in the AWS SDK for Java Developer Guide.
     */
    final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();

    try {
      Runnable job;

      if (publisher) {
        job = new SQSPublisher(sqs, queueUrl);
      } else {
        job = new SQSConsumer(sqs, queueUrl, pollingDelay);
      }

      Thread t = new Thread(job);
      t.run();
      t.join();
    } catch (InterruptedException iex) {
      System.out.println("Thread interrupted!");
      System.out.println(iex);
    } catch (final AmazonServiceException ase) {
      System.out.println(
          "Caught an AmazonServiceException, which means "
              + "your request made it to Amazon SQS, but was "
              + "rejected with an error response for some reason.");
      System.out.println("Error Message:    " + ase.getMessage());
      System.out.println("HTTP Status Code: " + ase.getStatusCode());
      System.out.println("AWS Error Code:   " + ase.getErrorCode());
      System.out.println("Error Type:       " + ase.getErrorType());
      System.out.println("Request ID:       " + ase.getRequestId());
    } catch (final AmazonClientException ace) {
      System.out.println(
          "Caught an AmazonClientException, which means "
              + "the client encountered a serious internal problem while "
              + "trying to communicate with Amazon SQS, such as not "
              + "being able to access the network.");
      System.out.println("Error Message: " + ace.getMessage());
    }
  }
}
