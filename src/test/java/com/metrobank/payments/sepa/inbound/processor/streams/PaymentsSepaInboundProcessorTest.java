/*
 * * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.payments.sepa.inbound.processor.streams;

import com.metrobank.payments.sepa.external.schema.model.avro.netreveal.AnonType_Message;
import com.metrobank.payments.sepa.external.schema.model.avro.pacs008.Document;
import com.metrobank.payments.sepa.inbound.processor.domain.t24.model.SepaPaymentResponse;
import com.metrobank.payments.sepa.inbound.processor.service.T24RequestSepaInboundService;
import com.metrobank.payments.sepa.inbound.processor.service.impl.T24RequestSepaInboundServiceImpl;
import com.metrobank.payments.sepa.inbound.processor.utils.DocumentAnnoTypeMessage;
import com.metrobank.payments.sepa.inbound.processor.utils.Tuple;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.kafka.streams.KafkaStreamsBinderSupportAutoConfiguration;
import org.springframework.cloud.stream.binder.kafka.streams.function.KafkaStreamsFunctionAutoConfiguration;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext
@ActiveProfiles("test")
@EmbeddedKafka
@EnableAutoConfiguration(
        exclude = {
                KafkaStreamsBinderSupportAutoConfiguration.class,
                KafkaStreamsFunctionAutoConfiguration.class
        })
class PaymentsSepaInboundProcessorTest {

  @Autowired private WebClient webClient;

  private T24RequestSepaInboundService t24RequestSepaInboundService;

  private PaymentsSepaInboundProcessor paymentsSepaInboundProcessor;

  @Autowired
  private Function<
          KStream<String, AnonType_Message>,
          Function<
                  KTable<String, AnonType_Message>,
                  Function<KTable<String, Document>, KStream<String, AnonType_Message>>>>
          paymentsSepaInboundProcess;

  @Autowired
  private BiFunction<
          KStream<String, AnonType_Message>,
          KTable<String, AnonType_Message>,
          KStream<String, AnonType_Message>[]>
          identifyDuplicateOrNonDuplicateStream;

  @Autowired
  private BiFunction<
          KStream<String, AnonType_Message>,
          KTable<String, Document>,
          KStream<String, DocumentAnnoTypeMessage>[]>
          identifyNotFoundRequests;


  @BeforeEach
  public void setup() {
    t24RequestSepaInboundService = new T24RequestSepaInboundServiceImpl(webClient);
    paymentsSepaInboundProcessor = new PaymentsSepaInboundProcessor(t24RequestSepaInboundService);
  }

  @Test
  @DisplayName("Test identify not found requests")
  void test_identifyNotFoundRequests() {
    assertNotNull(identifyNotFoundRequests);
  }

  @Test
  @DisplayName("Test identify duplicate Or non-duplicate stream")
  void test_identifyDuplicateOrNonDuplicateStream() {
    assertNotNull(identifyDuplicateOrNonDuplicateStream);
  }

  @Test
  @DisplayName("Test Inbound Payments Processor")
  void test_inboundProcessor() {
    assertNotNull(paymentsSepaInboundProcess);
  }

  @Test
  @DisplayName("Test inbound Payments Processor")
  void test_inboundPaymentProcessor() {
    assertNotNull(paymentsSepaInboundProcessor);
    paymentsSepaInboundProcessor.paymentsSepaInboundProcess();
  }

//  @Test
//  @DisplayName("Test time stamp extractor")
//  void timestampExtractorTest() {
//    assertNotNull(timestampExtractor);
//  }

  @Test
  @DisplayName("Test T24 Sepa Payment API Failure")
  void test_isPaymentProcessingFailure() {
    Tuple<AnonType_Message, SepaPaymentResponse> documentApiRequestResponse =
            Tuple.<AnonType_Message, SepaPaymentResponse>builder().build();
    assertTrue(
            PaymentsSepaInboundProcessor.isPaymentProcessingFailure("12345", documentApiRequestResponse));
    documentApiRequestResponse =
            Tuple.<AnonType_Message, SepaPaymentResponse>builder()
                    .response(SepaPaymentResponse.builder().build())
                    .build();
    assertFalse(
            PaymentsSepaInboundProcessor.isPaymentProcessingFailure("12345", documentApiRequestResponse));
  }

  @Test
  @DisplayName("Test T24 Sepa Payment API Success")
  void test_isPaymentProcessingSuccess() {
    Tuple<AnonType_Message, SepaPaymentResponse> documentApiRequestResponse =
            Tuple.<AnonType_Message, SepaPaymentResponse>builder()
                    .response(SepaPaymentResponse.builder().build())
                    .build();
    assertTrue(
            PaymentsSepaInboundProcessor.isPaymentProcessingSuccessful("12345", documentApiRequestResponse));
    documentApiRequestResponse = Tuple.<AnonType_Message, SepaPaymentResponse>builder().build();
    assertFalse(
            PaymentsSepaInboundProcessor.isPaymentProcessingSuccessful("12345", documentApiRequestResponse));
  }
}
