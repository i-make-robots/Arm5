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
     * Update the system over time.
     * @param dt the time step in seconds.
     */
    @Override
    public void update(double dt) {}

    /**
     * Get the Swing view of this component.
     *
     * @param view      the factory to use to create the panel
     * @param component the component to visualize
     */
    @Override
    public void decorate(ComponentSwingViewFactory view, Component component) {
        if( component instanceof GripperComponentLinear) decorateGripperLinear(view,(GripperComponentLinear)component);
        else if( component instanceof GripperComponentRotary) decorateGripperRotary(view,(GripperComponentRotary)component);
        else if( component instanceof GripperComponentJaw) decorateGripperJaw(view,(GripperComponentJaw)component);
    }

    private void decorateGripperJaw(ComponentSwingViewFactory view, GripperComponentJaw jaw) {
        view.add(jaw.openDistance);
        view.add(jaw.closeDistance);
    }

    private void decorateGripperRotary(ComponentSwingViewFactory view, GripperComponentRotary gripper) {
        ViewElementComboBox box = (ViewElementComboBox)view.addComboBox(gripper.mode, GripperComponentAbstract.names);
        box.setReadOnly(true);
        ViewElementButton bToggleGripper = view.addButton(Translator.get("RobotGripperSystem.grab"));
        bToggleGripper.addActionEventListener((evt)-> {
            switch(gripper.mode.get()) {
                case GripperComponentLinear.MODE_OPEN -> doGrabRotary(gripper);
                case GripperComponentLinear.MODE_CLOSED -> doReleaseRotary(gripper);
                default -> {}
            }
        });
        gripper.mode.addPropertyChangeListener( e->setGripperButton(bToggleGripper,gripper) );
        setGripperButton(bToggleGripper,gripper);
    }

    private void decorateGripperLinear(ComponentSwingViewFactory view, GripperComponentLinear gripper) {
        ViewElementComboBox box = (ViewElementComboBox)view.addComboBox(gripper.mode, GripperComponentAbstract.names);
        box.setReadOnly(true);

        ViewElementButton bToggleGripper = view.addButton(Translator.get("RobotGripperSystem.grab"));
        bToggleGripper.addActionEventListener((evt)-> {
            switch(gripper.mode.get()) {
                case GripperComponentLinear.MODE_OPEN -> doGrabLinear(gripper);
                case GripperComponentLinear.MODE_CLOSED -> doReleaseLinear(gripper);
                default -> {}
            }
        });
        gripper.mode.addPropertyChangeListener( e->setGripperButton(bToggleGripper,gripper) );
        setGripperButton(bToggleGripper,gripper);
    }

    private void setGripperButton(ViewElementButton bToggleGripper, GripperComponentAbstract gripper) {
        switch (gripper.getMode()) {
            case GripperComponentLinear.MODE_OPEN:
                bToggleGripper.setText(Translator.get("RobotGripperSystem.grab"));
                bToggleGripper.setEnabled(true);
                break;
            case GripperComponentLinear.MODE_CLOSED:
                bToggleGripper.setText(Translator.get("RobotGripperSystem.release"));
                bToggleGripper.setEnabled(true);
                break;
            case GripperComponentLinear.MODE_OPENING:
                bToggleGripper.setText(Translator.get("RobotGripperSystem.opening"));
                bToggleGripper.setEnabled(false);
                break;
            case GripperComponentLinear.MODE_CLOSING:
                bToggleGripper.setText(Translator.get("RobotGripperSystem.closing"));
                bToggleGripper.setEnabled(false);
                break;
        }
    }

    private void doGrabRotary(GripperComponentRotary gripper) {
        List<GripperComponentJaw> jaws = gripper.getJaws();
        if (jaws.isEmpty()) return;

        // Cast a ray along axis of jaw travel and collect all hits
        List<RayHit> hits = new ArrayList<>();
        for(GripperComponentJaw jaw : jaws) {
            double distance = (jaw.openDistance.get() - jaw.closeDistance.get());
            Matrix4d jawMatrix = jaw.getEntity().getComponent(PoseComponent.class).getWorld();
            Point3d jawP = new Point3d(MatrixHelper.getPosition(jawMatrix));
            Vector3d jawZ = MatrixHelper.getZAxis(jawMatrix);
            Ray ray = new Ray(jawP,jawZ,distance);
            RayPickSystem picker = new RayPickSystem(entityManager);
            try {
                List<RayHit> jawHit = picker.findRayIntersections(ray);
                hits.addAll(jawHit);
            } catch (Exception e) {
                logger.error("Error while ray casting.",e);
            }
        }

        doGrabShared(gripper, hits);

        // Close the gripper
        // FIXME until it touches the object.
        moveJawsRotary(gripper, 1);
    }

    void doGrabLinear(GripperComponentLinear gripper) {
        List<GripperComponentJaw> jaws = gripper.getJaws();
        if (jaws.isEmpty()) return;

        // cast a ray along axis of jaw travel and collect all hits
        List<RayHit> hits = new ArrayList<>();
        for(GripperComponentJaw jaw : jaws) {
            double distance = (jaw.openDistance.get() - jaw.closeDistance.get());
            Matrix4d jawMatrix = jaw.getEntity().getComponent(PoseComponent.class).getWorld();
            Point3d jawP = new Point3d(MatrixHelper.getPosition(jawMatrix));
            Vector3d jawZ = MatrixHelper.getZAxis(jawMatrix);
            Ray ray = new Ray(jawP,jawZ,distance);
            RayPickSystem picker = new RayPickSystem(entityManager);
            try {
                List<RayHit> jawHit = picker.findRayIntersections(ray);
                hits.addAll(jawHit);
            } catch (Exception e) {
                logger.error("Error while ray casting.",e);
            }
        }

        doGrabShared(gripper, hits);

        // close the gripper
        // FIXME until it touches the object.
        moveJawsLinear(gripper,1);

        // remember grip direction for later
        Matrix4d gripperWorld = gripper.getEntity().getComponent(PoseComponent.class).getWorld();
        gripperWorld.setTranslation(new Vector3d());
        gripperWorld.invert();
    }

    /**
     * Change the gripper status to closed and move all picked items to be children of the gripper.
     * @param gripper the gripper to use
     * @param hits the list of items to move
     */
    private void doGrabShared(GripperComponentAbstract gripper, List<RayHit> hits) {
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
        gripper.mode.set(GripperComponentLinear.MODE_CLOSED);
    }

    /**
     * Open the gripper
     * @param gripper
     */
    private void doReleaseRotary(GripperComponentRotary gripper) {
        doReleaseShared(gripper);
        moveJawsRotary(gripper, -1);
    }

    /**
     * Open the gripper
     * @param gripper
     */
    void doReleaseLinear(GripperComponentLinear gripper) {
        doReleaseShared(gripper);
        moveJawsLinear(gripper, -1);
    }

    private void doReleaseShared(GripperComponentAbstract gripper) {
        // release the object
        List<GripperComponentJaw> jaws = gripper.getJaws();
        List<Entity> children = new ArrayList<>(gripper.getEntity().getChildren());

        // move all non-jaw items from the list to the world
        if(children.size()>jaws.size()) {
            for(Entity child : children) {
                if(child.getComponent(GripperComponentJaw.class) != null) continue;
                // move the entity to the world
                Matrix4d entityWorld = child.getComponent(PoseComponent.class).getWorld();
                entityManager.addEntityToParent(child, entityManager.getRoot());
                child.getComponent(PoseComponent.class).setWorld(entityWorld);
            }
        }

        // change state to "open"
        gripper.mode.set(GripperComponentLinear.MODE_OPEN);
    }

    /**
     * @param gripper the gripper to move
     * @param direction 1 for close, -1 for open
     */
    private void moveJawsRotary(GripperComponentRotary gripper, double direction) {
        List<GripperComponentJaw> jaws = gripper.getJaws();
        for(GripperComponentJaw jaw : jaws) {
            double distance = (jaw.openDistance.get() - jaw.closeDistance.get());
            moveOneJawRotary(jaw, distance);
        }
    }

    private void moveOneJawRotary(GripperComponentJaw jaw, double distance) {
        PoseComponent pose = jaw.getEntity().getComponent(PoseComponent.class);
        Vector3d r = pose.getRotation();
        r.z += distance;
        pose.setRotation(r);
    }

    /**
     * @param gripper the gripper to move
     * @param direction 1 for close, -1 for open
     */
    private void moveJawsLinear(GripperComponentLinear gripper,double direction) {
        List<GripperComponentJaw> jaws = gripper.getJaws();
        for(GripperComponentJaw jaw : jaws) {
            double distance = (jaw.openDistance.get() - jaw.closeDistance.get());
            moveOneJawLinear(jaw, distance*direction);
        }
    }

    private void moveOneJawLinear(GripperComponentJaw jaw, double distance) {
        PoseComponent pose = jaw.getEntity().getComponent(PoseComponent.class);
        Matrix4d m = pose.getWorld();
        Vector3d p = MatrixHelper.getPosition(m);
        Vector3d z = MatrixHelper.getZAxis(m);
        p.scaleAdd(distance,z,p);
        m.setTranslation(p);
        pose.setWorld(m);
    }

    private void removeGripperAndJawsFromHits(GripperComponentAbstract gripper, List<RayHit> hits) {
        List<GripperComponentJaw> jaws = gripper.getJaws();
        List<ShapeComponent> meshes = new ArrayList<>();
        for(GripperComponentJaw jaw : jaws) {
            meshes.add(jaw.getEntity().getComponent(ShapeComponent.class));
        }
        ShapeComponent gripperBody = gripper.getEntity().getComponent(ShapeComponent.class);
        hits.removeIf(hit -> meshes.contains(hit.target) || hit.target == gripperBody);
    }
}
