/*
 * * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.payments.sepa.inbound.processor.transformer;

import com.metrobank.payments.sepa.external.schema.model.avro.netreveal.AnonType_Message;
import com.metrobank.payments.sepa.inbound.processor.domain.t24.model.SepaPayment;
import com.metrobank.payments.sepa.inbound.processor.domain.t24.model.SepaPaymentBody;
import com.metrobank.payments.sepa.inbound.processor.domain.t24.model.SepaPaymentResponse;
import com.metrobank.payments.sepa.inbound.processor.service.T24RequestSepaInboundService;
import com.metrobank.payments.sepa.inbound.processor.utils.DocumentAnnoTypeMessage;
import com.metrobank.payments.sepa.inbound.processor.utils.Tuple;
import com.metrobank.payments.sepa.mapping.utils.AvroToFinMT;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;

import java.util.Arrays;

/** Description: The transformer for T24 payment initiation. */
@Log4j2
public class T24RequestInboundPaymentTransformer
    implements ValueTransformer<
        DocumentAnnoTypeMessage, Tuple<AnonType_Message, SepaPaymentResponse>> {
  private T24RequestSepaInboundService t24RequestSepaInboundService;

  public T24RequestInboundPaymentTransformer(
      T24RequestSepaInboundService t24SepaReturnInitiationService) {
    this.t24RequestSepaInboundService = t24SepaReturnInitiationService;
  }

  @Override
  public void init(ProcessorContext processorContext) {
    /*No processing on the processor context is required.*/
  }

  @Override
  public Tuple<AnonType_Message, SepaPaymentResponse> transform(
      DocumentAnnoTypeMessage documentAnnoTypeMessage) {

    /* Make T24 Call */
    SepaPaymentResponse sepaPaymentResponse = null;
    try {
      sepaPaymentResponse =
          t24RequestSepaInboundService
              .initiateSepaReturn(
                  SepaPayment.builder()
                      .body(
                          SepaPaymentBody.builder()
                              .inMsg(
                                  AvroToFinMT.convert(documentAnnoTypeMessage.getDocument())
                                      .getFINString())
                              .build())
                      .build())
              .block();
    } catch (RuntimeException exception) {
      log.info(Arrays.asList(exception.getStackTrace()));
    }
    return Tuple.<AnonType_Message, SepaPaymentResponse>builder()
        .message(documentAnnoTypeMessage.getAnnoType())
        .response(sepaPaymentResponse)
        .build();
  }

  @Override
  public void close() {
    /*Overridden the abstract method of the transformer*/
  }
}
