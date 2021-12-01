/*
 * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.payments.sepa.inbound.processor.domain.t24.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Data;

/**
 * Description: The model to handle the error response.
 *
 * @author dev.AHemke
 */
@Data
@Builder
@ApiModel(description = "ScreenErrorResponse")
public class ScreenErrorResponse {
  @JsonProperty("header")
  private ErrorHeader header;

  @JsonProperty("error")
  private ScreenErrorResponseBody error;

  @JsonProperty("override")
  private OverrideBody override;
}
