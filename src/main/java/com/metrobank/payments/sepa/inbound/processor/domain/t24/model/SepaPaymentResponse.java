/*
 * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.payments.sepa.inbound.processor.domain.t24.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description: Response class to be returned by Api
 *
 * @author dev.AHemke
 */
@ApiModel(description = "SepaPaymentResponse")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SepaPaymentResponse {
  @JsonProperty("header")
  private ScreenHeader header;

  @JsonProperty("body")
  private SepaPaymentResponseBody body;
}
