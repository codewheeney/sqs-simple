package org.example;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;

@AutoValue
@JsonSerialize(as = SampleMessage.class)
@JsonDeserialize(builder = SampleMessage.Builder.class)
public abstract class SampleMessage {
  public static Builder builder() {
    return Builder.builder();
  }

  @JsonProperty("publishTime")
  public abstract String publishTime();

  @JsonProperty("delay")
  public abstract int delay();

  @AutoValue.Builder
  public abstract static class Builder {

    @JsonCreator
    public static Builder builder() {
      return new AutoValue_SampleMessage.Builder();
    }

    public abstract SampleMessage build();

    @JsonProperty("publishTime")
    public abstract Builder publishTime(String publishTime);

    @JsonProperty("delay")
    public abstract Builder delay(int delay);
  }
}
