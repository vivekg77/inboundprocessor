/*
 * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.payments.sepa.inbound.processor.domain.t24.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * Description: The model to handle the header of the error response.
 *
 * @author ahemke
 */
@Data
@Builder
public class ErrorHeader {
  @JsonProperty("id")
  private String id;

  @JsonProperty("status")
  private String status;

  @JsonProperty("transactionStatus")
  private String transactionStatus;

  @JsonProperty("audit")
  private Object audit;
}
