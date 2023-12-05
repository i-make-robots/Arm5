package com.marginallyclever.robotoverlord.systems.robot;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.RayHit;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.parameters.swing.ViewElementButton;
import com.marginallyclever.robotoverlord.parameters.swing.ViewElementComboBox;
import com.marginallyclever.robotoverlord.parameters.swing.ComponentSwingViewFactory;
import com.marginallyclever.robotoverlord.swing.translator.Translator;
import com.marginallyclever.robotoverlord.systems.EntitySystem;
import com.marginallyclever.robotoverlord.systems.RayPickSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;

/**
 * This system is responsible for handling roboot grippers.
 */
public class RobotGripperSystem implements EntitySystem {
    private static final Logger logger = LoggerFactory.getLogger(RobotGripperSystem.class);
    private final EntityManager entityManager;

    public RobotGripperSystem(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Get the Swing view of this component.
     *
     * @param view      the factory to use to create the panel
     * @param component the component to visualize
     */
    @Override
    public void decorate(ComponentSwingViewFactory view, Component component) {
        if( component instanceof RobotGripperComponent) decorateGripper(view,component);
    }

    /**
     * Update the system over time.
     *
     * @param dt the time step in seconds.
     */
    @Override
    public void update(double dt) {}

    private void decorateGripper(ComponentSwingViewFactory view, Component component) {
        RobotGripperComponent gripper = (RobotGripperComponent)component;

        view.add(gripper.openDistance);
        view.add(gripper.closeDistance);

        ViewElementComboBox box = (ViewElementComboBox)view.addComboBox(gripper.mode,RobotGripperComponent.names);
        box.setReadOnly(true);

        ViewElementButton bToggleGripper = view.addButton(Translator.get("RobotGripperSystem.Grab"));
        bToggleGripper.addActionEventListener((evt)-> {
            switch(gripper.mode.get()) {
                case RobotGripperComponent.MODE_OPEN -> doGrab(gripper);
                case RobotGripperComponent.MODE_CLOSED -> doRelease(gripper);
                default -> {}
            }
        });
        gripper.mode.addPropertyChangeListener( e->setGripperButton(bToggleGripper,gripper) );
        setGripperButton(bToggleGripper,gripper);
    }

    private void setGripperButton(ViewElementButton bToggleGripper,RobotGripperComponent gripper) {
        if(gripper.mode.get() == RobotGripperComponent.MODE_OPEN) {
            bToggleGripper.setText(Translator.get("RobotGripperSystem.grab"));
            bToggleGripper.setEnabled(true);
        } else if(gripper.mode.get() == RobotGripperComponent.MODE_CLOSED) {
            bToggleGripper.setText(Translator.get("RobotGripperSystem.release"));
            bToggleGripper.setEnabled(true);
        } else if(gripper.mode.get() == RobotGripperComponent.MODE_OPENING) {
            bToggleGripper.setText(Translator.get("RobotGripperSystem.opening"));
            bToggleGripper.setEnabled(false);
        } else if(gripper.mode.get() == RobotGripperComponent.MODE_CLOSING) {
            bToggleGripper.setText(Translator.get("RobotGripperSystem.closing"));
            bToggleGripper.setEnabled(false);
        }
    }

    public void doGrab(RobotGripperComponent gripper) {
        List<RobotGripperJawComponent> jaws = gripper.getJaws();
        if (jaws.isEmpty()) return;

        double distance = (gripper.openDistance.get() - gripper.closeDistance.get());

        List<RayHit> hits = new ArrayList<>();
        // cast a ray along axis of jaw travel
        for(RobotGripperJawComponent jaw : jaws) {
            Matrix4d jawMatrix = jaw.getEntity().getComponent(PoseComponent.class).getWorld();
            Point3d jawP = new Point3d(MatrixHelper.getPosition(jawMatrix));
            Vector3d jawZ = MatrixHelper.getZAxis(jawMatrix);
            Ray ray = new Ray(jawP,jawZ,distance);
            RayPickSystem picker = new RayPickSystem(entityManager);
            try {
                List<RayHit> jawHit = picker.findRayIntersections(ray);
                hits.addAll(jawHit);
            } catch (Exception e) {
                logger.error("Error while raycasting.",e);
            }
        }

        // do not consider the gripper itself or the jaws.
        removeGripperAndJawsFromHits(gripper, hits);

        if(!hits.isEmpty()) {
            // move the entities to the gripper
            for(RayHit hit : hits) {
                Entity entityBeingGrabbed = hit.target.getEntity();
                Matrix4d entityWorld = entityBeingGrabbed.getComponent(PoseComponent.class).getWorld();
                entityManager.addEntityToParent(entityBeingGrabbed, gripper.getEntity());
                entityBeingGrabbed.getComponent(PoseComponent.class).setWorld(entityWorld);
            }
        }

        // change state to "closed"
        gripper.mode.set(RobotGripperComponent.MODE_CLOSED);

        // close the gripper
        // TODO until it touches the object.
        moveJaws(gripper, distance/2);

        // remember grip direction for later
        Matrix4d gripperWorld = gripper.getEntity().getComponent(PoseComponent.class).getWorld();
        gripperWorld.setTranslation(new Vector3d());
        gripperWorld.invert();
    }

    public void doRelease(RobotGripperComponent gripper) {
        // release the object
        List<RobotGripperJawComponent> jaws = gripper.getJaws();
        List<Entity> children = new ArrayList<>(gripper.getEntity().getChildren());

        // move all non-jaw items from the list to the world
        if(children.size()>jaws.size()) {
            for(Entity child : children) {
                if(child.getComponent(RobotGripperJawComponent.class) != null) continue;
                // move the entity to the world
                Matrix4d entityWorld = child.getComponent(PoseComponent.class).getWorld();
                entityManager.addEntityToParent(child, entityManager.getRoot());
                child.getComponent(PoseComponent.class).setWorld(entityWorld);
            }
        }

        // change state to "open"
        gripper.mode.set(RobotGripperComponent.MODE_OPEN);

        // open the gripper
        double distance = (gripper.openDistance.get() - gripper.closeDistance.get());
        moveJaws(gripper, -distance/2);
    }

    private void moveJaws(RobotGripperComponent gripper, double distance) {
        List<RobotGripperJawComponent> jaws = gripper.getJaws();
        for(RobotGripperJawComponent jaw : jaws) {
            moveOneJaw(jaw, distance);
        }
    }

    private void moveOneJaw(RobotGripperJawComponent jaw, double distance) {
        PoseComponent pose = jaw.getEntity().getComponent(PoseComponent.class);
        Matrix4d m = pose.getWorld();
        Vector3d p = MatrixHelper.getPosition(m);
        Vector3d z = MatrixHelper.getZAxis(m);
        p.scaleAdd(distance,z,p);
        m.setTranslation(p);
        pose.setWorld(m);
    }

    private void removeGripperAndJawsFromHits(RobotGripperComponent gripper, List<RayHit> hits) {
        List<RobotGripperJawComponent> jaws = gripper.getJaws();
        List<ShapeComponent> meshes = new ArrayList<>();
        for(RobotGripperJawComponent jaw : jaws) {
            meshes.add(jaw.getEntity().getComponent(ShapeComponent.class));
        }
        ShapeComponent gripperBody = gripper.getEntity().getComponent(ShapeComponent.class);
        hits.removeIf(hit -> meshes.contains(hit.target) || hit.target == gripperBody);
    }
}
