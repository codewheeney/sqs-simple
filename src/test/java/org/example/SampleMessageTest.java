package org.example;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Instant;
import org.testng.annotations.Test;

@Test
public class SampleMessageTest {
  public void testSerialization() throws IOException {
    final ObjectMapper objectMapper = new ObjectMapper();
    final int delay = 10;
    final Instant now = Instant.now();

    SampleMessage sampleMessage =
        SampleMessage.builder().delay(delay).publishTime(now.toString()).build();

    String serialized = objectMapper.writeValueAsString(sampleMessage);

    assertFalse(serialized.isEmpty());

    SampleMessage deserializedMessage = objectMapper.readValue(serialized, SampleMessage.class);

    assertEquals(deserializedMessage.delay(), delay);
    assertEquals(Instant.parse(deserializedMessage.publishTime()), now);
  }
}
