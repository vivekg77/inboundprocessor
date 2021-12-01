/*
 * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.payments.sepa.inbound.processor.transformer;

import com.metrobank.header.util.HeaderConstants;
import com.metrobank.header.util.HeaderEnums;
import com.metrobank.payments.sepa.external.schema.model.avro.netreveal.AnonType_Message;
import com.metrobank.payments.sepa.external.schema.utils.AvroTypeMapper;
import com.metrobank.payments.sepa.inbound.processor.domain.t24.model.SepaPaymentResponse;
import com.metrobank.payments.sepa.inbound.processor.utils.GeneralUtils;
import com.metrobank.payments.sepa.inbound.processor.utils.InboundProcessorConstants;
import com.metrobank.payments.sepa.inbound.processor.utils.Tuple;
import com.metrobank.storeandforward.exception.handler.service.StoreForwardExceptionHandlerService;
import com.metrobank.storeandforward.internal.exceptionpayload.model.avro.StoreAndForwardExceptionPayload;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Description: This transformer converts the error response to store and forward exception
 * response.
 */
@Log4j2
public class ErrorResponseTransformer
    implements ValueTransformer<
        Tuple<AnonType_Message, SepaPaymentResponse>, StoreAndForwardExceptionPayload> {
  // Using the Exception Handler, generate the Store and Forward Exception Payload
  private StoreForwardExceptionHandlerService service =
      new StoreForwardExceptionHandlerService(
          InboundProcessorConstants.STORE_AND_FORWARD_CONFIG_FILE);

  private ProcessorContext context;

  @Override
  public void init(ProcessorContext context) {
    this.context = context;
  }

  @SneakyThrows
  @Override
  public StoreAndForwardExceptionPayload transform(
      Tuple<AnonType_Message, SepaPaymentResponse> value) {
    addAdditionalHeaders(context);

    return handleStoreAndForwardExceptions(context, value.getMessage());
  }

  /**
   * Description: This method handles store and forward exception details to be sent over the error
   * topic.
   *
   * @param context The processor context of the kafka topic.
   * @param value The payload from input request topic.
   * @return StoreAndForwardExceptionPayload
   * @throws IOException Common IO exception
   * @throws ClassNotFoundException Class not found exception
   */
  private StoreAndForwardExceptionPayload handleStoreAndForwardExceptions(
      ProcessorContext context, AnonType_Message value) throws IOException, ClassNotFoundException {

    String storeAndForwardExceptionName = InboundProcessorConstants.T24_SEPA_PAYMENT_EXCEPTION_NAME;
    String storeAndForwardExceptionReason = getExceptionReason(storeAndForwardExceptionName);
    log.info("context.topic() {} ", context.topic());
    return service.handleException(
        context.topic(),
        value,
        AvroTypeMapper.findAvroTypeByClass(AnonType_Message.class),
        storeAndForwardExceptionName,
        storeAndForwardExceptionReason,
        context.headers());
  }

  /**
   * Description: This method derives exception reason corresponding to the given exception reason
   * code.
   *
   * @param storeAndForwardExceptionName The name of store and forward exception.
   * @return storeAndForwardExceptionReason
   */
  private String getExceptionReason(String storeAndForwardExceptionName) {
    String storeAndForwardExceptionReason = null;

    if (storeAndForwardExceptionName.equals(
        InboundProcessorConstants.T24_SEPA_PAYMENT_EXCEPTION_NAME)) {
      storeAndForwardExceptionReason = InboundProcessorConstants.T24_SEPA_PAYMENT_EXCEPTION_REASON;
    }
    return storeAndForwardExceptionReason;
  }

  /**
   * Description: Adds additional headers.
   *
   * @param context Kstream Context with headers
   */
  private void addAdditionalHeaders(ProcessorContext context) {

    GeneralUtils.fetchHeaders(
        context,
        InboundProcessorConstants.SEPA_INBOUND_PAYMENT_ERROR,
        HeaderEnums.MessageType.NETREVEAL_RESPONSE_V001_01);
    context
        .headers()
        .add(
            HeaderConstants.INSERT_TIMESTAMP,
            LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern(InboundProcessorConstants.DATE_PATTERN))
                .getBytes());
  }

  @Override
  public void close() {
    // This need to remain empty as we are not doing any cleanup here.
  }
}
