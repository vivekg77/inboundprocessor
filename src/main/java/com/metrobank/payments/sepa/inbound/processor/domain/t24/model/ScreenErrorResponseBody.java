/*
 * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.payments.sepa.inbound.processor.domain.t24.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Description: The model to handle the error response.
 *
 * @author dev.AHemke
 */
@Data
@Builder
public class ScreenErrorResponseBody {
  @JsonProperty("errorDetails")
  private List<Object> errorDetails;

  @JsonProperty("type")
  private String type;
}
