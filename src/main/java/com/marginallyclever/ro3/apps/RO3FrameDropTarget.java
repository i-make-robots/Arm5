package com.marginallyclever.ro3.apps;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.commands.ImportScene;
import com.marginallyclever.ro3.node.nodes.pose.MeshInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.List;

/**
 * Allows the user to drop Scene or a supported mesh file onto the main window.  They will be
 * imported to the existing scene and appear at the world origin.
 */
public class RO3FrameDropTarget extends DropTargetAdapter {
    private static final Logger logger = LoggerFactory.getLogger(RO3FrameDropTarget.class);

    public RO3FrameDropTarget() {
        super();
        logger.debug("adding drag + drop support...");
    }

    @Override
    public void drop(DropTargetDropEvent event) {
        try {
            Transferable tr = event.getTransferable();
            DataFlavor[] flavors = tr.getTransferDataFlavors();
            int complete = 0;
            for (DataFlavor flavor : flavors) {
                logger.debug("Possible flavor: {}", flavor.getMimeType());
                if (flavor.isFlavorJavaFileListType()) {
                    event.acceptDrop(DnDConstants.ACTION_COPY);
                    Object object = tr.getTransferData(flavor);
                    if (object instanceof List<?> list) {
                        for(Object item : list) {
                            if (item instanceof File file) {
                                if(importMesh(file.getAbsolutePath())) {
                                    // drop a mesh
                                    complete++;
                                } else if(importScene(file)) {
                                    // drop a scene
                                    complete++;
                                } // else silently ignore
                            }
                        }
                    }
                }
            }
            if(complete>0) {
                logger.debug("Drop ok: {}", complete);
                event.dropComplete(true);
                return;
            }
            logger.debug("Drop failed: {}", event);
            event.rejectDrop();
        } catch (Exception e) {
            logger.error("Drop error", e);
            event.rejectDrop();
        }
    }

    private boolean importMesh(String absolutePath) {
        logger.debug("drag importMesh {}",absolutePath);
        if(!Registry.meshFactory.canLoad(absolutePath)) {
            logger.info("can't load file.");
            return false;
        }

        MeshInstance meshInstance = new MeshInstance(getFilenameWithoutExtensionFromPath(absolutePath));
        meshInstance.setMesh(Registry.meshFactory.load(absolutePath));
        Registry.getScene().addChild(meshInstance);
        logger.error("done.");
        return true;
    }

    private boolean importScene(File file) {
        logger.debug("drag importScene {}",file);
        try {
            var imported = new ImportScene(file);
            UndoSystem.addEvent(imported);
        } catch (Exception e) {
            logger.error("Error importing scene",e);
            return false;
        }
        return true;
    }

    private String getFilenameWithoutExtensionFromPath(String absolutePath) {
        File f = new File(absolutePath);
        String fullName = f.getName();
        return fullName.substring(0,fullName.lastIndexOf('.'));
    }
}
