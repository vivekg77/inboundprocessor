/*
 * Copyright 2021 Metro Bank. All rights reserved.
 */

package com.metrobank.payments.sepa.inbound.processor.service;

import com.metrobank.payments.sepa.inbound.processor.domain.t24.model.SepaPayment;
import com.metrobank.payments.sepa.inbound.processor.domain.t24.model.SepaPaymentResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/** This request makes a blocking REST call to T24 service */
@Service
public interface T24RequestSepaInboundService {

  Mono<SepaPaymentResponse> initiateSepaReturn(SepaPayment sepaPayment);
}
