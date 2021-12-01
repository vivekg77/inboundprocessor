/*
 * Copyright 2021 Metro Bank. All rights reserved.
 */

package com.metrobank.payments.sepa.inbound.processor.service.impl;

import com.metrobank.payments.sepa.inbound.processor.domain.t24.model.SepaPayment;
import com.metrobank.payments.sepa.inbound.processor.domain.t24.model.SepaPaymentResponse;
import com.metrobank.payments.sepa.inbound.processor.service.T24RequestSepaInboundService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/** Default implementation of the T24 Request service. */
@Log4j2
@Service
public class T24RequestSepaInboundServiceImpl implements T24RequestSepaInboundService {

  private WebClient webClient;

  public T24RequestSepaInboundServiceImpl(WebClient webClient) {
    this.webClient = webClient;
  }

  public Mono<SepaPaymentResponse> initiateSepaReturn(SepaPayment sepaPayment) {

    log.info("Calling initiateSepaReturn API");

    return webClient
        .post() // the post method of the API is being called
        .contentType(MediaType.APPLICATION_JSON) // Setting the content type to be application/json
        .bodyValue(sepaPayment) // Setting the request body
        .accept(MediaType.APPLICATION_JSON) // Setting Accept: application/json
        .exchange()
        .log()
        .flatMap(
            clientResponse -> {
              if (clientResponse.statusCode().isError()) {
                return clientResponse.createException().flatMap(Mono::error);
              } else {
                return clientResponse
                    .bodyToMono(SepaPaymentResponse.class)
                    .timeout(Duration.ofMillis(300L)); // will throw ReadTimeoutException
                // This is the timeout that comes to the picture if a given publisher
                // failed to emit the next signal within the given time.
              }
            });
  }
}
