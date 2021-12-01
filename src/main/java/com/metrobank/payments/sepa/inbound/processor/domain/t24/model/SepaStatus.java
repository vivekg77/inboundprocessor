/*
 * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.payments.sepa.inbound.processor.domain.t24.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Data;

/**
 * Description: Sepa status with the the status body.
 *
 * @author dev.AHemke
 */
@ApiModel(description = "SepaStatus")
@Data
@Builder
public class SepaStatus {
  @JsonProperty("body")
  private SepaStatusBody body;
}
