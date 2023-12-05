package com.marginallyclever.robotoverlord.systems.robot;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.GripperComponentLinear;
import com.marginallyclever.robotoverlord.components.GripperComponentJaw;
import com.marginallyclever.robotoverlord.components.shapes.Box;
import com.marginallyclever.robotoverlord.components.shapes.Cylinder;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.vecmath.Matrix4d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

public class RobotGripperSystemTest {
    private EntityManager entityManager;
    private RobotGripperSystem gripperSystem;
    private Entity base;
    private Entity j0;
    private Entity j1;
    private Entity subject;
    private GripperComponentLinear gripperComponent;

    @BeforeEach
    public void BeforeEach() {
        entityManager = new EntityManager();
        gripperSystem = new RobotGripperSystem(entityManager);
        base = new Entity("base");
        j0 = new Entity("j0");
        j1 = new Entity("j1");
        subject = new Entity("subject");

        entityManager.addEntityToParent(j0,base);
        entityManager.addEntityToParent(j1,base);
        entityManager.addEntityToParent(base,entityManager.getRoot());
        entityManager.addEntityToParent(subject,entityManager.getRoot());

        GripperComponentJaw jaw0 = new GripperComponentJaw();
        GripperComponentJaw jaw1 = new GripperComponentJaw();

        j0.addComponent(new Box());
        j0.addComponent(jaw0);

        j1.addComponent(new Box());
        j1.addComponent(jaw1);

        base.addComponent(new Cylinder());

        subject.addComponent(new Box());

        jaw0.openDistance.set(5.0);
        jaw0.closeDistance.set(0.0);
        jaw1.openDistance.set(5.0);
        jaw1.closeDistance.set(0.0);

        // set the jaws in the open position
        Matrix4d m = MatrixHelper.createIdentityMatrix4();
        m.rotY(Math.PI/2);
        m.setTranslation(new Vector3d(-2.5,0,2.5));

        j0.getComponent(PoseComponent.class).setWorld(m);
        m.rotY(-Math.PI/2);
        m.setTranslation(new Vector3d(2.5,0,2.5));
        j1.getComponent(PoseComponent.class).setWorld(m);
        // put the subject in the middle of the gripper
        subject.getComponent(PoseComponent.class).setPosition(new Vector3d(0,0,2.5));

        // set the gripper base to the origin
        gripperComponent = new GripperComponentLinear();
        base.addComponent(gripperComponent);
    }

    @Test
    public void grabAndRelease() {
        // test grab
        gripperSystem.doGrabLinear(gripperComponent);
        Assertions.assertEquals(base, subject.getParent());

        // test release
        gripperSystem.doReleaseLinear(gripperComponent);
        Assertions.assertEquals(entityManager.getRoot(), subject.getParent());

        // assert that the subjectWorld pose is rotated Z+90 degrees
        checkSubjectMatchesBase(subject, base);
    }

    @Test
    public void grabAndReleaseWithRotation() {
        gripperSystem.doGrabLinear(gripperComponent);
        // turn the gripper Z+90 degrees
        base.getComponent(PoseComponent.class).setRotation(new Vector3d(0,0,90));
        gripperSystem.doReleaseLinear(gripperComponent);

        // assert that the subject is rotated same as base
        checkSubjectMatchesBase(subject,base);

        Matrix4d subjectWorld = subject.getComponent(PoseComponent.class).getWorld();
        assertEquals(MatrixHelper.getPosition(subjectWorld),new Vector3d(0.0,0.0,2.5),0.0001);

        Vector3d jp0 = MatrixHelper.getPosition(j0.getComponent(PoseComponent.class).getWorld());
        assertEquals(jp0,new Vector3d(0.0,-2.5,2.5),0.0001);
        Vector3d jp1 = MatrixHelper.getPosition(j1.getComponent(PoseComponent.class).getWorld());
        assertEquals(jp1,new Vector3d(0.0,2.5,2.5),0.0001);
    }

    private void assertEquals(Tuple3d a,Tuple3d b,double margin) {
        Assertions.assertEquals(a.x, b.x, margin);
        Assertions.assertEquals(a.y, b.y, margin);
        Assertions.assertEquals(a.z, b.z, margin);
    }

    @Test
    public void grabAndReleaseWithRotationAndTranslation() {
        gripperSystem.doGrabLinear(gripperComponent);
        // turn the gripper Z+90 degrees
        base.getComponent(PoseComponent.class).setRotation(new Vector3d(0,0,90));
        // translate (1,2,3)
        base.getComponent(PoseComponent.class).setPosition(new Vector3d(1,2,3));
        gripperSystem.doReleaseLinear(gripperComponent);

        // assert that the subject is rotated same as base
        checkSubjectMatchesBase(subject,base);

        Matrix4d subjectWorld = subject.getComponent(PoseComponent.class).getWorld();
        assertEquals(MatrixHelper.getPosition(subjectWorld),new Vector3d(1.0,2.0,5.5),0.0001);

        Vector3d jp0 = MatrixHelper.getPosition(j0.getComponent(PoseComponent.class).getWorld());
        assertEquals(jp0,new Vector3d(1.0,2.0-2.5,5.5),0.0001);
        Vector3d jp1 = MatrixHelper.getPosition(j1.getComponent(PoseComponent.class).getWorld());
        assertEquals(jp1,new Vector3d(1.0,2.0+2.5,5.5),0.0001);

    }

    private void checkSubjectMatchesBase(Entity subject, Entity base) {
        Matrix4d baseWorld = base.getComponent(PoseComponent.class).getWorld();
        Matrix4d subjectWorld = subject.getComponent(PoseComponent.class).getWorld();
        Assertions.assertEquals(subjectWorld.m00,baseWorld.m00,0.0001);
        Assertions.assertEquals(subjectWorld.m10,baseWorld.m10,0.0001);
        Assertions.assertEquals(subjectWorld.m20,baseWorld.m20,0.0001);
        Assertions.assertEquals(subjectWorld.m01,baseWorld.m01,0.0001);
        Assertions.assertEquals(subjectWorld.m11,baseWorld.m11,0.0001);
        Assertions.assertEquals(subjectWorld.m21,baseWorld.m21,0.0001);
        Assertions.assertEquals(subjectWorld.m02,baseWorld.m02,0.0001);
        Assertions.assertEquals(subjectWorld.m12,baseWorld.m12,0.0001);
        Assertions.assertEquals(subjectWorld.m22,baseWorld.m22,0.0001);
    }
}
