/*
 * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.payments.sepa.inbound.processor.domain.t24.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description: The model to handle details of the screen header.
 *
 * @author dev.AHemke
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScreenHeader {
  @JsonProperty("id")
  private String id;

  @JsonProperty("status")
  private String status;

  @JsonProperty("transactionStatus")
  private String transactionStatus;

  @JsonProperty("audit")
  private Object audit;
}
