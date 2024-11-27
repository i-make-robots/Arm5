package com.marginallyclever.ro3.apps.viewport;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.Camera;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.event.ActionEvent;
import java.util.Objects;

/**
 * Turn the active camera to face the last selected Pose.
 */
public class LookAtLastSelected extends AbstractAction {
    private final Viewport viewport;

    public LookAtLastSelected(Viewport viewport) {
        super("Look At Last Selected");
        this.viewport = viewport;
        putValue(Action.SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/pose/poses/icons8-look-16.png"))));
        putValue(Action.SHORT_DESCRIPTION,"Look at the last selected Pose.");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Pose lastFound = null;
        for(Node node : Registry.selection.getList()) {
            if(node instanceof Pose pose) {
                lastFound = pose;
            }
        }
        if(lastFound==null) return;

        // look at the last selected pose
        Camera camera = viewport.getActiveCamera();
        Matrix4d m = camera.getWorld();
        var cameraPosition = MatrixHelper.getPosition(m);
        var lastFoundPosition = MatrixHelper.getPosition(lastFound.getWorld());
        var lookAt = MatrixHelper.lookAt(lastFoundPosition,cameraPosition);
        m.set(lookAt);
        m.setTranslation(cameraPosition);
        camera.setWorld(m);
        // adjust the camera orbit to be the distance from the camera to the last selected pose.
        Vector3d diff = new Vector3d(cameraPosition);
        diff.sub(lastFoundPosition);
        double distance = Math.max(1,diff.length());
        // TODO use the bounding sphere of the pose to determine the zoom distance.
        camera.setOrbitRadius(distance);
    }
}
