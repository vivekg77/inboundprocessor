/*
 * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.payments.sepa.inbound.processor.domain.t24.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * Description: Request class with input message.
 *
 * @author dev.AHemke
 */
@Data
@Builder
public class SepaPaymentBody {
  @JsonProperty("inMsg")
  private String inMsg;
}
