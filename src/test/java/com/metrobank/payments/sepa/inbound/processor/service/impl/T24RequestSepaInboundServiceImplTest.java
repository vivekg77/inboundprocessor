package com.metrobank.payments.sepa.inbound.processor.service.impl;


import com.metrobank.payments.sepa.inbound.processor.domain.t24.model.SepaPayment;
import com.metrobank.payments.sepa.inbound.processor.domain.t24.model.SepaPaymentResponse;
import com.metrobank.payments.sepa.inbound.processor.utils.TestDataHelper;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.streams.processor.MockProcessorContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class T24RequestSepaInboundServiceImplTest {

    private static final String BASE_URL = "http://localhost:8080/api/v1.0.0/order/sepaPayment";
    private String responseJson;
    private WebClient webClientMock;
    private T24RequestSepaInboundServiceImpl t24RequestSepaInboundService;

    @BeforeEach
    public void setup() throws IOException {
        responseJson = TestDataHelper.getDummyT24ResponseJson();
        webClientMock =
                WebClient.builder()
                        .baseUrl(BASE_URL)
                        .exchangeFunction(
                                clientRequest ->
                                        Mono.just(
                                                ClientResponse.create(HttpStatus.OK)
                                                        .header("content-type", "application/json")
                                                        .body(responseJson)
                                                        .build()))
                        .build();
        t24RequestSepaInboundService = new T24RequestSepaInboundServiceImpl(webClientMock);
    }

    @Test
    @DisplayName("Test T24 Payment Initiation Transformer")
    void test_t24PaymentInitiationTransformer() {
        // Given
        MockProcessorContext context = new MockProcessorContext();
        context.setRecordMetadata("dummy", 1, 0, new RecordHeaders(), 1);

        // When
        Mono<SepaPaymentResponse> sepaPaymentResponseMono =
                t24RequestSepaInboundService.initiateSepaReturn(SepaPayment.builder().build());

        // Then
        assertThat(sepaPaymentResponseMono.block()).isNotNull();
    }

    @AfterEach
    void cleanUp() {
        //Nothing to cleanup
    }

}