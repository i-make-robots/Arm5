package com.marginallyclever.ro3.apps.webcampanel;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

/**
 * A panel that displays the default USB web camera.
 */
public class WebCamPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(WebCamPanel.class);
    private WebcamPanel panel;

    public WebCamPanel() {
        super(new BorderLayout());
        setName("webcam");

        var toolBar = new JToolBar();
        toolBar.setFloatable(false);

        toolBar.add(new JButton(new AbstractAction() {
            {
                putValue(NAME, "Snapshot");
                putValue(SHORT_DESCRIPTION, "Snapshot");
                putValue(SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-screenshot-16.png"))));
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                takeSnapshot();
            }
        }));


        add(toolBar, BorderLayout.NORTH);
    }

    @Override
    public void addNotify() {
        super.addNotify();

        try {
            Webcam webcam = Webcam.getDefault(1000);
            var list = webcam.getViewSizes();
            webcam.setViewSize(list[list.length - 1]);  // probably the biggest

            panel = new WebcamPanel(webcam, false);
            panel.setDrawMode(WebcamPanel.DrawMode.FIT);  // fit, fill, or none
            panel.setFPSDisplayed(true);
            add(panel, BorderLayout.CENTER);
            panel.start();
        } catch (TimeoutException e) {
            logger.error("TimeoutException",e);
            add(new JLabel("No webcam found."), BorderLayout.CENTER);
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if(panel==null) return;

        panel.stop();
        remove(panel);
        panel=null;
    }

    public void takeSnapshot() {
        if(panel==null) return;

        BufferedImage img = panel.getWebcam().getImage();
        if(img==null) return;
        logger.info("Snapshot {}x{}",img.getWidth(),img.getHeight());
        logger.error("Not implemented yet.");
    }
}
