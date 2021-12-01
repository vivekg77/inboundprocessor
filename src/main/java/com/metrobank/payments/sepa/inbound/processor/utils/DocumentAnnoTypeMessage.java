/*
 * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.payments.sepa.inbound.processor.utils;

import com.metrobank.payments.sepa.external.schema.model.avro.netreveal.AnonType_Message;
import com.metrobank.payments.sepa.external.schema.model.avro.pacs008.Document;
import lombok.Builder;
import lombok.Data;

/** Description: The class to hold both message and anontype message. */
@Builder
@Data
public class DocumentAnnoTypeMessage {
  Document document;
  AnonType_Message annoType;
}
