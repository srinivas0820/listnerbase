package com.jbhunt.edi.sterlingarchive.dao;

import com.jbhunt.edi.general.dto.ArchiveMessageHeaderUtil;
import enums.HeaderType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class ArchiveDAO {
    private static final String APPLICATION_NAME = "EDIRCHV";

    private final NamedParameterJdbcTemplate siJdbcTemplate;

    public ArchiveDAO(final NamedParameterJdbcTemplate siJdbcTemplate){
        this.siJdbcTemplate = siJdbcTemplate;
    }

    public String getProcessDataHexFromLastStepOfBP(String bpid) {
        log.info(bpid);
        String sql = "SELECT DATA_OBJECT FROM dbo.TRANS_DATA TD INNER JOIN (SELECT TOP(1) * FROM dbo.WORKFLOW_CONTEXT " +
                "WHERE WORKFLOW_ID = :bpid ORDER BY STEP_ID DESC) AS WC ON TD.DATA_ID = WC.CONTENT";
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("bpid", bpid);
        List<String> result = siJdbcTemplate.query(sql, queryParams, new RowMapper<String>(){
            public String mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                return rs.getString(1);
            }
        });
        if(result.isEmpty()) {
            return null;
        }
        log.info(result.get(0));
        return result.get(0);
    }

    /*private static void insertKey(NamedParameterJdbcTemplate template, BigInteger ediDocHdrI, KeyDTO key, Integer controlNumber) {
        String sql = "INSERT INTO JBH.EDI_DOC_IDX (EDI_DOC_HDR_I, IDX_TYP,\n" +
                "IDX_VAL, CRT_S, CRT_UID, CRT_PGM_C, LST_UPD_S, LST_UPD_UID,\n" +
                "LST_UPD_PGM_C, XAC_SET_CTL_NBR)\n" +
                "VALUES (:ediDocHdrI, :keyType, :keyValue, CURRENT_TIMESTAMP, :appName,\n" +
                ":appName, CURRENT_TIMESTAMP, :appName, :appName, :controlNumber)";
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("ediDocHdrI", String.valueOf(ediDocHdrI));
        queryParams.put("keyType", key.getType());
        queryParams.put("keyValue", key.getValue());
        queryParams.put("appName", APPLICATION_NAME);
        queryParams.put("controlNumber", controlNumber);

        template.update(sql, queryParams);
    }

    public void insertKeyToBothTables(BigInteger ediDocHdrIOnPrem, BigInteger ediDocHdrIAzure, KeyDTO key, Integer controlNumber) {
        insertKey(ediJdbcTemplate, ediDocHdrIOnPrem, key, controlNumber);
        insertKey(ediAzureJdbcTemplate, ediDocHdrIAzure, key, controlNumber);
    }

    public String getProcessIdFromUniqueId(String uniqueId) {
        String sql = "INSERT INTO JBH.EDI_ARCV_LKUP (DOC_UUID, CRT_S, CRT_UID, CRT_PGM_C, LST_UPD_S, LST_UPD_UID, " +
                "LST_UPD_PGM_C) VALUES (:uniqueId, CURRENT_TIMESTAMP, :appName, :appName, CURRENT_TIMESTAMP, " +
                ":appName, :appName)";
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("uniqueId", uniqueId);
        queryParams.put("appName",APPLICATION_NAME);
        try {
            ediJdbcTemplate.update(sql, queryParams);
        } catch(DuplicateKeyException e) {
            //do nothing; whether the unique id was inserted from this insert query or another one, the next step is to
            //select to find the process id to use
        }
        sql = "SELECT EDI_ARCV_LKUP_I FROM JBH.EDI_ARCV_LKUP WHERE DOC_UUID = :uniqueId";
        List<String> result = ediJdbcTemplate.query(sql, queryParams, new RowMapper<String>(){
            public String mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                return rs.getString(1);
            }
        });
        if(result.isEmpty()) {
            throw new IllegalStateException("No processId could be found for the uniqueId \"" + uniqueId + "\".");
        }
        return result.get(0);
    }

    private static String getEdiDocHdrIFromProcessId(NamedParameterJdbcTemplate template, String processId, Object createdByUser) {
        log.info("Looking for EDI_DOC_HDR_I...");
        String sql = "SELECT EDI_DOC_HDR_I FROM JBH.EDI_DOC_HDR WHERE PRS_I = :processId " +
                "AND DOC_TYP not in ('ProcessData', 'RAW') AND (:userid is null OR CRT_UID = :userid) ORDER BY CRT_S DESC";
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("processId", processId);
        queryParams.put("userid", createdByUser);
        List<String> result = template.query(sql, queryParams, new RowMapper<String>(){
            public String mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                return rs.getString(1);
            }
        });
        if(result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

    //returns null if it can't find BOTH the PRS_I in on prem and azure DB instances
    public String[] getEdiDocHdrIFromProcessIdOnPremAndAzure(String processId, Object createdByUser) {
        String[] result = new String[2];
        result[0] = getEdiDocHdrIFromProcessId(ediJdbcTemplate, processId, createdByUser);
        result[1] = getEdiDocHdrIFromProcessId(ediAzureJdbcTemplate, processId, createdByUser);
        if(result[0] == null || result[1] == null) {
            return null;
        }
        return result;
    }

    private static void ediDocHdrInsert(NamedParameterJdbcTemplate template, String ediData, String processId, ArchiveMessageHeaderUtil headerUtil) {
        String sql = "INSERT INTO JBH.EDI_DOC_HDR (SND_ID, RCV_ID, DOC_TYP, DOC_DIR_C, TPR_NBR, PRS_I, FIL_NM, EDI_DAT, " +
                "CRT_S, CRT_UID, CRT_PGM_C, LST_UPD_S, LST_UPD_UID, LST_UPD_PGM_C, PRS_STT, ISA_SND_ID, ISA_RCV_ID, " +
                "ISA_BAT_CTL_NBR, GS_SND_ID, GS_RCV_ID, GS_GRP_CTL_NBR, EDI_DAT_DOC_Q) VALUES (:SND_ID, :RCV_ID, " +
                ":DOC_TYP, :DOC_DIR_C, :TPR_NBR, :processId, '', :EDI_DAT, CURRENT_TIMESTAMP, :userid, :program, " +
                "CURRENT_TIMESTAMP, :userid, :program, :PRS_STT, :ISA_SND_ID, :ISA_RCV_ID, :ISA_BAT_CTL_NBR, :GS_SND_ID, " +
                ":GS_RCV_ID, :GS_GRP_CTL_NBR, :EDI_DAT_DOC_Q)";
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("SND_ID", headerUtil.getHeader(HeaderType.SENDER));
        queryParams.put("RCV_ID", headerUtil.getHeader(HeaderType.RECEIVER));
        queryParams.put("DOC_TYP", headerUtil.getHeader(HeaderType.DOC_TYPE));
        queryParams.put("DOC_DIR_C", headerUtil.getHeader(HeaderType.DIRECTION));
        queryParams.put("TPR_NBR", headerUtil.getHeader(HeaderType.TRADING_PARTNER));
        queryParams.put("processId", processId);
        queryParams.put("EDI_DAT", ediData);
        queryParams.put("PRS_STT", headerUtil.getHeader(HeaderType.PROCESS_STATUS));
        queryParams.put("ISA_SND_ID", headerUtil.getHeader(HeaderType.ISA_SND_ID));
        queryParams.put("ISA_RCV_ID", headerUtil.getHeader(HeaderType.ISA_RCV_ID));
        queryParams.put("ISA_BAT_CTL_NBR", headerUtil.getHeader(HeaderType.ISA_BAT_CTL_NBR));
        queryParams.put("GS_SND_ID", headerUtil.getHeader(HeaderType.GS_SND_ID));
        queryParams.put("GS_RCV_ID", headerUtil.getHeader(HeaderType.GS_RCV_ID));
        queryParams.put("GS_GRP_CTL_NBR", headerUtil.getHeader(HeaderType.GS_GRP_CTL_NBR));
        queryParams.put("EDI_DAT_DOC_Q", headerUtil.getHeader(HeaderType.EDI_DAT_DOC_Q));
        queryParams.put("userid", Optional.ofNullable(headerUtil.getHeader(HeaderType.CREATED_BY_USER)).orElse(APPLICATION_NAME));
        queryParams.put("program", Optional.ofNullable(headerUtil.getHeader(HeaderType.CREATED_BY_PROGRAM)).orElse(APPLICATION_NAME));
        template.update(sql, queryParams);
    }

    public void ediDocHdrInsertToBothTables(String ediData, String processId, ArchiveMessageHeaderUtil headerUtil) {
        ediDocHdrInsert(ediJdbcTemplate, ediData, processId, headerUtil);
        ediDocHdrInsert(ediAzureJdbcTemplate, ediData, processId, headerUtil);
    }*/
}
