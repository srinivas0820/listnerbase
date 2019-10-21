
package com.jbhunt.edi.sterlingarchive.processor;

import com.jbhunt.edi.sterlingarchive.dao.ArchiveDAO;
import com.jbhunt.edi.sterlingarchive.dto.*;
import com.jbhunt.edi.sterlingarchive.utils.BlobStorageUtil;
import com.jbhunt.edi.sterlingarchive.utils.EventHubSendUtil;
import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import java.math.BigInteger;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class DocumentProcessorTest {

    @InjectMocks
    private DocumentProcessor documentProcessor;

    @Mock
    private ArchiveDAO archiveDAO;

    @Mock
    private BlobStorageUtil blobStorageUtil;

    @Mock
    private EventHubSendUtil eventHubSendUtil;

    @Mock
    private Exchange exchange;

    @Mock
    private Message messageIn;

    @Mock
    private Message messageOut;

    @Before
    public void before() {
        documentProcessor = new DocumentProcessor(archiveDAO, blobStorageUtil, eventHubSendUtil);
    }

    @Test
    public void documentProcessorTestAsync() throws Exception{
        when(exchange.getIn()).thenReturn(messageIn);
        when(messageIn.getBody(String.class)).thenReturn(getXmlString());
        doNothing().when(blobStorageUtil).upload(anyString(), anyString(), anyString(), anyString());
        documentProcessor.process(exchange, new AsyncCallback() {
            @Override
            public void done(boolean b) { }
        });
        verify(exchange, times(0))
                .setProperty(eq(Exchange.EXCEPTION_CAUGHT), anyObject());
    }

    @Test
    public void documentProcessorTestSync() {
        documentProcessor.process(exchange);
    }

    public String getXmlString(){

        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><ProcessData><MapResult><Row><XLT_MAP_I>31415</XLT_MAP_I>\n" +
                "<RECEIVER>HJBI</RECEIVER>\n" +
                "<SENDER>6120930002</SENDER>\n" +
                "<DOC_TYPE>204</DOC_TYPE>\n" +
                "<MAP_NAME>JBH_TRACTORSUPPLY_204_RECV_4010</MAP_NAME>\n" +
                "<TRANS_ENDPOINT_MBX>/APP/MQ/INBOUND</TRANS_ENDPOINT_MBX>\n" +
                "<DIRECTION>INBOUND</DIRECTION>\n" +
                "<DOC_EXT_MAP/>\n" +
                "<MQ_IP/>\n" +
                "<MQ_PORT/>\n" +
                "<MQ_QUEUE/>\n" +
                "<MQ_CHANNEL/>\n" +
                "<SKIP_HEADER_REC/>\n" +
                "<TRANSLATION_INPUT_TYPE/>\n" +
                "<QueuePriority>9</QueuePriority>\n" +
                "<FileFormatType/>\n" +
                "<Correlation_ID>MJW1094666</Correlation_ID>\n" +
                "</Row>\n" +
                "<Row><Correlation_ID>MJW1094666</Correlation_ID>\n" +
                "</Row>\n" +
                "</MapResult>\n" +
                "<PreDataFile SCIObjectID='JVTB2B01102:node2:16c4d56434f:49220859'/>\n" +
                "<PROCESS_ID>18091580</PROCESS_ID>\n" +
                "<myWorkflowID>18091585</myWorkflowID>\n" +
                "<ParentBP>JBH_BP_GENERIC_TRANSLATION_IB</ParentBP>\n" +
                "<ClusterID>A</ClusterID>\n" +
                "<ProcessDataNodeName>PostData</ProcessDataNodeName>\n" +
                "<PreData>ISA*00*          *00*          *08*6120930002     *02*HJBI           *190611*1451*U*00401*000000098*0*P*&gt;~\n" +
                "GS*SM*6120930002*HJBI*20190611*14512326*27*X*004010~\n" +
                "ST*204*0001~\n" +
                "B2**HJBI**MJW1094666**CC*I~\n" +
                "B2A*00~\n" +
                "G62*64*20190612*1*0851~\n" +
                "MS3*HJBI*B**I~\n" +
                "N1*SH*TRACTOR SUPPLY CO.~\n" +
                "N3*5401 VIRGINIA WAY~\n" +
                "N4*BRENTWOOD*TN*37207*US~\n" +
                "N7**NA*********CN~\n" +
                "S5*1*LD*13652.01*L*13200*EA*3250.01*E~\n" +
                "L11*SH01094735*SI~\n" +
                "L11*50000000003499592*BM~\n" +
                "G62*69*20190621*I*0000*LT~\n" +
                "PLD*26~\n" +
                "N1*SF*MANNA PRO MO DC*93*503359~\n" +
                "N3*6600 EXECUTIVE DRIVE~\n" +
                "N4*KANSAS CITY*MO*64120*US~\n" +
                "G61*SH*First Last*TE*800-690-9908~\n" +
                "OID*1023316420_00865528*1023316420~\n" +
                "L5***1198175*Z~\n" +
                "S5*2*UL*13652.01*L*13200*EA*3250.01*E~\n" +
                "L11*SH01094735*SI~\n" +
                "L11*50000000003499593*BM~\n" +
                "G62*68*20190628*G*0000*LT~\n" +
                "PLD*26~\n" +
                "N1*CN*120 MACON DC*93*0120~\n" +
                "N3*151 TRACTOR DRIVE~\n" +
                "N4*MACON*GA*31216*US~\n" +
                "OID*1023316420_00865528*1023316420~\n" +
                "L5***1198175*Z~\n" +
                "L3*13652.01*L*******3250.01*E~\n" +
                "SE*32*0001~\n" +
                "GE*1*27~\n" +
                "IEA*1*000000098~</PreData>\n" +
                "<PrimaryDocument SCIObjectID='JVTB2B01102:node2:16c4d56434f:49220859'/>\n" +
                "<PostData>204                                                                                                 \n" +
                "I000000000000986120930002                         HJBI                               1906111451  00401000001000001000000003300000000                            00000000000027SM    6120930002                         HJBI                               1906111451  004010                  000001              00000000000001204               0000000032MJW1094666                         00000000000000000000TRACSUPPI                                                                       \n" +
                "CTRACSUPPI                          LOADTENDERRECV1             PY N                                                                                                                                                                                                                                        \n" +
                "DPENDINGORDER    MJW1094666                    ICCL 000000000013652                  000000000000000                  2019-06-2100.00.002019-06-2100.00.002019-06-2800.00.002019-06-2800.00.002019-06-1208.51.00    0000HJBI1KF 002                 \n" +
                "DPARTYINFORMATION     BT                                                                                  TRACTOR SUPPLY CO.                                                                                       5401 VIRGINIA WAY                                                                                                                           BRENTWOOD          TN000037207US   BT                       \n" +
                "DAPPOINTMENTS       642019-06-12108.51.00\n" +
                "DROUTEINFORMATION   HJBIB X \n" +
                "DEQUIPMENT       000CN          000000000                     \n" +
                "DSTOPOFFDETAIL   001LDL000000000013652000000000013200EA000000000003250E                                                                                \n" +
                "DSTOPOFFDETAIL   002ULL000000000013652000000000013200EA000000000003250E                                                                                \n" +
                "DREFERENCENUMBERD001  IKSH01094735                       \n" +
                "DREFERENCENUMBERD001  BM50000000003499592                \n" +
                "DAPPOINTMENTSD   001692019-06-21I00.00.00     \n" +
                "DPARTYINFOD      001  SH93503359                                                                          MANNA PRO MO DC                                                                                          6600 EXECUTIVE DRIVE                                                                                                                        KANSAS CITY        MO64120    US   SH                       \n" +
                "DCONTACTINFOD    001  SHFirst Last                         TE800-690-9908         \n" +
                "DSHIPMENTPODETAIL0011023316420_00865528                                   000000000 000000000000000\n" +
                "DLINEITEMINFO                                                      Z1198175                                             \n" +
                "DREFERENCENUMBER2     PO1023316420                    \n" +
                "DSPECIALSERVICES              26       \n" +
                "DREFERENCENUMBERD002  IKSH01094735                       \n" +
                "DREFERENCENUMBERD002  BM50000000003499593                \n" +
                "DAPPOINTMENTSD   002682019-06-28G00.00.00     \n" +
                "DPARTYINFOD      002  CN930120                                                                            120 MACON DC                                                                                             151 TRACTOR DRIVE                                                                                                                           MACON              GA31216    US   CN                       \n" +
                "DSHIPMENTPODETAIL0021023316420_00865528                                   000000000 000000000000000\n" +
                "DLINEITEMINFO                                                      Z1198175                                             \n" +
                "DREFERENCENUMBER2     PO1023316420                    \n" +
                "DSPECIALSERVICES              26       \n" +
                "END TSET</PostData>\n" +
                "</ProcessData>";
    }
}
