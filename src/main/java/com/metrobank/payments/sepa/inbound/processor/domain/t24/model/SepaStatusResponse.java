/*
 * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.payments.sepa.inbound.processor.domain.t24.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Data;

/**
 * Description : Common Sepa status response class.
 *
 * @author dev.AHemke
 */
@ApiModel(description = "SepaStatusResponse")
@Data
@Builder
public class SepaStatusResponse {
  @JsonProperty("header")
  private ScreenHeader header;

  @JsonProperty("body")
  private SepaStatusResponseBody body;
}
