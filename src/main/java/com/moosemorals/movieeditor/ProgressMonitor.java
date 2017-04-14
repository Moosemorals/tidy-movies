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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BoundedRangeModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Osric Wilkinson <osric@fluffypeople.com>
 */
public class ProgressMonitor implements BoundedRangeModel {

    private final Logger log = LoggerFactory.getLogger(ProgressMonitor.class);

    private final float totalPercent;
    private final Set<ChangeListener> changeListeners;
    private float currentPercent = 0;
    private int fileCount = 0;

    public ProgressMonitor(List<TimingPair> pairs) {
        changeListeners = new HashSet<>();
        totalPercent = pairs.size() + 1;
        log.debug("Total percent {}", totalPercent);
    }

    public void addDuration(float percent) {
        currentPercent += percent;
        log.debug("Total {} current {} adding {}", totalPercent, currentPercent, percent);
        notifyListeners();
    }

    public void fileCompleted() {
        fileCount += 1;
        currentPercent = fileCount;
        notifyListeners();
    }

    @Override
    public int getMinimum() {
        return 0;
    }

    @Override
    public void setMinimum(int newMinimum) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getMaximum() {
        return 100;
    }

    @Override
    public void setMaximum(int newMaximum) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getValue() {
        return Math.round(currentPercent * 100 / totalPercent);
    }

    @Override
    public void setValue(int newValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setValueIsAdjusting(boolean b) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean getValueIsAdjusting() {
        return false;
    }

    @Override
    public int getExtent() {
        return 0;
    }

    @Override
    public void setExtent(int newExtent) {
        log.debug("Set extent: {}", newExtent);
    }

    @Override
    public void setRangeProperties(int value, int extent, int min, int max, boolean adjusting) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addChangeListener(ChangeListener x) {
        synchronized (changeListeners) {
            log.debug("Adding change listener");
            changeListeners.add(x);
        }
    }

    @Override
    public void removeChangeListener(ChangeListener x) {
        synchronized (changeListeners) {
            log.debug("Removing change listener");
            changeListeners.remove(x);
        }
    }

    private void notifyListeners() {
        ChangeEvent e = new ChangeEvent(this);
        SwingUtilities.invokeLater(() -> {
            synchronized (changeListeners) {
                for (ChangeListener x : changeListeners) {
                    x.stateChanged(e);
                }
            }
        });
    }

}
