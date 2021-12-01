/*
 * * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.payments.sepa.inbound.processor.streams;

import com.metrobank.header.util.HeaderConstants;
import com.metrobank.header.util.HeaderEnums;
import com.metrobank.payments.common.NullPayload;
import com.metrobank.payments.sepa.external.schema.model.avro.netreveal.AnonType_Message;
import com.metrobank.payments.sepa.external.schema.model.avro.pacs008.Document;
import com.metrobank.payments.sepa.inbound.processor.utils.InboundProcessorConstants;
import com.metrobank.payments.sepa.inbound.processor.utils.TestDataHelper;
import com.metrobank.storeandforward.internal.exceptionpayload.model.avro.StoreAndForwardExceptionPayload;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.schemaregistry.testutil.MockSchemaRegistry;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.test.TestRecord;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.kafka.streams.KafkaStreamsBinderSupportAutoConfiguration;
import org.springframework.cloud.stream.binder.kafka.streams.function.KafkaStreamsFunctionAutoConfiguration;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Description: A test suite to handle scenarios of inbound processor topology.

 */
@Log4j2
@SpringBootTest
@ContextConfiguration
@EmbeddedKafka
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext
@EnableAutoConfiguration(
    exclude = {
      KafkaStreamsBinderSupportAutoConfiguration.class,
      KafkaStreamsFunctionAutoConfiguration.class
    })
class TopologyTests {
  // Mock Schema registry
  private static final String SCHEMA_REGISTRY_SCOPE = TopologyTests.class.getName();
  private static final String MOCK_SCHEMA_REGISTRY_URL = "mock://" + SCHEMA_REGISTRY_SCOPE;
  private static final String INBOUND_REQUEST_TOPIC_NAME =
      "dev.private.payments.sepa.inbound.request";
  private static final String MONITORING_TOPIC_NAME =
      "dev.private.payments.sepa.inbound.monitoring";
  private static final String REQUEST_RELEASE_TOPIC_NAME = "dev.private.payments.sepa.inbound.release";
  private static final String PAYMENT_PROCESSED_TOPIC_NAME =
      "dev.private.payments.sepa.inbound.processed";
  private static final String ERROR_TOPIC_NAME = "dev.private.payments.sepa.inbound.error";
  // required serdes
  private final Serde<String> stringSerde = new Serdes.StringSerde();
  // Input topics
  private TestInputTopic<String, Document> requestTopic;
  private TestInputTopic<String, AnonType_Message> releaseTopic; // input

  // Output topics
  private TestOutputTopic<String, NullPayload> monitoringTopic;
  private TestOutputTopic<String, StoreAndForwardExceptionPayload> errorOutputTopic;
  private SpecificAvroSerde<AnonType_Message> anonTypeSerde;
  private SpecificAvroSerde<Document> documentPayloadSerde;
  private SpecificAvroSerde<StoreAndForwardExceptionPayload> errorSerde;
  private SpecificAvroSerde<NullPayload> nullPayloadSerde;
  private Properties props;
  private StreamsBuilder builder;

  @Autowired
  private Function<
          KStream<String, AnonType_Message>,
          Function<
              KTable<String, AnonType_Message>,
              Function<KTable<String, Document>, KStream<String, AnonType_Message>>>>
          paymentsSepaInboundProcess;

  /** A method used to setup the topology test driver */
  @BeforeEach
  public void setup() throws IOException, RestClientException {
    final SchemaRegistryClient schemaRegistryClient =
        MockSchemaRegistry.getClientForScope(SCHEMA_REGISTRY_SCOPE);
    ParsedSchema anonTypeSchema = new AvroSchema(AnonType_Message.getClassSchema());
    ParsedSchema nullPayloadSchema = new AvroSchema(NullPayload.getClassSchema());
    ParsedSchema documentSchema = new AvroSchema(Document.getClassSchema());
    ParsedSchema errorSchema = new AvroSchema(StoreAndForwardExceptionPayload.getClassSchema());

    schemaRegistryClient.register(REQUEST_RELEASE_TOPIC_NAME + "-value", anonTypeSchema);
    schemaRegistryClient.register(INBOUND_REQUEST_TOPIC_NAME + "-value", documentSchema);
    schemaRegistryClient.register(PAYMENT_PROCESSED_TOPIC_NAME + "-value", anonTypeSchema);
    schemaRegistryClient.register(MONITORING_TOPIC_NAME + "-value", nullPayloadSchema);
    schemaRegistryClient.register(ERROR_TOPIC_NAME + "-value", errorSchema);

    // create a topology builder and build the topology
    builder = new StreamsBuilder();
    KTable<String, Document> tableSource1 = builder.table(INBOUND_REQUEST_TOPIC_NAME);
    KStream<String, AnonType_Message> streamSource2 = builder.stream(REQUEST_RELEASE_TOPIC_NAME);
    KTable<String, AnonType_Message> tableSource3 = builder.table(PAYMENT_PROCESSED_TOPIC_NAME);

    // Create a Topology driver with the topology
    props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9091");
    props.setProperty(
        StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    props.setProperty(
        StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class.getName());
    props.setProperty(
        AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, MOCK_SCHEMA_REGISTRY_URL);
    Map<String, String> config =
        Map.of(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, MOCK_SCHEMA_REGISTRY_URL);
    anonTypeSerde = new SpecificAvroSerde<>(schemaRegistryClient);
    anonTypeSerde.configure(config, false);
    nullPayloadSerde = new SpecificAvroSerde<>(schemaRegistryClient);
    nullPayloadSerde.configure(config, false);
    documentPayloadSerde = new SpecificAvroSerde<>(schemaRegistryClient);
    documentPayloadSerde.configure(config, false);

    errorSerde = new SpecificAvroSerde<>(schemaRegistryClient);
    errorSerde.configure(config, false);

    KStream<String, AnonType_Message> streams =
            paymentsSepaInboundProcess.apply(streamSource2).apply(tableSource3).apply(tableSource1);
    streams.to(PAYMENT_PROCESSED_TOPIC_NAME);
  }

  @Test
  @DisplayName("Test inbound Payment Failed")
  void test_inboundPaymentFailed() {

    try (TopologyTestDriver testDriver = new TopologyTestDriver(builder.build(), props)) {
      releaseTopic =
          testDriver.createInputTopic(
                  REQUEST_RELEASE_TOPIC_NAME, stringSerde.serializer(), anonTypeSerde.serializer());
      requestTopic =
          testDriver.createInputTopic(
              INBOUND_REQUEST_TOPIC_NAME,
              stringSerde.serializer(),
              documentPayloadSerde.serializer());
      errorOutputTopic =
          testDriver.createOutputTopic(
              ERROR_TOPIC_NAME, stringSerde.deserializer(), errorSerde.deserializer());

      AnonType_Message testRequest;
      Document testDocument;
      // given
      {
        testRequest = TestDataHelper.getHitDisApprovedTestPayload();
        testDocument = TestDataHelper.getPacs008AvroDocument();
      }
      // when
      {
        requestTopic.pipeInput("testKey1", testDocument);
        releaseTopic.pipeInput("testKey1", testRequest);


      }
      // then
      {
        //log.info("something" + errorOutputTopic.toString());
        // test the values
        assertFalse(errorOutputTopic.isEmpty());
        // test the values
        TestRecord<String, StoreAndForwardExceptionPayload> testRecord =
            errorOutputTopic.readRecord();
        StoreAndForwardExceptionPayload receivedOnRequest = testRecord.getValue();
        Assertions.assertNotNull(receivedOnRequest);
        String key = testRecord.getKey();
        Assertions.assertNotNull(key);
        assertEquals("testKey1", key);

        // Test Headers
        Headers headers = testRecord.getHeaders();

        Header monitoringHeader = headers.lastHeader(HeaderConstants.MONITORING_HEADER);
        Assertions.assertNotNull(monitoringHeader);

        Header statusHeaderFlowDirection = headers.lastHeader(HeaderConstants.FLOW_DIRECTION);
        Assertions.assertNotNull(statusHeaderFlowDirection);
        assertEquals(
                InboundProcessorConstants.SEPA_INBOUND_PAYMENT_ERROR,
            new String(headers.lastHeader(HeaderConstants.MONITORING_HEADER).value()));
        assertEquals(
            HeaderEnums.FlowDirection.INBOUND.getValue(),
            new String(statusHeaderFlowDirection.value()));
      }
    }
  }

  @Test
  @DisplayName("Test inbound Payment not found")
  void test_inboundPaymentNotFound() {
    try (TopologyTestDriver testDriver = new TopologyTestDriver(builder.build(), props)) {

      releaseTopic =
          testDriver.createInputTopic(
                  REQUEST_RELEASE_TOPIC_NAME, stringSerde.serializer(), anonTypeSerde.serializer());
      monitoringTopic =
          testDriver.createOutputTopic(
              MONITORING_TOPIC_NAME, stringSerde.deserializer(), nullPayloadSerde.deserializer());
      TestOutputTopic<String, AnonType_Message> returnProcessedOutputTopic =
          testDriver.createOutputTopic(
                  PAYMENT_PROCESSED_TOPIC_NAME,
              stringSerde.deserializer(),
              anonTypeSerde.deserializer());
      errorOutputTopic =
          testDriver.createOutputTopic(
              ERROR_TOPIC_NAME, stringSerde.deserializer(), errorSerde.deserializer());

      AnonType_Message testRequest;
      // given
      {
        testRequest = TestDataHelper.getHitDisApprovedTestPayload();
      }
      // when
      {
        releaseTopic.pipeInput("testKey2", testRequest);
      }
      // then
      {
        // test the values
        assertTrue(returnProcessedOutputTopic.isEmpty());
        assertFalse(monitoringTopic.isEmpty());

        // test the values
        TestRecord<String, NullPayload> testRecord = monitoringTopic.readRecord();
        NullPayload receivedOnRequest = testRecord.getValue();
        Assertions.assertNotNull(receivedOnRequest);
        String key = testRecord.getKey();
        Assertions.assertNotNull(key);
        assertEquals("testKey2", key);

        // Test Headers
        Headers headers = testRecord.getHeaders();

        Header monitoringHeader = headers.lastHeader(HeaderConstants.MONITORING_HEADER);
        Assertions.assertNotNull(monitoringHeader);
        Header statusHeaderFlowDirection = headers.lastHeader(HeaderConstants.FLOW_DIRECTION);
        Assertions.assertNotNull(statusHeaderFlowDirection);
        assertEquals(
                InboundProcessorConstants.SEPA_INBOUND_PAYMENT_NOT_FOUND,
            new String(headers.lastHeader(HeaderConstants.MONITORING_HEADER).value()));
        Assertions.assertEquals(
            HeaderEnums.FlowDirection.INBOUND.getValue(),
            new String(statusHeaderFlowDirection.value()));
      }
    }
  }

  @Test
  @DisplayName("Test inbound Payment duplicate check")
  void test_inboundAmlReturnDuplicateCheck() {
    try (TopologyTestDriver testDriver = new TopologyTestDriver(builder.build(), props)) {

      releaseTopic =
          testDriver.createInputTopic(
              REQUEST_RELEASE_TOPIC_NAME, stringSerde.serializer(), anonTypeSerde.serializer());
      requestTopic =
          testDriver.createInputTopic(
              INBOUND_REQUEST_TOPIC_NAME,
              stringSerde.serializer(),
              documentPayloadSerde.serializer());
      TestInputTopic<String, AnonType_Message> returnProcessedTopic =
          testDriver.createInputTopic(
                  PAYMENT_PROCESSED_TOPIC_NAME, stringSerde.serializer(), anonTypeSerde.serializer());
      monitoringTopic =
          testDriver.createOutputTopic(
              MONITORING_TOPIC_NAME, stringSerde.deserializer(), nullPayloadSerde.deserializer());
      AnonType_Message testRequest;
      // given
      {
        testRequest = TestDataHelper.getHitDisApprovedTestPayload();
        log.info("display testRequest55555" + testRequest);
      }
      // when
      {
        returnProcessedTopic.pipeInput("testKey3", testRequest);
        releaseTopic.pipeInput("testKey3", testRequest);
      }
      // then
      {
        // test the values
        TestRecord<String, NullPayload> testRecord = monitoringTopic.readRecord();
        NullPayload receivedOnRequest = testRecord.getValue();
        Assertions.assertNotNull(receivedOnRequest);
        String key = testRecord.getKey();
        Assertions.assertNotNull(key);
        assertEquals("testKey3", key);

        // Test Headers
        Headers headers = testRecord.getHeaders();

        Header monitoringHeader = headers.lastHeader(HeaderConstants.MONITORING_HEADER);
        Assertions.assertNotNull(monitoringHeader);
        assertEquals(
                InboundProcessorConstants.SEPA_INBOUND_PAYMENT_DUPLICATE,
            new String(headers.lastHeader(HeaderConstants.MONITORING_HEADER).value()));
        Header statusHeaderFlowDirection = headers.lastHeader(HeaderConstants.FLOW_DIRECTION);
        Assertions.assertNotNull(statusHeaderFlowDirection);
        Assertions.assertEquals(
            HeaderEnums.FlowDirection.INBOUND.getValue(),
            new String(statusHeaderFlowDirection.value()));
      }
    }
  }

  /** TearDown Method */
  @AfterEach
  public void tearDown() {
    MockSchemaRegistry.dropScope(SCHEMA_REGISTRY_SCOPE);
  }
}
