/*
 * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.payments.sepa.inbound.processor.domain.t24.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * Description: Response class which contains output details.
 *
 * @author dev.AHemke
 */
@Data
@Builder
public class SepaStatusBody {
  @JsonProperty("status")
  private StatusEnum status;

  @JsonProperty("date")
  private LocalDate date;

  /** The status identifier */
  public enum StatusEnum {
    SEPA("SEPA"),

    AML("AML"),

    REV("REV");

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    @JsonCreator
    public static StatusEnum fromValue(String text) {
      for (StatusEnum b : StatusEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }
  }
}
