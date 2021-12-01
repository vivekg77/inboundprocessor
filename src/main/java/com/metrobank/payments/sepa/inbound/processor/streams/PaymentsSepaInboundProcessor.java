/*
 * Copyright 2021 Metro Bank. All rights reserved.
 */

package com.metrobank.payments.sepa.inbound.processor.streams;

import com.metrobank.payments.common.NullPayload;
import com.metrobank.payments.sepa.external.schema.model.avro.netreveal.AnonType_Message;
import com.metrobank.payments.sepa.external.schema.model.avro.pacs008.Document;
import com.metrobank.payments.sepa.inbound.processor.domain.t24.model.SepaPaymentResponse;
import com.metrobank.payments.sepa.inbound.processor.service.T24RequestSepaInboundService;
import com.metrobank.payments.sepa.inbound.processor.transformer.ErrorResponseTransformer;
import com.metrobank.payments.sepa.inbound.processor.transformer.InboundPaymentProcessedTransformer;
import com.metrobank.payments.sepa.inbound.processor.transformer.MonitoringTransformer;
import com.metrobank.payments.sepa.inbound.processor.transformer.T24RequestInboundPaymentTransformer;
import com.metrobank.payments.sepa.inbound.processor.utils.DocumentAnnoTypeMessage;
import com.metrobank.payments.sepa.inbound.processor.utils.InboundProcessorConstants;
import com.metrobank.payments.sepa.inbound.processor.utils.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Description: Consume the events from the SEPA Inbound Release topic and checks if the payment is
 * already processed or not by checking if the payment exists in the SEPA Inbound Processed topic.
 * If Doesn't exist Convert the message to MT103 and send to T24 to initiate the payment. Record the
 * information in SEPA Inbound Processed topic. If Payment already exists, Record the information in
 * SEPA Inbound monitoring topic
 */
@Log4j2
@Configuration
@RequiredArgsConstructor
public class PaymentsSepaInboundProcessor {

  private final T24RequestSepaInboundService t24RequestSepaInboundService;

  @Value("${kafka.payments.sepa.inbound.monitoring.topic}")
  protected String monitoringTopic;

  @Value("${kafka.payments.sepa.inbound.error.topic}")
  protected String inboundErrorTopic;

  @Value("${spring.t24.api.url}")
  protected String t24ApiUrl;

  /**
   * Description: This service will check whether the payment processing failed.
   *
   * @param key The key of the kafka topic.
   * @param documentApiRequestResponse The struct which has message, t24 api request and response.
   * @return Tuple with message, mt103 message and sepaPaymentResponse.
   */
  static boolean isPaymentProcessingFailure(
      String key, Tuple<AnonType_Message, SepaPaymentResponse> documentApiRequestResponse) {
    return documentApiRequestResponse.getResponse() == null;
  }

  /**
   * Description: This service will check whether payment processing is successful.
   *
   * @param key The key of the kafka topic.
   * @param documentApiRequestResponse The struct which has message, t24 api request and response.
   * @return Tuple with message, mt103 message and sepaPaymentResponse.
   */
  static boolean isPaymentProcessingSuccessful(
      String key, Tuple<AnonType_Message, SepaPaymentResponse> documentApiRequestResponse) {
    return documentApiRequestResponse.getResponse() != null;
  }

  /**
   * Description: The topology class for inbound Payment Processor.
   *
   * @return The record to inbound Payments Processed topic.
   */
  @Bean("paymentsSepaInboundProcess")
  public Function<
          KStream<String, AnonType_Message>,
          Function<
              KTable<String, AnonType_Message>,
              Function<KTable<String, Document>, KStream<String, AnonType_Message>>>>
      paymentsSepaInboundProcess() {
    return inboundReleasePaymentKStream ->
        inboundProcessedKTable ->
            inboundPaymentKTable -> {
              log.info("Inside method paymentsSepaInboundProcess");

              /*Adding MDC*/
              inboundReleasePaymentKStream
                  .peek((key, value) -> MDC.put("uuid", key))
                  .peek((key, value) -> log.info("***key: " + key));

              /* Step 1: Left Join Payment Release Stream and Payment Processed KTable to check if the payment is already processed or not, . */
              KStream<String, AnonType_Message>[] duplicateOrNonDuplicateStream =
                  identifyDuplicateOrNonDuplicateStream()
                      .apply(inboundReleasePaymentKStream, inboundProcessedKTable);

              /* Step 2: If the payment is already processed, then send to monitoring topic. */
              duplicateOrNonDuplicateStream[0]
                  .transformValues(
                      () ->
                          new MonitoringTransformer<AnonType_Message, NullPayload>(
                              InboundProcessorConstants.SEPA_INBOUND_PAYMENT_DUPLICATE))
                  .to(monitoringTopic);

              /* Step 3: Left Join between the release topic stream and KTable pertaining to initial payment request topic to see if the payment exists or not */
              KStream<String, DocumentAnnoTypeMessage>[] foundNotFoundStream =
                  identifyNotFoundRequests()
                      .apply(duplicateOrNonDuplicateStream[1], inboundPaymentKTable);

              /*Step 4: If the payment does not exist in the inbound topic, then send to monitoring topic.*/
              foundNotFoundStream[0]
                  .peek((key, value) -> log.info("Final checking the not found record {}" + key))
                  .transformValues(
                      () ->
                          new MonitoringTransformer<DocumentAnnoTypeMessage, NullPayload>(
                              InboundProcessorConstants.SEPA_INBOUND_PAYMENT_NOT_FOUND))
                  .to(monitoringTopic);

              /* Step 6: If the payment exists in the original inbound topic then Convert PACS008 Document to MT103 and call the T24 Restful API for
               * payment processing. */
              KStream<String, Tuple<AnonType_Message, SepaPaymentResponse>> apiResponseStream =
                  foundNotFoundStream[1].transformValues(
                      () -> new T24RequestInboundPaymentTransformer(t24RequestSepaInboundService));

              /* Step 7: Appropriately route the success and failure records to two streams. */
              KStream<String, Tuple<AnonType_Message, SepaPaymentResponse>>[]
                  successFailureKStream =
                      apiResponseStream.branch(
                          PaymentsSepaInboundProcessor::isPaymentProcessingSuccessful,
                          PaymentsSepaInboundProcessor::isPaymentProcessingFailure);

              /* Step 8: Handle Failure Response from T24, Send the Error details to the error topic. */
              successFailureKStream[1]
                  .transformValues(ErrorResponseTransformer::new)
                  .to(inboundErrorTopic);

              /* Step 9: Handle Success Response from T24 */
              return successFailureKStream[0]
                  .peek((key, value) -> log.info("Success Record {} " + key))
                  .transformValues(InboundPaymentProcessedTransformer::new);
            };
  }

  /**
   * A bean that returns a functional interface that is responsible for implementation of a
   * sub-topology that identifies non duplicate messages.
   *
   * @return functional interface
   */
  @Bean
  BiFunction<
          KStream<String, AnonType_Message>,
          KTable<String, AnonType_Message>,
          KStream<String, AnonType_Message>[]>
      identifyDuplicateOrNonDuplicateStream() {
    return PaymentsSepaInboundProcessor::apply;
  }

  private static KStream<String, AnonType_Message>[] apply(
      KStream<String, AnonType_Message> inboundReleasePaymentKStream,
      KTable<String, AnonType_Message> inboundProcessedKTable) {
    return inboundReleasePaymentKStream
        .leftJoin(
            inboundProcessedKTable,
            (leftValue, rightValue) -> rightValue == null ? leftValue : null)
        .branch((key, value) -> value == null, (key, value) -> value != null);
  }

  /**
   * A bean that returns a functional interface that is responsible for the implementation of a
   * sub-topology that identifies the messages that do not have corresponding original payment
   * requests. it does so by taking a left join with the Ktable created from the original payment
   * requests topic.
   *
   * @return a functional interface
   */
  @Bean
  BiFunction<
          KStream<String, AnonType_Message>,
          KTable<String, Document>,
          KStream<String, DocumentAnnoTypeMessage>[]>
      identifyNotFoundRequests() {

    log.info("Identifying Not Found Requests ..");
    return (nonDuplicateStream, inboundPaymentKTable) ->
        nonDuplicateStream
            .leftJoin(
                inboundPaymentKTable,
                (leftValue, rightValue) ->
                    DocumentAnnoTypeMessage.builder()
                        .document(rightValue)
                        .annoType(leftValue)
                        .build())
            .branch(
                (key, value) -> value.getDocument() == null,
                (key, value) -> value.getDocument() != null);
  }
}
