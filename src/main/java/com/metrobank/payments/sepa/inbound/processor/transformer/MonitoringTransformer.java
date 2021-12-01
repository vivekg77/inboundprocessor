/*
 * * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.payments.sepa.inbound.processor.transformer;

import com.metrobank.header.util.HeaderEnums;
import com.metrobank.payments.common.NullPayload;
import com.metrobank.payments.sepa.inbound.processor.utils.GeneralUtils;
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;

/**
 * * Description: The transformer for monitoring topic records handling.
 *
 * @param <V> The input value to the stream.
 * @param <O> The output of the stream.
 */
public class MonitoringTransformer<V, O> implements ValueTransformer<V, O> {
  private ProcessorContext context;
  private String sepaMonitoringStatus;

  public MonitoringTransformer(String sepaMonitoringStatus) {
    this.sepaMonitoringStatus = sepaMonitoringStatus;
  }

  @Override
  public void init(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public O transform(V value) {
    addAdditionalHeaders(context);
    return (O) NullPayload.newBuilder().build();
  }

  /**
   * Description: Adds additional headers.
   *
   * @param context Kstream Context with headers
   */
  private void addAdditionalHeaders(ProcessorContext context) {
    GeneralUtils.fetchHeaders(
        context, sepaMonitoringStatus, HeaderEnums.MessageType.NETREVEAL_RESPONSE_V001_01);
  }

  @Override
  public void close() {
    // This need to remain empty as we are not doing any cleanup here.
  }
}
