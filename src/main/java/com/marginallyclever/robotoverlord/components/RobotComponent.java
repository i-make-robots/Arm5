package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.systems.robot.robotarm.robotarm.ApproximateJacobian2;
import com.marginallyclever.robotoverlord.parameters.ReferenceParameter;
import com.marginallyclever.robotoverlord.robots.Robot;
import org.json.JSONException;
import org.json.JSONObject;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * RobotComponent is attached to the root of a robotic arm.
 * It requires that each link in the arm has a {@link PoseComponent} and a {@link DHComponent}.  The end effector
 * must have an {@link ArmEndEffectorComponent}.
 * @author Dan Royer
 * @since 2022-09-14
 */
public class RobotComponent extends Component implements Robot {
    private int activeJoint;
    private final List<DHComponent> bones = new ArrayList<>();
    public final ReferenceParameter gcodePath = new ReferenceParameter("Path");

    @Override
    public void setEntity(Entity entity) {
        super.setEntity(entity);
        entity.addComponent(new PoseComponent());
        findBones();
    }

    public void goHome() {
        double [] homeValues = new double[getNumBones()];
        for(int i=0;i<getNumBones();++i) {
            homeValues[i] = getBone(i).getJointHome();
        }
        setAllJointValues(homeValues);
    }

    public int getNumBones() {
        return bones.size();
    }

    public DHComponent getBone(int index) {
        return bones.get(index);
    }

    public void findBones() {
        bones.clear();

        // recursively add all DHComponents to the list.
        Queue<Entity> queue = new LinkedList<>();
        queue.add(getEntity());
        while(!queue.isEmpty()) {
            Entity e = queue.poll();
            DHComponent c = e.findFirstComponent(DHComponent.class);
            if(c!=null) bones.add(c);
            queue.addAll(e.getChildren());
        }
    }

    @Override
    public Object get(int property) {
        switch(property) {
            case NAME: return getName();
            case NUM_JOINTS: return getNumBones();
            case ACTIVE_JOINT: return activeJoint;
            case JOINT_NAME: return getBone(activeJoint).getEntity().getName();
            case JOINT_VALUE: return getActiveJointValue();
            case JOINT_RANGE_MAX: return getBone(activeJoint).getJointMax();
            case JOINT_RANGE_MIN: return getBone(activeJoint).getJointMin();
            case JOINT_HAS_RANGE_LIMITS: return true;
            case JOINT_PRISMATIC: return false;
            case END_EFFECTOR: return getEndEffectorPose();
            case END_EFFECTOR_TARGET: return getEndEffectorTargetPose();
            case END_EFFECTOR_TARGET_POSITION: return getEndEffectorTargetPosition();
            case TOOL_CENTER_POINT: return getToolCenterPoint();
            case POSE: return getPoseWorld();
            case JOINT_POSE: return getActiveJointPose();
            case JOINT_HOME: return getBone(activeJoint).getJointHome();
            case ALL_JOINT_VALUES: return getAllJointValues();
            default : return null;
        }
    }

    private Object getActiveJointValue() {
        DHComponent b = getBone(activeJoint);
        return b.getJointValue();
    }

    private Object getActiveJointPose() {
        Matrix4d m = new Matrix4d();
        m.setIdentity();
        for(int i=0;i<=activeJoint;++i) {
            m.mul(getBone(i).getLocal());
        }
        return m;
    }

    public double[] getAllJointValues() {
        double[] result = new double[getNumBones()];
        for(int i=0;i<getNumBones();++i) {
            result[i] = getBone(i).getJointValue();
        }
        return result;
    }

    @Override
    public void set(int property, Object value) {
        switch (property) {
            case ACTIVE_JOINT -> activeJoint = Math.max(0, Math.min(getNumBones(), (int) value));
            case JOINT_VALUE -> setActiveJointValue((double) value);
            case END_EFFECTOR_TARGET -> setEndEffectorTargetPose((Matrix4d) value);
            case END_EFFECTOR_TARGET_POSITION -> setEndEffectorTargetPosition((Point3d) value);
            case TOOL_CENTER_POINT -> setToolCenterPointOffset((Matrix4d) value);
            case POSE -> setPoseWorld((Matrix4d) value);
            case JOINT_HOME -> getBone(activeJoint).setJointHome((double) value);
            case ALL_JOINT_VALUES -> setAllJointValues((double[]) value);
            default -> { }
        }
    }

    private Matrix4d inBaseFrameOfReference(Matrix4d m) {
        Matrix4d base = getPoseWorld();
        assert base != null;
        base.invert();
        m.mul(base,m);
        return m;
    }

    private Matrix4d getToolCenterPoint() {
        ArmEndEffectorComponent ee = getEntity().findFirstComponentRecursive(ArmEndEffectorComponent.class);
        if(ee==null) return null;

        Matrix4d m = ee.getToolCenterPoint();
        return inBaseFrameOfReference(m);
    }

    private void setToolCenterPointOffset(Matrix4d value) {
        ArmEndEffectorComponent ee = getEntity().findFirstComponentRecursive(ArmEndEffectorComponent.class);
        if(ee==null) return;
        Matrix4d base = getPoseWorld();
        assert base != null;

        value.mul(base);
        ee.setToolCenterPoint(base);
    }

    /**
     * Returns the end effector's target pose relative to the robot's base.
     * @return the end effector's target pose relative to the robot's base.
     */
    private Matrix4d getEndEffectorTargetPose() {
        ArmEndEffectorComponent ee = getEntity().findFirstComponentRecursive(ArmEndEffectorComponent.class);
        if(ee==null) return null;

        PoseComponent pose = ee.getEntity().findFirstComponent(PoseComponent.class);
        if(pose==null) return null;
        Matrix4d m = pose.getWorld();
        return inBaseFrameOfReference(m);
    }

    /**
     * Returns the end effector's position relative to the robot's base.
     * @return the end effector's position relative to the robot's base.
     */
    private Point3d getEndEffectorTargetPosition() {
        Matrix4d m = getEndEffectorTargetPose();
        return new Point3d(m.m03, m.m13, m.m23);
    }

    /**
     * Sets the end effector target pose and immediately attempts to move the robot to that pose.
     * @param targetPose the target pose relative to the robot's base.
     * @throws RuntimeException if the robot cannot be moved to the target pose.
     */
    private void setEndEffectorTargetPose(Matrix4d targetPose) {
        Matrix4d startPose = this.getEndEffectorPose();
        double[] cartesianVelocity = MatrixHelper.getCartesianBetweenTwoMatrices(startPose, targetPose);
        applyCartesianForceToEndEffector(cartesianVelocity);
    }

    /**
     * Sets the end effector target position and immediately attempts to move the robot to that position.
     * Intended for 3 axis robots only.
     * @param targetPosition the target position relative to the robot's base.
     */
    private void setEndEffectorTargetPosition(Point3d targetPosition) {
        Matrix4d startPose = this.getEndEffectorPose();
        double[] cartesianVelocity = new double[]{
                targetPosition.x - startPose.m03,
                targetPosition.y - startPose.m13,
                targetPosition.z - startPose.m23,
                0, 0, 0};
        applyCartesianForceToEndEffector(cartesianVelocity);
    }

    private double sumCartesianVelocityComponents(double [] cartesianVelocity) {
        double sum = 0;
        for (double v : cartesianVelocity) {
            sum += Math.abs(v);
        }
        return sum;
    }

    /**
     * Applies a cartesian force to the robot, moving it in the direction of the cartesian force.
     * @param cartesianVelocity three linear forces (mm) and three angular forces (degrees).
     * @throws RuntimeException if the robot cannot be moved in the direction of the cartesian force.
     */
    private void applyCartesianForceToEndEffector(double[] cartesianVelocity) {
        double sum = sumCartesianVelocityComponents(cartesianVelocity);
        if(sum <= 1) {
            applySmallCartesianForceToEndEffector(cartesianVelocity);
        } else {
            // split the big move in to smaller moves.
            int total = (int) Math.ceil(sum);
            // allocate a new buffer so that we don't smash the original.
            double[] cartesianVelocityUnit = new double[cartesianVelocity.length];
            for (int i = 0; i < cartesianVelocity.length; ++i) {
                cartesianVelocityUnit[i] = cartesianVelocity[i] / total;
            }
            for (int i = 0; i < total; ++i) {
                applySmallCartesianForceToEndEffector(cartesianVelocityUnit);
            }
        }
    }

    /**
     * Applies a cartesian force to the robot, moving it in the direction of the cartesian force.
     * @param cartesianVelocity three linear forces (mm) and three angular forces (degrees).
     * @throws RuntimeException if the robot cannot be moved in the direction of the cartesian force.
     */
    private void applySmallCartesianForceToEndEffector(double[] cartesianVelocity) {
        ApproximateJacobian2 aj = new ApproximateJacobian2(this);
        try {
            double[] jointVelocity = aj.getJointVelocityFromCartesianVelocity(cartesianVelocity);  // uses inverse jacobian
            double[] angles = this.getAllJointValues();  // # dof long
            for (int i = 0; i < angles.length; ++i) {
                angles[i] += jointVelocity[i];
            }
            this.setAllJointValues(angles);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return The pose of the end effector relative to the robot's base.
     */
    public Matrix4d getEndEffectorPose() {
        ArmEndEffectorComponent ee = getEntity().findFirstComponentRecursive(ArmEndEffectorComponent.class);
        if(ee==null) return null;
        PoseComponent endEffectorPose = ee.getEntity().findFirstComponent(PoseComponent.class);
        if(endEffectorPose==null) return null;
        Matrix4d m = endEffectorPose.getWorld();
        return inBaseFrameOfReference(m);
    }

    /**
     * @return The pose of the robot's base relative to the world.
     */
    private Matrix4d getPoseWorld() {
        PoseComponent pose = getEntity().findFirstComponent(PoseComponent.class);
        if(pose==null) return null;
        return pose.getWorld();
    }

    /**
     * @param m The pose of the robot's base relative to the world.
     */
    private void setPoseWorld(Matrix4d m) {
        PoseComponent pose = getEntity().findFirstComponent(PoseComponent.class);
        if(pose==null) return;
        pose.setWorld(m);
    }

    private final List<PropertyChangeListener> listeners = new ArrayList<>();

    @Override
    public void addPropertyChangeListener(PropertyChangeListener p) {
        listeners.add(p);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener p) {
        listeners.remove(p);
    }

    private void notifyPropertyChangeListeners(PropertyChangeEvent ee) {
        for(PropertyChangeListener p : listeners) {
            p.propertyChange(ee);
        }
    }

    /**
     * Change one joint angle and update the end effector pose.
     * @param value the new angle for the active joint, in degrees.
     */
    private void setActiveJointValue(double value) {
        Matrix4d eeOld = getEndEffectorPose();
        getBone(activeJoint).setJointValueWRTLimits(value);
        Matrix4d eeNew = getEndEffectorPose();

        notifyPropertyChangeListeners(new PropertyChangeEvent(this,"ee",eeOld,eeNew));
    }

    public void setAllJointValues(double[] angles) {
        assert angles.length == getNumBones();

        Matrix4d eeOld = getEndEffectorPose();
        boolean changed = false;
        for(int i=0;i<getNumBones();++i) {
            DHComponent bone = getBone(i);
            double t = bone.getJointValue();
            bone.setJointValueWRTLimits(angles[i]);
            changed |= (t!=angles[i]);
        }
        if(changed) {
            Matrix4d eeNew = getEndEffectorPose();
            notifyPropertyChangeListeners(new PropertyChangeEvent(this, "ee", eeOld, eeNew));
        }
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jo = super.toJSON();

        jo.put("path", gcodePath.toJSON());

        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo) throws JSONException {
        super.parseJSON(jo);

        if(jo.has("path")) gcodePath.parseJSON(jo.getJSONObject("path"));
    }

    public String getGCodePathEntityUUID() {
        return gcodePath.get();
    }
}
