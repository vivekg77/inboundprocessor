/*
 * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.payments.sepa.inbound.processor.domain.t24.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * Description: Payments sepa status response model.
 *
 * @author dev.AHemke
 */
@Data
@Builder
public class SepaStatusResponseBody {
  @JsonProperty("status")
  private String status;

  @JsonProperty("date")
  private LocalDate date;
}
