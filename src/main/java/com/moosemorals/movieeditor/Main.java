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

import com.moosemorals.movieeditor.http.LocalServer;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 *
 * @author Osric Wilkinson <osric@fluffypeople.com>
 */
public class Main {

    public static void main(String[] args) throws IOException {

        Config config = new Config.Builder()
                .setInputFile("/home/media/Video/Movies/Super Heroes/Marvel MCU - 1x02 - The Incredible Hulk  [1920x1080 - Freeview - ITV HD] UNEDITED.ts")
                .setOutputFile("/home/osric/scratch/result.ts")
                .setTempDir(new File("/home/osric/scratch"))
                .setPort(25245)
                .build();

        List<TimingPair> timingPairs = TimingDataParser.parse(new InputStreamReader(Main.class.getResourceAsStream("/timingData"), "utf-8"));

        ProgressMonitor monitor = new ProgressMonitor(timingPairs);

        UI ui = new UI(monitor);
        LocalServer httpServer = new LocalServer(config.getPort(), monitor);

        httpServer.start();
        ui.start();

        Splitter.split(config, timingPairs);

        httpServer.stop();
    }

}
