/*
 * The MIT License
 *
 * Copyright 2016 Osric Wilkinson (osric@fluffypeople.com).
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

import com.moosemorals.elite.types.StarSystem;
import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Osric Wilkinson (osric@fluffypeople.com)
 */
public class UI implements PropertyChangeListener, ActionListener {

    private final Logger log = LoggerFactory.getLogger(UI.class);
    private final TrayIcon icon;
    private final Main main;

    private static final String ACTION_EXIT = "Exit";
    private static final String ACTION_AUTH = "Auth";

    public UI(Main main) {

        this.main = main;

        SystemTray tray = SystemTray.getSystemTray();

        BufferedImage imgIcon;
        try {

            imgIcon = ImageIO.read(getClass().getResourceAsStream("/images/elite-dangerous-minimalistic-small.png"));
        } catch (IOException ex) {
            throw new RuntimeException("Can't load icon image", ex);
        }

        icon = new TrayIcon(imgIcon);
        icon.setImageAutoSize(true);

        PopupMenu menu = new PopupMenu();

        MenuItem item;
        item = new MenuItem("Authorise Client");
        item.setActionCommand(ACTION_AUTH);
        item.addActionListener(this);
        menu.add(item);

        item = new MenuItem("Exit");
        item.setActionCommand(ACTION_EXIT);
        item.addActionListener(this);
        menu.add(item);

        try {
            icon.setPopupMenu(menu);
            tray.add(icon);
        } catch (AWTException ex) {
            throw new RuntimeException("Can't add icon to tray", ex);
        }

    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        switch (pce.getPropertyName()) {
            case "system":
                StarSystem system = (StarSystem) pce.getNewValue();
                log.debug("System changed to {}", system);
                icon.displayMessage("System changed", system.getName(), TrayIcon.MessageType.INFO);
                break;

        }
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        switch (ae.getActionCommand()) {
            case ACTION_EXIT:
                SystemTray.getSystemTray().remove(icon);
                main.stop();
                break;
            case ACTION_AUTH:
                try {
                    String host = InetAddress.getLocalHost().getHostName();
                    new AuthFrame("http://" + host + ":8081/");
                } catch (UnknownHostException ex) {
                    log.warn("Can't get hostname");
                    new AuthFrame("Can't authenticate");
                }
                break;

        }
    }
}
