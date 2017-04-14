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
package com.moosemorals.movieeditor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;
import org.apache.http.client.utils.URIBuilder;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes a list of start/end timing pairs, and splits the file.
 *
 * @author Osric Wilkinson <osric@fluffypeople.com>
 */
public class Splitter {

    private final static Logger log = LoggerFactory.getLogger(Splitter.class);

    public static void split(Config config, List<TimingPair> pairs) throws IOException {
        File temp = File.createTempFile("scratch", "tmp");

        File finalResult = new File(config.getOutputFile());

        if (finalResult.exists()) {
            finalResult.delete();
        }

        Duration length = Duration.ZERO;

        try (PrintWriter out = new PrintWriter(new FileWriter(temp))) {
            for (TimingPair pair : pairs) {
                File partFile = File.createTempFile("part-", ".ts", config.getTempDir());
                partFile.deleteOnExit();

                String[] cmd = {
                    "/usr/bin/ffmpeg",
                    "-hide_banner",
                    "-progress", buildTargetURI(config, "start", TimingPair.format(pair.getStart()), "duration", TimingPair.format(pair.getDuration())),
                    "-loglevel", "0",
                    "-y",
                    "-ss", TimingPair.format(pair.getStart()),
                    "-t", TimingPair.format(pair.getDuration()),
                    "-i", config.getInputFile(),
                    "-f", "mpegts",
                    "-c", "copy",
                    "-avoid_negative_ts", "1",
                    "-copyts",
                    partFile.getAbsolutePath()
                };

                int result = runProcess(cmd);
                log.info("Process result {}", result);
                if (result != 0) {
                    throw new IOException("ffmpeg error, no details. Sorry.");
                }
                out.printf("file '%s'\n", partFile.getAbsolutePath());

                length = length.plus(pair.getDuration());
            }
            out.flush();
        }

        String[] cmd = {
            "/usr/bin/ffmpeg",
            "-hide_banner",
            "-progress", buildTargetURI(config, "start", TimingPair.format(Duration.ZERO), "duration", TimingPair.format(length)),
            "-loglevel", "0",
            "-f", "concat",
            "-safe", "0",
            "-i", temp.getAbsolutePath(),
            "-c", "copy",
            finalResult.getAbsolutePath()
        };

        int result = runProcess(cmd);
        log.debug("Process result {}", result);
    }

    private static int runProcess(String... cmd) throws IOException {
        try {
            log.debug("Running  {}", join(" ", cmd));
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);

            Process p = pb.start();

            Scanner scan = new Scanner(p.getInputStream());

            while (scan.hasNextLine()) {
                log.debug("Process output: {}", scan.nextLine());
            }

            return p.waitFor();

        } catch (InterruptedException ex) {
            log.warn("Interrupted while running command");
            return -1;
        }
    }

    private static String join(String sep, String... parts) {
        if (parts == null) {
            return null;
        } else if (parts.length == 0) {
            return "";
        } else if (parts.length == 1) {
            return " ";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < parts.length; i += 1) {
            if (i != 0) {
                result.append(sep);
            }
            result.append(parts[i]);
        }

        return result.toString();
    }

    private static String buildTargetURI(Config config, String... argPairs) {

        URIBuilder builder = new URIBuilder();
        builder
                .setHost("localhost")
                .setPort(config.getPort())
                .setScheme("http");

        if (argPairs != null) {
            for (int i = 0; i < argPairs.length; i += 2) {
                builder.addParameter(argPairs[i], argPairs[i + 1]);
            }
        }

        return builder.toString();
    }
}
