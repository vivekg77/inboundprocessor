/*
 * * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.payments.sepa.inbound.processor.transformer;

import com.metrobank.header.util.HeaderConstants;
import com.metrobank.payments.common.NullPayload;
import com.metrobank.payments.sepa.external.schema.model.avro.pacs008.Document;
import com.metrobank.payments.sepa.inbound.processor.utils.InboundProcessorConstants;
import com.metrobank.payments.sepa.inbound.processor.utils.TestDataHelper;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.streams.processor.MockProcessorContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Description: Test monitoring topic transformer
 */
class MonitoringTransformerTests {

  private MonitoringTransformer<Document, NullPayload> transformer;

  @BeforeEach
  public void setup() {
    transformer = new MonitoringTransformer<>(InboundProcessorConstants.SEPA_INBOUND_PAYMENT_NOT_FOUND);
  }

  @Test
  @DisplayName("Test Inbound Payment Processed Transformer")
  void test_inboundReturnProcessedTransformer() {
    // Given
    MockProcessorContext context = new MockProcessorContext();
    context.setRecordMetadata("dummy", 1, 0, new RecordHeaders(), 1);

    // and
    transformer.init(context);

    // When
    NullPayload nullPayload = transformer.transform(TestDataHelper.getPacs008AvroDocument());

    // Then
    assertThat(nullPayload).isNotNull();
    assertEquals(
            InboundProcessorConstants.SEPA_INBOUND_PAYMENT_NOT_FOUND,
        new String(context.headers().lastHeader(HeaderConstants.MONITORING_HEADER).value()));
    assertEquals(
            "INBOUND",
            new String(context.headers().lastHeader(HeaderConstants.FLOW_DIRECTION).value()));
  }

  @AfterEach
  void cleanUp() {
    transformer.close();
  }
}
