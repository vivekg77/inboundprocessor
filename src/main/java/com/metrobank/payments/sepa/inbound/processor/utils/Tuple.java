/*
 * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.payments.sepa.inbound.processor.utils;

import lombok.Builder;
import lombok.Data;

/**
 * Description: A class to hold multiple data elements.
 *
 * @param <U> message/ anontype message.
 * @param <V> T24 response object.
 */
@Builder
@Data
public class Tuple<U, V> {
  U message;
  V response;
}
