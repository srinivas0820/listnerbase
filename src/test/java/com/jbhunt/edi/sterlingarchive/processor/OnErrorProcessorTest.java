package com.jbhunt.edi.sterlingarchive.processor;

import org.apache.camel.Exchange;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class OnErrorProcessorTest {
    private OnErrorProcessor onErrorProcessor;

    @Mock
    private Exchange exchange;


    @Before
    public void setup() {
        onErrorProcessor = new OnErrorProcessor();
        when(exchange.getProperty(anyString())).thenReturn(new Exception());
    }

    @Test
    public void onErrorProcessorTest() throws Exception {
        onErrorProcessor.process(exchange);
    }
}
