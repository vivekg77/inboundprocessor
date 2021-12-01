/*
 * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.payments.sepa.inbound.processor.utils;

import com.metrobank.payments.sepa.external.schema.model.avro.netreveal.*;
import com.metrobank.payments.sepa.external.schema.model.avro.pacs008.*;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Description: A class that generated dummy data for integration test cases
 *
 */
@Log4j2
public class TestDataHelper {

  /**
   * A method used to obtain a dummy success response from T24 in a raw json format
   *
   * @return raw dummy json
   * @throws IOException - thrown when unable to read from file
   */
  public static String getDummyT24ResponseJson() throws IOException {
    String path = "src/test/resources/t24/2xxResponse.json";
    return Files.readString(Paths.get(path));
  }

  /**
   * A method to generate the hit disapproved AnonType_Message
   *
   * @return hit disapproved AnonType_Message
   */
  public static AnonType_Message getHitDisApprovedTestPayload() {
    return AnonType_Message.newBuilder()
        .setId("12345#SCT")
        .setResult(
            AnonType_ResultMessage.newBuilder()
                .setResults(
                    AnonType_resultsResultMessage.newBuilder()
                        .setResult(
                            AnonType_resultresultsResultMessage.newBuilder()
                                .setRecordNumber(0)
                                .setFunctionValue(
                                    AnonType_functionresultresultsResultMessage.newBuilder()
                                        .setName("WLM-PROCESS")
                                        .setOrderValue(1)
                                        .setResultItem(
                                            AnonType_resultItemfunctionresultresultsResultMessage
                                                .newBuilder()
                                                .setId("1234")
                                                .setType("resultType")
                                                .setProperty(
                                                    AnonType_propertyresultItemfunctionresultresultsResultMessage
                                                        .newBuilder()
                                                        .setKey("code")
                                                        .setValue("DISAPPROVED")
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build())
        .build();
  }

  /**
   * A method to generate the valid Pacs008 Avro message
   *
   * @return valid message
   */
  public static Document
      getPacs008AvroDocument() {
    List<CreditTransferTransactionInformation11> CdtTrfTxInfList = new ArrayList<>();
    CreditTransferTransactionInformation11 CdtTrfTxInf =
        CreditTransferTransactionInformation11.newBuilder()
            .setPmtId(
                AnonType_PmtIdCreditTransferTransactionInformation11.newBuilder()
                    .setInstrId(null)
                    .setEndToEndId("IAIRLeFontiAwards2017")
                    .setTxId("B170301M2543452")
                    .build())
            .setPmtTpInf(
                S2SCTPaymentTypeInformation21.newBuilder()
                    .setLclInstrm(null)
                    .setCtgyPurp(null)
                    .setSvcLvl(S2SCTServiceLevel8Choice.newBuilder().setCd("SEPA").build())
                    .build())
            .setIntrBkSttlmAmt(
                S2SCTCurrencyAndAmount.newBuilder().setCcy("EUR").setTextValue(11.11).build())
            .setIntrBkSttlmDt(null)
            .setAccptncDtTm(null)
            .setInstdAmt(null)
            .setXchgRate(null)
            .setChrgBr("SLEV")
            .setChrgsInf(null)
            .setPrvsInstgAgt(null)
            .setInstgAgt(
                S2SCTBranchAndFinancialInstitutionIdentification4.newBuilder()
                    .setFinInstnId(
                        S2SCTFinancialInstitutionIdentification7.newBuilder()
                            .setBIC("BBRUBEBB")
                            .build())
                    .build())
            .setUltmtDbtr(null)
            .setDbtr(
                S2SCTPartyIdentification322.newBuilder()
                    .setNm("PENNYBRIDGE AB")
                    .setPstlAdr(
                        S2SCTPostalAddress6.newBuilder()
                            .setCtry("SE")
                            .setAdrLine(
                                new ArrayList<>() {
                                  {
                                    add("ALVTOMAGATAN 14");
                                    add("70342 OREBRO");
                                  }
                                })
                            .build())
                    .setId(null)
                    .setCtryOfRes(null)
                    .build())
            .setDbtrAcct(
                S2SCTCashAccount161.newBuilder()
                    .setId(
                        S2SCTAccountIdentification4Choice.newBuilder()
                            .setIBAN("GB27MYMB23058017142259")
                            .build())
                    .build())
            .setDbtrAgt(
                S2SCTBranchAndFinancialInstitutionIdentification4.newBuilder()
                    .setFinInstnId(
                        S2SCTFinancialInstitutionIdentification7.newBuilder()
                            .setBIC("SWEDSESS")
                            .build())
                    .build())
            .setCdtrAgt(
                S2SCTBranchAndFinancialInstitutionIdentification4.newBuilder()
                    .setFinInstnId(
                        S2SCTFinancialInstitutionIdentification7.newBuilder()
                            .setBIC("MYMBGB2L")
                            .build())
                    .build())
            .setCdtr(
                S2SCTPartyIdentification322.newBuilder()
                    .setNm("IAIR AWARDS LIMITED")
                    .setPstlAdr(
                        S2SCTPostalAddress6.newBuilder()
                            .setCtry("GB")
                            .setAdrLine(
                                new ArrayList<>() {
                                  {
                                    add("SUITE 28 DREW HOUSE 23 WHARF STREET");
                                    add("LONDON, SE8 3GG UNITED KINGDOM");
                                  }
                                })
                            .build())
                    .setId(
                        S2SCTParty6Choice.newBuilder()
                            .setOrgId(null)
                            .setPrvtId(
                                S2SCTPersonIdentification51.newBuilder()
                                    .setDtAndPlcOfBirth(null)
                                    .setOthr(
                                        GenericPersonIdentification1.newBuilder()
                                            .setId("IAIR Le Fonti Awards")
                                            .setSchmeNm(null)
                                            .setIssr(null)
                                            .build())
                                    .build())
                            .build())
                    .setCtryOfRes(null)
                    .build())
            .setCdtrAcct(
                S2SCTCashAccount161.newBuilder()
                    .setId(
                        S2SCTAccountIdentification4Choice.newBuilder()
                            .setIBAN("GB27MYMB23058017142259")
                            .build())
                    .build())
            .setUltmtCdtr(null)
            .setInstrForCdtrAgt(null)
            .setPurp(null)
            .setRgltryRptg(null)
            .setRltdRmtInf(null)
            .setRmtInf(null)
            .build();
    CdtTrfTxInfList.add(CdtTrfTxInf);

    return Document.newBuilder()
        .setFIToFICstmrCdtTrf(
            FIToFICustomerCreditTransferV02.newBuilder()
                .setGrpHdr(
                    S2SCTGroupHeader33.newBuilder()
                        .setMsgId("0203201768")
                        .setCreDtTm("1488440300000")
                        .setNbOfTxs("1")
                        .setTtlIntrBkSttlmAmt(
                            S2SCTCurrencyAndAmount.newBuilder()
                                .setCcy("EUR")
                                .setTextValue(11.11)
                                .build())
                        .setIntrBkSttlmDt("1488440300000")
                        .setSttlmInf(
                            S2SCTSettlementInformation13.newBuilder()
                                .setSttlmMtd("CLRG")
                                .setClrSys(
                                    S2SCTClearingSystemIdentification3Choice.newBuilder()
                                        .setPrtry("ST2")
                                        .build())
                                .build())
                        .setInstgAgt(null)
                        .setInstdAgt(
                            S2SCTBranchAndFinancialInstitutionIdentification4.newBuilder()
                                .setFinInstnId(
                                    S2SCTFinancialInstitutionIdentification7.newBuilder()
                                        .setBIC("MYMBGB2L")
                                        .build())
                                .build())
                        .build())
                .setCdtTrfTxInf(CdtTrfTxInfList)
                .build())
        .build();
  }
}
