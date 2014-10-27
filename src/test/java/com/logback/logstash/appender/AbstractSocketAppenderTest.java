package com.logback.logstash.appender;
/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

import ch.qos.logback.core.BasicStatusManager;
import ch.qos.logback.core.encoder.EncoderBase;
import ch.qos.logback.core.util.StatusPrinter;
import com.logback.logstash.appender.mock.MockContext;
import org.junit.*;

import ch.qos.logback.core.spi.PreSerializationTransformer;

import javax.net.ServerSocketFactory;

/**
 * Unit tests for {@link AbstractSocketAppender}.
 *
 * @author Carl Harris
 */
public class AbstractSocketAppenderTest {

    private static final int DELAY = 10000;

    private ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    private MockContext mockContext = new MockContext(executorService);
    private InstrumentedSocketAppender instrumentedAppender = new InstrumentedSocketAppender();


    @Before
    public void setUp() throws Exception {
        instrumentedAppender.setContext(mockContext);
    }

    @After
    public void tearDown() throws Exception {
        instrumentedAppender.stop();
        assertFalse(instrumentedAppender.isStarted());
        executorService.shutdownNow();
        assertTrue(executorService.awaitTermination(DELAY, TimeUnit.MILLISECONDS));
    }

    @Test
    public void appenderShouldFailToStartWithoutValidPort() throws Exception {
        instrumentedAppender.setPort(-1);
        instrumentedAppender.setRemoteHost("localhost");
        instrumentedAppender.setQueueSize(0);
        instrumentedAppender.start();
        assertFalse(instrumentedAppender.isStarted());
        assertTrue(mockContext.getLastStatus().getMessage().contains("port"));
    }

    @Test
    public void appenderShouldFailToStartWithoutValidRemoteHost() throws Exception {
        instrumentedAppender.setPort(1);
        instrumentedAppender.setRemoteHost(null);
        instrumentedAppender.setQueueSize(0);
        instrumentedAppender.start();
        assertFalse(instrumentedAppender.isStarted());
        assertTrue(mockContext.getLastStatus().getMessage().contains("remote host"));
    }

    @Test
    public void appenderShouldFailToStartWithNegativeQueueSize() throws Exception {
        instrumentedAppender.setPort(1);
        instrumentedAppender.setRemoteHost("localhost");
        instrumentedAppender.setQueueSize(-1);
        instrumentedAppender.start();
        assertFalse(instrumentedAppender.isStarted());
        assertTrue(mockContext.getLastStatus().getMessage().contains("Queue"));
    }

    @Test
    public void appenderShouldFailToStartWithUnresolvableRemoteHost() throws Exception {
        instrumentedAppender.setPort(1);
        instrumentedAppender.setRemoteHost("NOT.A.VALID.REMOTE.HOST.NAME");
        instrumentedAppender.setQueueSize(0);
        instrumentedAppender.start();
        assertFalse(instrumentedAppender.isStarted());
        assertTrue(mockContext.getLastStatus().getMessage().contains("unknown host"));
    }

    @Test
    public void appenderShouldStartWithValidParameters() throws Exception {
        instrumentedAppender.setPort(1);
        instrumentedAppender.setRemoteHost("localhost");
        instrumentedAppender.setQueueSize(1);
        instrumentedAppender.start();
        assertTrue(instrumentedAppender.isStarted());
        assertEquals(1, instrumentedAppender.getQueueSize());
    }

    // this test takes 1 second and is deemed too long
//    @Ignore
    @Test(timeout = 2000)
    public void appenderShouldCleanupTasksWhenStopped() throws Exception {
        mockContext.setStatusManager(new BasicStatusManager());
        instrumentedAppender.setPort(1);
        instrumentedAppender.setRemoteHost("localhost");
        instrumentedAppender.setQueueSize(1);
        instrumentedAppender.start();
        assertTrue(instrumentedAppender.isStarted());

        waitForActiveCountToEqual(executorService, 2);
        instrumentedAppender.stop();
        waitForActiveCountToEqual(executorService, 0);
        StatusPrinter.print(mockContext);
        assertEquals(0, executorService.getActiveCount());

    }

    private void waitForActiveCountToEqual(ThreadPoolExecutor executorService, int i) {
        while (executorService.getActiveCount() != i) {
            try {
                Thread.yield();
                Thread.sleep(1);
                System.out.print(".");
            } catch (InterruptedException e) {
            }
        }
    }


    @Test
    public void testAppendWhenNotStarted() throws Exception {
        instrumentedAppender.setRemoteHost("localhost");
        instrumentedAppender.start();
        instrumentedAppender.stop();

        // make sure the appender task has stopped
        executorService.shutdownNow();
        assertTrue(executorService.awaitTermination(DELAY, TimeUnit.MILLISECONDS));

        instrumentedAppender.append("some event");
        assertEquals(0, instrumentedAppender.queueCopy().size());
    }

//    @Test(timeout = 5000
    @Test
    public void testAppendSingleEvent() throws Exception {
        instrumentedAppender.setPort(1);
        instrumentedAppender.setRemoteHost("localhost");
        instrumentedAppender.setQueueSize(1);
        instrumentedAppender.start();

        assertTrue(instrumentedAppender.isStarted());
        instrumentedAppender.append("some event");
        assertEquals(1, instrumentedAppender.getQueueSize());
    }

    @Test
    public void testAppendEvent() throws Exception {
        instrumentedAppender.setPort(1);
        instrumentedAppender.setRemoteHost("localhost");
        instrumentedAppender.setQueueSize(1);
        instrumentedAppender.start();

        // stop the appender task, but don't stop the appender
        executorService.shutdownNow();
        assertTrue(executorService.awaitTermination(DELAY, TimeUnit.MILLISECONDS));

        instrumentedAppender.append("some event");
        assertEquals("some event", instrumentedAppender.queueCopy().poll());
    }

//    @Test(timeout = 5000)
    @Test
    public void testDispatchEvent() throws Exception {
        //given
        ServerSocket serverSocket = createServerSocket();
        instrumentedAppender.setRemoteHost(serverSocket.getInetAddress().getHostAddress());
        instrumentedAppender.setPort(serverSocket.getLocalPort());
        instrumentedAppender.setQueueSize(1);
        instrumentedAppender.setEncoder(new EncoderBase<String>() {
            OutputStream outputStream;

            public void init(OutputStream outputStream1){
                this.outputStream = outputStream1;
            }

            @Override
            public void doEncode(String s) throws IOException {
                assertEquals("some event", s);
             }

            @Override
            public void close() throws IOException {
            }
        });
        instrumentedAppender.start();
        Thread thread = new Thread(instrumentedAppender);
        thread.start();
        Socket appenderSocket = serverSocket.accept();

        //when
        instrumentedAppender.append("some event");

        //then
        final int shortDelay = 100;
        for (int i = 0, retries = DELAY / shortDelay;
             !(instrumentedAppender.queueCopy().size() == 0) && i < retries;
             i++) {
            Thread.sleep(shortDelay);
        }
        assertTrue(instrumentedAppender.queueCopy().size() == 0);

        thread.interrupt();
        appenderSocket.close();

    }

    private static class InstrumentedSocketAppender extends AbstractSocketAppender<String> {

        CountDownLatch latch = new  CountDownLatch(1);
        @Override
        protected void postProcessEvent(String event) {
        }

        @Override
        protected PreSerializationTransformer<String> getPST() {
            return new PreSerializationTransformer<String>() {
                public Serializable transform(String event) {
                    return event;
                }
            };
        }

        @Override
        protected void signalEntryInRunMethod() {
            latch.countDown();
        }

    }


    //couple of utility
    public static ServerSocket createServerSocket() throws IOException {
        return createServerSocket(ServerSocketFactory.getDefault());
    }

    /**
     * Creates a new {@link ServerSocket} bound to a random unused port.
     * @param socketFactory socket factory that will be used to create the
     *    socket
     * @return socket
     * @throws IOException
     */
    public static ServerSocket createServerSocket(
            ServerSocketFactory socketFactory) throws IOException {
        ServerSocket socket = null;
        int retries = 10;
        while (retries-- > 0 && socket == null) {
            int port = (int)((65536 - 1024) * Math.random()) + 1024;
            try {
                socket = socketFactory.createServerSocket(port);
            }
            catch (BindException ex) {
                // try again with different port
            }
        }
        if (socket == null) {
            throw new BindException("cannot find an unused port to bind");
        }
        return socket;
    }
}




