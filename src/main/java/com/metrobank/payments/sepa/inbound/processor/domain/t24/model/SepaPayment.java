/*
 * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.payments.sepa.inbound.processor.domain.t24.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Data;

/**
 * Description: The model with Sepa payments input details.
 *
 * @author dev.AHemke
 */
@ApiModel(description = "SepaPayment")
@Data
@Builder
public class SepaPayment {
  @JsonProperty("body")
  private SepaPaymentBody body;
}
