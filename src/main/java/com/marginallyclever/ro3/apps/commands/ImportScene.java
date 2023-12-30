package com.marginallyclever.ro3.apps.commands;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.List;

/**
 * Load a scene from a file.
 */
public class ImportScene extends AbstractUndoableEdit {
    private final Logger logger = LoggerFactory.getLogger(ImportScene.class);
    private final File selectedFile;
    private Node created;

    public ImportScene(File selectedFile) {
        super();
        this.selectedFile = selectedFile;
        execute();
    }

    @Override
    public String getPresentationName() {
        return "Import " + selectedFile.getName();
    }

    @Override
    public void redo() {
        super.redo();
        execute();
    }

    /**
     * Load a scene from a file.
     */
    public void execute() {
        if( selectedFile == null ) throw new InvalidParameterException("Selected file is null.");
        if( !selectedFile.exists() ) throw new InvalidParameterException("File does not exist.");

        logger.info("Import scene from {}",selectedFile.getAbsolutePath());

        try {
            String content = new String(Files.readAllBytes(Paths.get(selectedFile.getAbsolutePath())));
            // Add the loaded scene to the current scene.
            var jsonObject = new JSONObject(content);
            created = createFromJSON(jsonObject);
            Registry.getScene().addChild(created);
        } catch (IOException e) {
            logger.error("Error loading scene from JSON", e);
        }
        logger.info("done.");
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        reverse();
    }

    public void reverse() {
        Node parent = created.getParent();
        parent.removeChild(created);
        created = null;
    }

    public static Node createFromJSON(JSONObject jsonObject) {
        Node loaded = Registry.nodeFactory.create(jsonObject.get("type").toString());
        loaded.fromJSON(jsonObject);
        loaded.witnessProtection();
        return loaded;
    }
}
