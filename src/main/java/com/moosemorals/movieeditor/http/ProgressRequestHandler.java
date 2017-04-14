/*
 * The MIT License
 *
 * Copyright 2017 Osric Wilkinson <osric@fluffypeople.com>.
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

import com.moosemorals.movieeditor.TimingPair;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Osric Wilkinson <osric@fluffypeople.com>
 */
public class ProgressRequestHandler implements HttpRequestHandler {

    private final Logger log = LoggerFactory.getLogger(ProgressRequestHandler.class);

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {

        if (request instanceof HttpEntityEnclosingRequest) {

            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();

            TimingPair timing = getTimingPairFromUri(request.getRequestLine().getUri());

            BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));

            long out_time_start = 0;

            String line;
            while ((line = in.readLine()) != null) {
                String[] parts = line.split("=");
                String key = parts[0];
                String value = parts[1];

                switch (key) {
                    case "progress":
                        if (value.equals("end")) {
                            log.debug("done");
                        }
                        break;
                    case "out_time_ms":
                        if (out_time_start == 0) {
                            out_time_start = Long.parseLong(value);
                        } else {
                            long done_ms = (Long.parseLong(value) - out_time_start) / 1000;

                            log.debug("Percent done: {}%", ((float) done_ms / timing.getDuration().getMillis()) * 100);
                        }

                        break;
                }
            }

            EntityUtils.consumeQuietly(entity);

        } else {
            log.warn("Can't read input from {}", request.getRequestLine());
        }
    }

    private static TimingPair getTimingPairFromUri(String uri) throws IOException {

        try {
            String start = null, duration = null;
            for (NameValuePair pair : new URIBuilder(uri).getQueryParams()) {
                switch (pair.getName()) {
                    case "start":
                        start = pair.getValue();
                        break;
                    case "duration":
                        duration = pair.getValue();
                        break;
                }
            }
            if (start != null && duration != null) {
                return new TimingPair(start, duration);
            } else {
                throw new URISyntaxException(uri, "Missing start or end parameters");
            }
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
    }
}
