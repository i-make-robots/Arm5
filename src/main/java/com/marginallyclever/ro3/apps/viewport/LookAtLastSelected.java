package com.marginallyclever.ro3.apps.viewport;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.Camera;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;

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

        double distance = 0;
        Camera camera = viewport.getActiveCamera();

        // look at the last selected pose
        Matrix4d m = camera.getWorld();
        var cameraPosition = MatrixHelper.getPosition(m);
        var lastFoundPosition = MatrixHelper.getPosition(lastFound.getWorld());
        var lookAt = MatrixHelper.lookAt(lastFoundPosition,cameraPosition);

        // adjust the camera orbit distance so that orbiting will be around the lastFound position.
        Vector3d diff = new Vector3d(cameraPosition);
        diff.sub(lastFoundPosition);
        distance = Math.max(1, diff.length());

        m.set(lookAt,cameraPosition,1);
        camera.setWorld(m);
        camera.setOrbitRadius(distance);
    }
}
