/*
 * * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.payments.sepa.inbound.processor.transformer;

import com.metrobank.header.util.HeaderConstants;
import com.metrobank.payments.sepa.external.schema.model.avro.netreveal.AnonType_Message;
import com.metrobank.payments.sepa.inbound.processor.domain.t24.model.SepaPaymentResponse;
import com.metrobank.payments.sepa.inbound.processor.utils.TestDataHelper;
import com.metrobank.payments.sepa.inbound.processor.utils.Tuple;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.streams.processor.MockProcessorContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
/**
 * Description: Tests for inbound return processor transformer.
 *
 */
class InboundPaymentProcessedTransformerTests {

  private InboundPaymentProcessedTransformer transformer;

  @BeforeEach
  public void setup() {
    transformer = new InboundPaymentProcessedTransformer();
  }

  @Test
  @DisplayName("Test Inbound Payment Processed Transformer")
  void test_inboundPaymentProcessedTransformer() {
    // Given
    MockProcessorContext context = new MockProcessorContext();
    context.setRecordMetadata("dummy", 1, 0, new RecordHeaders(), 1);

    // and
    transformer.init(context);

    // When
    AnonType_Message document =
        transformer.transform(
            Tuple.<AnonType_Message, SepaPaymentResponse>builder()
                .message(TestDataHelper.getHitDisApprovedTestPayload())
                .build());

    // Then
    assertThat(document).isNotNull();
    assertEquals(
        "INBOUND",
        new String(context.headers().lastHeader(HeaderConstants.FLOW_DIRECTION).value()));
    assertEquals(
        "NETREVEAL_RESPONSE",
        new String(context.headers().lastHeader(HeaderConstants.MESSAGE_TYPE).value()));
    assertEquals(0, document.getResult().getResults().getResult().getRecordNumber());
  }

  @AfterEach
  void cleanUp() {
    transformer.close();
  }
}
