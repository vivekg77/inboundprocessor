/*
 * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.payments.sepa.inbound.processor.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/** Description: Web configuration to obtain the be web client bean. */
@Configuration
@Log4j2
public class WebClientConfig {

  @Value("${spring.t24.api.url}")
  protected String t24ApiUrl;

  /**
   * Description: The webClient bean to call Restful API.
   *
   * @return webClient bean object.
   */
  @Bean
  public WebClient webClient() {
    HttpClient httpClient = HttpClient.create();
    log.info("Creating webClient bean");
    return WebClient.builder()
        .baseUrl(t24ApiUrl)
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .build();
  }
}
