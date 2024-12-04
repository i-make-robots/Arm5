package com.marginallyclever.ro3.apps.log;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.marginallyclever.ro3.apps.App;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.Properties;
import java.util.TreeSet;

/**
 * <p>{@link LogPanel} is a read-only panel that contains the log and a button to open the log file location in the
 * OS.</p>
 */
public class LogPanel extends App {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LogPanel.class);
    private final JTextArea logArea = new JTextArea();

    public LogPanel() {
        super(new BorderLayout());

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new JButton(new OpenLogFileLocation()));
        add(toolbar, BorderLayout.NORTH);

        logArea.setEditable(false);
        JScrollPane scroll = new JScrollPane();
        scroll.setViewportView(logArea);
        add(scroll, BorderLayout.CENTER);

        // append log events to this panel
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        LogPanelAppender appender = new LogPanelAppender(this);
        appender.setContext(lc);
        rootLogger.addAppender(appender);
        appender.start();

        reportSystemInfo();
    }

    private void reportSystemInfo() {
        logger.info("------------------------------------------------");
        Properties p = System.getProperties();
        TreeSet<String> list = new TreeSet<>(p.stringPropertyNames());
        for(String n : list) {
            logger.info(n+" = "+p.get(n));
        }
        logger.info("locale = "+ Locale.getDefault());
        logger.info("------------------------------------------------");
    }

    public void appendToLog(String message) {
        logArea.append(message + "\n");
    }
}
