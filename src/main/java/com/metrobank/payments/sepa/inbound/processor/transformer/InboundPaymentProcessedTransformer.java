/*
 * * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.payments.sepa.inbound.processor.transformer;

import com.metrobank.header.util.HeaderEnums;
import com.metrobank.payments.sepa.external.schema.model.avro.netreveal.AnonType_Message;
import com.metrobank.payments.sepa.inbound.processor.domain.t24.model.SepaPaymentResponse;
import com.metrobank.payments.sepa.inbound.processor.utils.GeneralUtils;
import com.metrobank.payments.sepa.inbound.processor.utils.InboundProcessorConstants;
import com.metrobank.payments.sepa.inbound.processor.utils.Tuple;
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;

/** Description: The transform for inbound payment processed messages. */
public class InboundPaymentProcessedTransformer
    implements ValueTransformer<Tuple<AnonType_Message, SepaPaymentResponse>, AnonType_Message> {

  private ProcessorContext context;

  @Override
  public void init(ProcessorContext processorContext) {
    this.context = processorContext;
    /*No processing on the processor context is required.*/
  }

  @Override
  public AnonType_Message transform(
      Tuple<AnonType_Message, SepaPaymentResponse> documentApiRequestResponse) {
    addAdditionalHeaders(context);
    return documentApiRequestResponse.getMessage();
  }
  /**
   * Description: Adds additional headers.
   *
   * @param context Kstream Context with headers
   */
  private void addAdditionalHeaders(ProcessorContext context) {
    GeneralUtils.fetchHeaders(
        context,
        InboundProcessorConstants.SEPA_INBOUND_PAYMENT_PROCESSED,
        HeaderEnums.MessageType.NETREVEAL_RESPONSE_V001_01);
  }

  @Override
  public void close() {
    /*Overridden the abstract method of the transformer*/
  }
}
