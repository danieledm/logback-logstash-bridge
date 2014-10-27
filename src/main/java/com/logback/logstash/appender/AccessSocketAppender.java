package com.logback.logstash.appender;

import ch.qos.logback.access.net.AccessEventPreSerializationTransformer;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.spi.PreSerializationTransformer;

/**
 * Created by ddallemule on 09/10/2014.
 */
public class AccessSocketAppender extends AbstractSocketAppender<IAccessEvent>{
    private static final PreSerializationTransformer<IAccessEvent> PST = new AccessEventPreSerializationTransformer();


    @Override
    protected void postProcessEvent(IAccessEvent event) {
    }

    @Override
    protected PreSerializationTransformer<IAccessEvent> getPST() {
        return PST;
    }
}
