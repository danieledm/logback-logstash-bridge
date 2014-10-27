package com.logback.logstash.encoder;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.LayoutBase;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import org.apache.commons.lang.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;

/**
 * populate the access log json
 * Created by ddallemule on 14/10/2014.
 */
public class AccessLogstashLayout extends LayoutBase<IAccessEvent> {
    public static final String TIMESTAMP = "@timestamp";
    public static final String VERSION = "@version";
    public static final String CHANNEL = "channel";
    public static final String HOST = "host";
    public static final String CLINETIP = "clientip";
    public static final String AUTH = "auth";
    public static final String BYTES = "bytes";
    public static final String PROTOCOL = "protocol";
    public static final String REQUEST = "request";
    public static final String METHOD = "method";
    public static final String RESPONSE = "response";

    private static final JsonFactory FACTORY = new MappingJsonFactory().enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
    public static final FastDateFormat ISO_DATETIME_TIME_ZONE_FORMAT_WITH_MILLIS = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
    private static final String NEW_LINE = System.getProperty("line.separator");
    private final Logger log = LoggerFactory.getLogger(EventLogstashLayout.class);
    private String channel = "";

    public String doLayout(IAccessEvent event) {
        StringWriter writer = new StringWriter();
        JsonGenerator jsonGenerator = null;
        try {
            jsonGenerator = FACTORY.createGenerator(writer);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField(TIMESTAMP, ISO_DATETIME_TIME_ZONE_FORMAT_WITH_MILLIS.format(event.getTimeStamp()));
            jsonGenerator.writeNumberField(VERSION, 1);
            if(getContext() != null) {
                jsonGenerator.writeStringField(HOST, getContext().getProperty("HOSTNAME"));
            }
            jsonGenerator.writeStringField(CLINETIP, event.getRemoteHost());
            jsonGenerator.writeStringField(AUTH, event.getRemoteUser());
            long contentLength = event.getContentLength();
            if(contentLength >= 0){
                jsonGenerator.writeNumberField(BYTES, contentLength);
            }
            jsonGenerator.writeStringField(PROTOCOL, event.getProtocol());
            jsonGenerator.writeStringField(REQUEST, event.getRequestURI());
            jsonGenerator.writeStringField(METHOD, event.getMethod());
            jsonGenerator.writeNumberField(RESPONSE, event.getStatusCode());
            jsonGenerator.writeStringField(CHANNEL, channel);
            jsonGenerator.writeEndObject();
            jsonGenerator.close();
        } catch (IOException e) {
            //notify the problem and carry on
            log.error("Error building JSON for Logstash: " + e.getMessage());
        }
        writer.append(NEW_LINE);
        return writer.toString();
    }

    public void setChannel(String channel){
        this.channel = channel;
    }
}
