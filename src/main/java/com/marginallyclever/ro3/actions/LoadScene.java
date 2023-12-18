package com.marginallyclever.ro3.actions;

import com.marginallyclever.ro3.RecentFilesMenu;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.robotoverlord.RobotOverlord;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidParameterException;

public class LoadScene extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(LoadScene.class);
    private static final JFileChooser chooser = new JFileChooser();
    private final String filePath;
    private final RecentFilesMenu menu;

    public LoadScene(RecentFilesMenu menu) {
        this(menu,null);
    }

    public LoadScene(RecentFilesMenu menu, String filePath) {
        super(filePath==null || filePath.isEmpty() ? "Load Scene" : filePath);
        this.menu = menu;
        this.filePath = filePath;
        chooser.setFileFilter(RobotOverlord.FILE_FILTER);
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        File src = (filePath != null)
                ? new File(filePath)  // use the given filename
                : runFileDialog((Component) e.getSource());  // ask the user for a filename
        loadAsNewScene(src);
    }

    private File runFileDialog(Component source) {
        JFrame parentFrame = (JFrame)SwingUtilities.getWindowAncestor(source);
        if (chooser.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;  // cancelled
    }

    private void loadAsNewScene(File selectedFile) {
        if( selectedFile == null ) throw new InvalidParameterException("selectedFile cannot be null");

        logger.info("Load scene from {}",selectedFile.getAbsolutePath());

        if( !selectedFile.exists() ) {
            logger.error("File does not exist.");
            menu.removePath(selectedFile.getAbsolutePath());
            return;
        }

        try {
            String content = new String(Files.readAllBytes(Paths.get(selectedFile.getAbsolutePath())));
            JSONObject json = new JSONObject(content);
            Node loaded = new Node("Scene");
            loaded.fromJSON(json);
            Registry.setScene(loaded);
            menu.addPath(selectedFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Error loading scene from JSON", e);
        }

        logger.info("done.");
    }
}