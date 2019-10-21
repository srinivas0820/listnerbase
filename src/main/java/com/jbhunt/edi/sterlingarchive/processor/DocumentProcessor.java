package com.jbhunt.edi.sterlingarchive.processor;

import com.jbhunt.edi.general.dto.ArchiveMessageHeaderUtil;
import com.jbhunt.edi.general.dto.ArchiveMessageHeaderUtilFactory;
import com.jbhunt.edi.sterlingarchive.dao.ArchiveDAO;
import com.jbhunt.edi.sterlingarchive.dto.ArchiveDataDTO;
import com.jbhunt.edi.sterlingarchive.utils.BlobStorageUtil;
import com.jbhunt.edi.sterlingarchive.utils.EventHubSendUtil;
import com.jbhunt.edi.sterlingarchive.utils.HexZipStringConverter;
import com.jbhunt.edi.sterlingarchive.utils.StringExtractorUtil;
import enums.HeaderType;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@Slf4j
@RefreshScope
public class DocumentProcessor implements AsyncProcessor {

    private final ArchiveDAO archiveDao;
    private final BlobStorageUtil blobStorageUtil;
    private final EventHubSendUtil eventHubSendUtil;
    private static final String DOCUMENTS = "documents/{yyyy}/{mm}/{dd}";
    private static final String METADATA = "metadata/{yyyy}/{mm}/{dd}";

    public DocumentProcessor(ArchiveDAO archiveDao, BlobStorageUtil blobStorageUtil, EventHubSendUtil eventHubSendUtil) {
        this.archiveDao = archiveDao;
        this.blobStorageUtil = blobStorageUtil;
        this.eventHubSendUtil = eventHubSendUtil;
    }

    private String folderStructureDocuments() {
        Date date = new Date();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int year = localDate.getYear();
        int month = localDate.getMonthValue();
        int day = localDate.getDayOfMonth();

        String folderTree;
        folderTree = DOCUMENTS.replace("{yyyy}", String.valueOf(year))
                .replace("{mm}", String.valueOf(month))
                .replace("{dd}", String.valueOf(day));

        return folderTree;
    }

    private String folderStructureMetaData() {
        Date date = new Date();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int year = localDate.getYear();
        int month = localDate.getMonthValue();
        int day = localDate.getDayOfMonth();

        String folderTree;
        folderTree = METADATA.replace("{yyyy}", String.valueOf(year))
                .replace("{mm}", String.valueOf(month))
                .replace("{dd}", String.valueOf(day));

        return folderTree;
    }

    public boolean process(Exchange exchange, AsyncCallback callback) {
        try {
            /*Object body = exchange.getIn().getBody();
            if (body == null || !(body instanceof ArchiveDataDTO)) {
                log.error(String.format("The following failed to be parsed as ArchiveData:\n%s", exchange.getIn().getBody(String.class)));
                throw new IllegalStateException("The message was not ArchiveData, so it cannot be processed.");
            }
            ArchiveDataDTO archiveData = (ArchiveDataDTO) body;
            String archiveBpId = Optional.ofNullable(archiveData.getArchiveBpId()).orElseThrow(() ->
                    new IllegalStateException("Input message must contain ArchiveBPID"));
            String parentBpId = Optional.ofNullable(archiveData.getParentBpId()).orElseThrow(() ->
                    new IllegalStateException("Input message must contain ParentBPID"));
            String clusterId = Optional.ofNullable(archiveData.getClusterId()).orElseThrow(() ->
                    new IllegalStateException("Input message must contain ClusterID"));
            String processDataObjStr = HexZipStringConverter.convert(archiveDao.getProcessDataHexFromLastStepOfBP(archiveBpId));
            String leftBound = "<ProcessData>";
            String rightBound = "</ProcessData>";
            StringExtractorUtil extractorUtil = new StringExtractorUtil(processDataObjStr);
            String processDataXmlStr = extractorUtil.extractPreservingBounds(leftBound, rightBound);*/
            //ProcessData will now be passed directly to the listener, so above code is obsolete
            String processDataXmlStr = exchange.getIn().getBody(String.class);
            StringExtractorUtil extractorUtil = new StringExtractorUtil(processDataXmlStr);
            String docId = UUID.randomUUID().toString();
            String ediDocBlobName = UUID.randomUUID().toString();
            String rawDocBlobName = UUID.randomUUID().toString();
            String processDataDocBlobName = UUID.randomUUID().toString();

            //Create one just in case there is an error, even if there isn't
            String errorDocBlobName = UUID.randomUUID().toString();
            String processId = extractorUtil.dualExtract();
            //.orElse(Optional.ofNullable(extractorUtil.extract("<myWorkflowID>", "</myWorkflowID>")))

            String clusterId = Optional.ofNullable(extractorUtil.extract("<ClusterID>", "</ClusterID>"))
                    .orElseThrow(() -> new IllegalStateException("ProcessData did not contain Cluster ID"));
            Boolean errorFlag = processDataXmlStr.contains("<ERRORHANDLING_ProcessData>");
            String json = "{\"id\": \"" + docId + "\", \"processId\": \"" + processId + "\", \"clusterId\": \"" + clusterId +
                    "\"";
            String keyString = "";
            //String keys = "\"keys\": [ ";
            while (true) {
                String ediKey = extractorUtil.extract("<Correlation_ID>", "</Correlation_ID>");
                if (ediKey == null) {
                    break;
                }
                //keys += "{\"type\": \"EdiKey1\", \"value\": \"" + ediKey + "\"},";
                keyString += "EK1:" + ediKey + ";";
            }
            while (true) {
                String docKey = extractorUtil.extract("<DocKey1>", "</DocKey1>");
                if (docKey == null) {
                    break;
                }
                //keys += "{\"type\": \"KeyVal1\", \"value\": \"" + docKey + "\"},";
                keyString += "KV1:" + docKey + ";";
            }
            //keys = keys.substring(0, keys.length() - 1) + "]";
            //json += keys;
            json += ", \"keyString\": \"" + keyString + "\"";
            String direction = null;
            List<String> fields            = Arrays.asList("SENDER", "RECEIVER", "DOC_TYPE", "DIRECTION", "TRADING_PARTNER", "ISA_SND_ID", "ISA_RCV_ID", "ISA_BAT_CTL_NBR", "GS_SND_ID", "GS_RCV_ID", "GS_GRP_CTL_NBR", "EDI_DAT_DOC_Q", "TransactionSetControlNumber");
            List<String> alternativeFields = Arrays.asList("InterchangeSenderID", "InterchangeReceiverID", "TransactionSetIDCode", "", "InterchangeSenderID", "InterchangeSenderID", "InterchangeReceiverID", "InterchangeControlNumber", "GroupApplicationSenderCode", "GroupApplicationReceiverCode", "GroupControlNumber", "", "");
            List<String> jsonNames         = Arrays.asList("sender", "receiver", "docType", "direction", "tradingPartner", "isaSndId", "isaRcvId", "isaBatCtlNbr", "gsSndId", "gsRcvId", "gsGrpCtlNbr", "docCount", "stCtlNbr");
            for (int i = 0; i < fields.size(); i++) {
                String field = fields.get(i);
                String altField = alternativeFields.get(i);
                String jsonFieldName = jsonNames.get(i);
                String value = extractorUtil.extract("<" + field + ">", "</" + field + ">");
                String altValue = extractorUtil.extract("<" + altField + ">", "</" + altField + ">");
                value = Optional.ofNullable(value).orElse(altValue);
                if (value != null) {
                    json += ", \"" + jsonFieldName + "\": \"" + value + "\"";
                    if (altField.equals("TransactionSetIDCode") && value.trim().equals("997")) {
                        json += ", \"direction\": \"INBOUND\"";
                        direction = "INBOUND";
                    }
                }
                if (jsonFieldName.equals("direction")) {
                    direction = value;
                }
            }
            //If error flag is set to true (i.e., not null and not false)
            if (errorFlag) {
                json += ", \"error\": true";
                String errorMessage = Optional.ofNullable(extractorUtil.extract("<ADV_STATUS>", "</ADV_STATUS>")).orElse("");
                if (errorMessage.equals("")) {
                    errorMessage = Optional.ofNullable(extractorUtil.extract("<SERVICE_NAME>", "</SERVICE_NAME>")).orElse("");
                }
                json += ", \"errorMessage\": \"" + errorMessage + "\", \"resolved\": \"\"";
            }
            String translationReport = Optional.ofNullable(extractorUtil.extract("<stat_rpt>", "</stat_rpt>")).orElse("");
            json += ", \"timestamp\": \"" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\", \"timestampMillis\": " + System.currentTimeMillis();
            String ediTagName = ("inbound".equalsIgnoreCase(direction) ? "PreData" : "PostData");
            String rawTagName = ("inbound".equalsIgnoreCase(direction) ? "PostData" : "PreData");
            String origEdiData = extractorUtil.extract("<" + ediTagName + ">", "</" + ediTagName + ">");
            log.info("original EDI data: " + origEdiData);
            String unescapedEdiData = StringEscapeUtils.unescapeHtml4(origEdiData);
            log.info("unescaped EDI data: " + unescapedEdiData);
            String ediData = Optional.ofNullable(unescapedEdiData)
                    .orElse("");
            String rawData = Optional.ofNullable(StringEscapeUtils.unescapeHtml4(extractorUtil.extract("<" + rawTagName + ">", "</" + rawTagName + ">")))
                    .orElse("");
            if (!ediData.isEmpty()) {
                log.info("uploading ediData with blobName " + ediDocBlobName + " to " + folderStructureDocuments());
                blobStorageUtil.upload(BlobStorageUtil.EDI_CONTAINER_NAME, ediDocBlobName, ediData, folderStructureDocuments());
                json += ", \"ediDocRef\": \"" + ediDocBlobName + "\"";
            }
            if (!rawData.isEmpty()) {
                log.info("uploading rawData with blobName " + rawDocBlobName + " to " + folderStructureDocuments());
                blobStorageUtil.upload(BlobStorageUtil.EDI_CONTAINER_NAME, rawDocBlobName, rawData, folderStructureDocuments());
                json += ", \"rawDocRef\": \"" + rawDocBlobName + "\"";
            }
            log.info("uploading processData with blobName " + processDataDocBlobName + " to " + folderStructureDocuments());
            blobStorageUtil.upload(BlobStorageUtil.EDI_CONTAINER_NAME, processDataDocBlobName, processDataXmlStr, folderStructureDocuments());
            json += ", \"processDataDocRef\": \"" + processDataDocBlobName + "\"";
            if (errorFlag && !translationReport.equals("")) {
                log.info("uploading translationReport with blobName " + errorDocBlobName + " to " + folderStructureDocuments());
                blobStorageUtil.upload(BlobStorageUtil.EDI_CONTAINER_NAME, errorDocBlobName, translationReport, folderStructureDocuments());
                json += ", \"errorDocRef\": \"" + errorDocBlobName + "\"";
            }
            json += "}";
            log.info("about to upload metadata: " + json);
            blobStorageUtil.upload(BlobStorageUtil.EDI_CONTAINER_NAME, docId, json, folderStructureMetaData());
            log.info("about to send message to EventHub: " + json);
            eventHubSendUtil.sendMessage(json);
        } catch (Exception e) {
            exchange.setProperty(Exchange.EXCEPTION_CAUGHT, e);
        }
        callback.done(false);
        return false;
    }

    public void process(Exchange exchange) {
        log.warn("This process is meant to be async, so this message should not be seen.");
    }
}
