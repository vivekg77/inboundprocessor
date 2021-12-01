/*
 *
 * * Copyright 2021 Metro Bank. All rights reserved.
 *
 */
package com.metrobank.payments.sepa.inbound.processor.utils;

import com.metrobank.header.util.HeaderEnums;
import com.metrobank.header.util.HeaderProvider;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.streams.processor.ProcessorContext;

/** Description: General utility methods of the micro-service. */
public class GeneralUtils {
  private GeneralUtils() {
    /*To avoid instantiation of the class*/
  }

  /**
   * Description: Fetches header details for every case.
   *
   * @param processorContext The processor context
   * @param status The status of the monitoring header or payment status
   * @return The kafka headers headers.
   */
  public static Headers fetchHeaders(
      ProcessorContext processorContext, String status, HeaderEnums.MessageType messageType) {
    HeaderProvider headerProvider;
    headerProvider =
        HeaderProvider.getInstanceForClearingCustomHeaders(processorContext.headers(), status);
    headerProvider.messageType(messageType);
    headerProvider.flowDirection(HeaderEnums.FlowDirection.INBOUND);
    headerProvider.paymentStatus(status);
    return headerProvider.provide();
  }
}
