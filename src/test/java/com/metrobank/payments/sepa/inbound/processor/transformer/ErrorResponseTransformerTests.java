/*
 * * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.payments.sepa.inbound.processor.transformer;

import com.metrobank.header.util.HeaderConstants;
import com.metrobank.payments.sepa.external.schema.model.avro.netreveal.AnonType_Message;
import com.metrobank.payments.sepa.inbound.processor.domain.t24.model.SepaPaymentResponse;
import com.metrobank.payments.sepa.inbound.processor.utils.TestDataHelper;
import com.metrobank.payments.sepa.inbound.processor.utils.Tuple;
import com.metrobank.storeandforward.internal.exceptionpayload.model.avro.StoreAndForwardExceptionPayload;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.streams.processor.MockProcessorContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Description: Tests for error response transformer. */
class ErrorResponseTransformerTests {

  private ErrorResponseTransformer transformer;

  @BeforeEach
  public void setup() {
    transformer = new ErrorResponseTransformer();
  }

  @Test
  @DisplayName("Test Error Response Transformer")
  void test_errorResponseTransformer() {
    // Given
    MockProcessorContext context = new MockProcessorContext();
    context.setRecordMetadata("dummy", 1, 0, new RecordHeaders(), 1);

    // and
    transformer.init(context);

    // When
    Tuple<AnonType_Message, SepaPaymentResponse> documentMt103SepaPaymentResponseTuple =
        Tuple.<AnonType_Message, SepaPaymentResponse>builder()
            .message(TestDataHelper.getHitDisApprovedTestPayload())
            .build();

    StoreAndForwardExceptionPayload storeAndForwardExceptionPayload =
        transformer.transform(documentMt103SepaPaymentResponseTuple);
    // Then
    assertThat(storeAndForwardExceptionPayload).isNotNull();
    assertEquals("dummy", storeAndForwardExceptionPayload.getReplayTopic());
    assertEquals(
        "INBOUND",
        new String(context.headers().lastHeader(HeaderConstants.FLOW_DIRECTION).value()));
    assertEquals(
        "NETREVEAL_RESPONSE", new String(context.headers().lastHeader(HeaderConstants.MESSAGE_TYPE).value()));
  }

  @AfterEach
  void cleanUp() {
    transformer.close();
  }
}
