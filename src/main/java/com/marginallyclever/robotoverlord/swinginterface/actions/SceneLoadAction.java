package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.Scene;
import com.marginallyclever.robotoverlord.swinginterface.UnicodeIcon;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class SceneLoadAction extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(SceneLoadAction.class);
    private final RobotOverlord robotOverlord;

    /**
     * The file chooser remembers the last path.
     */
    private static final JFileChooser fc = new JFileChooser();

    public SceneLoadAction(RobotOverlord robotOverlord) {
        super(Translator.get("SceneLoadAction.name"));
        this.robotOverlord = robotOverlord;
        fc.setFileFilter(RobotOverlord.FILE_FILTER);
        putValue(SMALL_ICON,new UnicodeIcon("🗁"));
        putValue(SHORT_DESCRIPTION, Translator.get("SceneLoadAction.shortDescription"));
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK) );
    }

    public static void setLastDirectory(String s) {
        fc.setCurrentDirectory(new File(s));
    }

    public static String getLastDirectory() {
        return fc.getCurrentDirectory().getAbsolutePath();
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (fc.showOpenDialog(robotOverlord.getMainFrame()) == JFileChooser.APPROVE_OPTION) {
            loadIntoScene(fc.getSelectedFile());
        }
    }

    /**
     * Load the file into the current Scene.
     * @param filename the file to load
     */
    public void loadIntoScene(String filename) {
        loadIntoScene(new File(filename));
    }

    /**
     * Load the file into the current Scene.
     * @param file the file to load
     */
    public void loadIntoScene(File file) {
        try {
            Scene source = loadNewScene(file);

            SceneClearAction clear = new SceneClearAction(robotOverlord);
            clear.clearScene();

            Scene destination = robotOverlord.getScene();
            destination.setScenePath(source.getScenePath());
            // when entities are added to destination they will automatically be removed from source.
            // to prevent concurrent modification exception we have to have a copy of the list.
            List<Entity> entities = new LinkedList<>(source.getChildren());
            // now do the move safely.
            for(Entity e : entities) {
                destination.addEntity(e);
            }

            UndoSystem.reset();
        } catch(Exception e1) {
            logger.error(e1.getMessage());
            JOptionPane.showMessageDialog(robotOverlord.getMainFrame(),e1.getLocalizedMessage());
            e1.printStackTrace();
        }
    }

    /**
     * Attempt to load the file into a new Scene.
     * @param file the file to load
     * @return the new Scene
     * @throws IOException if the file cannot be read
     */
    public Scene loadNewScene(File file) throws IOException {
        StringBuilder responseStrBuilder = new StringBuilder();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String inputStr;
            while ((inputStr = reader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }
        }

        logger.debug("Loading scene from {}", file.getAbsolutePath());

        String pathName = (Paths.get(file.getAbsolutePath())).getParent().toString();
        Scene nextScene = new Scene(pathName);
        try {
            nextScene.parseJSON(new JSONObject(responseStrBuilder.toString()));
        } catch(Exception e1) {
            logger.error(e1.getMessage());
            JOptionPane.showMessageDialog(robotOverlord.getMainFrame(),e1.getLocalizedMessage());
            e1.printStackTrace();
        }

        return nextScene;
    }

}
