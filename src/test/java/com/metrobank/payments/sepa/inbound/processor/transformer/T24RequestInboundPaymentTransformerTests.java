/*
 * * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.payments.sepa.inbound.processor.transformer;

import com.metrobank.payments.sepa.external.schema.model.avro.netreveal.AnonType_Message;
import com.metrobank.payments.sepa.inbound.processor.domain.t24.model.SepaPayment;
import com.metrobank.payments.sepa.inbound.processor.domain.t24.model.SepaPaymentResponse;
import com.metrobank.payments.sepa.inbound.processor.service.T24RequestSepaInboundService;
import com.metrobank.payments.sepa.inbound.processor.utils.DocumentAnnoTypeMessage;
import com.metrobank.payments.sepa.inbound.processor.utils.TestDataHelper;
import com.metrobank.payments.sepa.inbound.processor.utils.Tuple;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.streams.processor.MockProcessorContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

/**
 * Description: Test T24 payment initiation transformer
 */
class T24RequestInboundPaymentTransformerTests {

  @Mock
  T24RequestSepaInboundService t24RequestSepaInboundService;
  private T24RequestInboundPaymentTransformer transformer;

  @BeforeEach
  public void setup() {
    t24RequestSepaInboundService = Mockito.mock(T24RequestSepaInboundService.class);
    transformer = new T24RequestInboundPaymentTransformer(t24RequestSepaInboundService);
  }

  @Test
  @DisplayName("Test T24 Payment Initiation Transformer")
  void test_t24PaymentInitiationTransformer() {
    // Given
    MockProcessorContext context = new MockProcessorContext();
    context.setRecordMetadata("dummy", 1, 0, new RecordHeaders(), 1);

    // and
    transformer.init(context);

    // When
    given(t24RequestSepaInboundService.initiateSepaReturn(Mockito.any(SepaPayment.class)))
        .willReturn(Mono.just(SepaPaymentResponse.builder().build()));

    Tuple<AnonType_Message, SepaPaymentResponse> documentApiRequestResponse =
        transformer.transform(
            DocumentAnnoTypeMessage.builder()
                .document(TestDataHelper.getPacs008AvroDocument())
                .annoType(TestDataHelper.getHitDisApprovedTestPayload())
                .build());

    // Then
    assertThat(documentApiRequestResponse).isNotNull();
    assertEquals("12345#SCT", documentApiRequestResponse.getMessage().getId());
  }

  @AfterEach
  void cleanUp() {
    transformer.close();
  }
}
