package com.logback.logstash.encoder;

import ch.qos.logback.access.spi.IAccessEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;
import static com.logback.logstash.encoder.AccessLogstashLayout.*;


/**
 * Created by ddallemule on 15/10/2014.
 */
@RunWith(MockitoJUnitRunner.class)
public class AccessLogstashLayoutTest {

    @Mock
    IAccessEvent accessEvent;


    @Test
    public void accessDoLayout() throws IOException {

        //given
        when(accessEvent.getMethod()).thenReturn("GET");
        when(accessEvent.getProtocol()).thenReturn("HTTP/1.1");
        when(accessEvent.getRemoteHost()).thenReturn("localhost");
        when(accessEvent.getRequestURI()).thenReturn("test/test");
        when(accessEvent.getStatusCode()).thenReturn(200);
        when(accessEvent.getContentLength()).thenReturn(12399L);
        long currentTimeStamp = System.currentTimeMillis();
        when(accessEvent.getTimeStamp()).thenReturn(currentTimeStamp);

        //when
        AccessLogstashLayout accessLogstashLayout = new AccessLogstashLayout();
        String result = accessLogstashLayout.doLayout(accessEvent);

        //then
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(result);
        assertEquals("GET", jsonNode.get(METHOD).asText());
        assertEquals(1, jsonNode.get(VERSION).asInt());
        assertEquals(200, jsonNode.get(RESPONSE).asInt());
        assertEquals("test/test", jsonNode.get(REQUEST).asText());
        assertEquals("HTTP/1.1", jsonNode.get(PROTOCOL).asText());
        assertEquals("localhost", jsonNode.get(CLINETIP).asText());
        assertEquals(12399L, jsonNode.get(BYTES).asLong());
        String expectedTimeStamp = EventLogstashLayout.ISO_DATETIME_TIME_ZONE_FORMAT_WITH_MILLIS.format(currentTimeStamp);
        assertEquals(expectedTimeStamp, jsonNode.get(TIMESTAMP).asText());
    }


    @Test
    public void accessDoLayoutContentSizeLessThen0() throws IOException {

        //given
        when(accessEvent.getMethod()).thenReturn("GET");
        when(accessEvent.getProtocol()).thenReturn("HTTP/1.1");
        when(accessEvent.getRemoteHost()).thenReturn("localhost");
        when(accessEvent.getRequestURI()).thenReturn("test/test");
        when(accessEvent.getStatusCode()).thenReturn(200);
        when(accessEvent.getContentLength()).thenReturn(-1L);
        long currentTimeStamp = System.currentTimeMillis();
        when(accessEvent.getTimeStamp()).thenReturn(currentTimeStamp);

        //when
        AccessLogstashLayout accessLogstashLayout = new AccessLogstashLayout();
        String result = accessLogstashLayout.doLayout(accessEvent);

        //then
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(result);
        assertEquals("GET", jsonNode.get(METHOD).asText());
        assertEquals(1, jsonNode.get(VERSION).asInt());
        assertEquals(200, jsonNode.get(RESPONSE).asInt());
        assertEquals("test/test", jsonNode.get(REQUEST).asText());
        assertEquals("HTTP/1.1", jsonNode.get(PROTOCOL).asText());
        assertEquals("localhost", jsonNode.get(CLINETIP).asText());
        assertNull(jsonNode.get(BYTES));
        String expectedTimeStamp = EventLogstashLayout.ISO_DATETIME_TIME_ZONE_FORMAT_WITH_MILLIS.format(currentTimeStamp);
        assertEquals(expectedTimeStamp, jsonNode.get(TIMESTAMP).asText());
    }
}
