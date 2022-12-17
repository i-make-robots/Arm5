package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.Scene;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.EntityPasteEdit;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Import a scene from a file and add it to the existing scene.
 * @author Dan Royer
 * @since 2.0.0
 */
public class SceneImportAction extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(SceneImportAction.class);
    private final RobotOverlord ro;

    /**
     * The file chooser remembers the last path.
     */
    private static final JFileChooser fc = new JFileChooser();

    public SceneImportAction(String name, RobotOverlord ro) {
        super(name);
        this.ro=ro;
        fc.setFileFilter(RobotOverlord.FILE_FILTER);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (fc.showOpenDialog(ro.getMainFrame()) == JFileChooser.APPROVE_OPTION) {
            try {
                SceneLoadAction loader = new SceneLoadAction("Load Scene",ro);
                Scene source = loader.loadScene(fc.getSelectedFile());
                Scene destination = ro.getScene();

                updateSceneAssetPaths(source,destination);

                UndoSystem.reset();
            } catch(Exception e1) {
                logger.error(e1.getMessage());
                JOptionPane.showMessageDialog(ro.getMainFrame(),e1.getLocalizedMessage());
                e1.printStackTrace();
            }
        }
    }

    /**
     * Copy the assets from source scene into folder of destination scene.
     * Ideally when I import projectB into projectA, any asset with filename
     *     <i>/foo/projectB/xxxx.yyy</i>
     * should be copied to
     *     <i>bar/projectA/projectB/xxxx.yyy</i>
     * and the asset name in the project should be updated to match.
      */
    private void updateSceneAssetPaths(Scene source, Scene destination) throws IOException {
        Path path = Path.of(source.getScenePath());

        Path lastPath = path.subpath(path.getNameCount()-1,path.getNameCount());
        String destinationPath = destination.getScenePath() + File.separator + lastPath.toString();

        FileUtils.copyDirectory(
                new File(source.getScenePath()),
                new File(destinationPath)
        );

        recursivelyUpdatePaths(source,destinationPath);
        source.setScenePath(destination.getScenePath());

        // when entities are added to destination they will automatically be removed from source.
        // to prevent concurrent modification exception we have to have a copy of the list.
        List<Entity> entities = new LinkedList<>(source.getEntities());
        // now do the move safely.
        for(Entity e : entities) {
            destination.addEntity(e);
        }
    }

    /**
     * Update the paths of each asset in the scene.  At this time there are two ({@link MaterialComponent} and
     * {@link MeshFromFile}).  Instead of a lot of work I'm going to just find and update these two classes.
     * @param source
     * @param destinationPath
     */
    private void recursivelyUpdatePaths(Scene source, String destinationPath) {
        LinkedList<Entity> list = new LinkedList<>(source.getEntities());
        String originalPath = source.getScenePath();
        source.setScenePath(destinationPath);

        while(!list.isEmpty()) {
            Entity e = list.removeFirst();

            MaterialComponent material = e.findFirstComponent(MaterialComponent.class);
            if(material!=null) {
                String oldPath = material.getTextureFilename();
                String newPath = oldPath;
                if(oldPath.startsWith(originalPath)) {
                    newPath = destinationPath + oldPath.substring(originalPath.length());
                }
                material.setTextureFilename(newPath);
            }

            MeshFromFile mesh = e.findFirstComponent(MeshFromFile.class);
            if(mesh!=null) {
                String oldPath = mesh.getFilename();
                String newPath = oldPath;
                if(oldPath.startsWith(originalPath)) {
                    newPath = destinationPath + oldPath.substring(originalPath.length());
                }
                mesh.setFilename(newPath);
            }

            list.addAll(e.getEntities());
        }
    }
}
