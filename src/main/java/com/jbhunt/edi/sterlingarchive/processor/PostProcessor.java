package com.jbhunt.edi.sterlingarchive.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RefreshScope
public class PostProcessor implements Processor {
    public void process(Exchange exchange) throws Exception{
        Object excObj = exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
        if(excObj != null) {
            throw (Exception) excObj;
        }
    }
}
