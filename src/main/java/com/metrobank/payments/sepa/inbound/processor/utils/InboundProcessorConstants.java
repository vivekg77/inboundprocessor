/*
 * Copyright 2021 Metro Bank. All rights reserved.
 */

package com.metrobank.payments.sepa.inbound.processor.utils;

public class InboundProcessorConstants {

  public static final String CONTENT_TYPE = "Content-Type";
  public static final String ACCEPT = "Accept";
  public static final String FLOW_DIRECTION = "return";

  public static final String SEPA_INBOUND_PAYMENT_NOT_FOUND = "SEPA-INBOUND-PAYMENT-NOT-FOUND";
  public static final String SEPA_INBOUND_PAYMENT_DUPLICATE = "SEPA-INBOUND-PAYMENT-DUPLICATE";
  public static final String SEPA_INBOUND_PAYMENT_ERROR = "SEPA_INBOUND_PAYMENT_ERROR";
  public static final String SEPA_INBOUND_PAYMENT_PROCESSED = "SEPA-INBOUND-PAYMENT-PROCESSED";
  public static final String STORE_AND_FORWARD_CONFIG_FILE = "/store-forward-config.yaml";

  public static final String T24_SEPA_PAYMENT_EXCEPTION_REASON = "T24_SEPA_PAYMENT_FAILURE_REASON";
  public static final String T24_SEPA_PAYMENT_EXCEPTION_NAME = "T24_SEPA_PAYMENT_EXCEPTION";

  public static final String NUM_RETRIES = "5";
  public static final String EMPTY_STRING = "";
  public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SS";

  private InboundProcessorConstants() {}
}
