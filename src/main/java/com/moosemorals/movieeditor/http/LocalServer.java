/*
 * The MIT License
 *
 * Copyright 2016 Osric Wilkinson <osric@fluffypeople.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.moosemorals.movieeditor.http;

import com.moosemorals.movieeditor.ProgressMonitor;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.http.ExceptionLogger;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Osric Wilkinson <osric@fluffypeople.com>
 */
public class LocalServer implements Runnable {

    private final Logger log = LoggerFactory.getLogger(LocalServer.class);

    private final HttpServer server;

    public LocalServer(int port, ProgressMonitor monitor) {

        HttpProcessor httpproc = HttpProcessorBuilder.create()
                .add(new ResponseDate())
                .add(new ResponseServer("Movie Splitter"))
                .add(new ResponseContent())
                .add(new ResponseConnControl())
                .build();

        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(1500)
                .setTcpNoDelay(true)
                .build();

        server = ServerBootstrap.bootstrap()
                .setListenerPort(port)
                .setHttpProcessor(httpproc)
                .setSocketConfig(socketConfig)
                .setExceptionLogger(new ExceptionLoggerImpl())
                .setHandlerMapper(new RequestMapper(monitor))
                .create();

    }

    public void start() {
        Thread t = new Thread(this, "WebServer");
        t.setDaemon(true);
        t.start();
    }

    public void stop() {
        log.debug("Stop request");
        server.shutdown(1, TimeUnit.SECONDS);
        log.debug("Stop request completed");
    }

    @Override
    public void run() {
        try {
            log.debug("Server starting");
            server.start();
        } catch (IOException ex) {
            log.error("Server shutdown", ex);
        }
    }

    private static class ExceptionLoggerImpl implements ExceptionLogger {

        private final Logger log = LoggerFactory.getLogger(ExceptionLoggerImpl.class);

        @Override
        public void log(Exception ex) {
            if (ex instanceof java.net.SocketTimeoutException) {
                return;
            }
            log.error("Internal HTTP error", ex);
        }
    }
}
