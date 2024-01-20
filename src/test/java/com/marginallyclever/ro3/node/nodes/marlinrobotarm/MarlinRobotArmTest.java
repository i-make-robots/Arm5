package com.marginallyclever.ro3.node.nodes.marlinrobotarm;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.Limb;
import com.marginallyclever.ro3.node.nodes.limbsolver.LimbSolver;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class MarlinRobotArmTest {
    @Test
    void testGetTarget() {
        Limb limb = new Limb();
        LimbSolver limbSolver = new LimbSolver();
        MarlinRobotArm marlinRobotArm = new MarlinRobotArm();
        Pose target = new Pose();
        limb.addChild(marlinRobotArm);
        limb.addChild(limbSolver);
        limb.addChild(target);
        limbSolver.setTarget(target);
        marlinRobotArm.setSolver(limbSolver);
        assertEquals(target, marlinRobotArm.getTarget());
    }

    @Test
    void testSendGCode() {
        MarlinRobotArm marlinRobotArm = new MarlinRobotArm();
        MarlinListener mockListener = Mockito.mock(MarlinListener.class);
        marlinRobotArm.addMarlinListener(mockListener);
        marlinRobotArm.sendGCode("M114");

        Mockito.verify(mockListener).messageFromMarlin(Mockito.startsWith("Ok: M114"));

    }

    @Test
    void testSetLimbAndSolver() {
        Limb limb = new Limb();
        MarlinRobotArm marlinRobotArm = new MarlinRobotArm();
        LimbSolver limbSolver = new LimbSolver();
        limb.addChild(marlinRobotArm);
        limb.addChild(limbSolver);
        marlinRobotArm.setLimb(limb);
        marlinRobotArm.setSolver(limbSolver);
        assertEquals(limb, marlinRobotArm.getLimb().getSubject());
        assertEquals(limbSolver, marlinRobotArm.getSolver().getSubject());
    }

    @Test
    public void toFromJSON() {
        Registry.start();

        var limb = new Limb("a");
        var solver = new LimbSolver("b");

        MarlinRobotArm mra = new MarlinRobotArm();
        mra.setReportInterval(mra.getReportInterval()+10);
        mra.addChild(limb);
        mra.addChild(solver);
        mra.setLimb(limb);
        mra.setSolver(solver);

        var json = mra.toJSON();
        MarlinRobotArm mra2 = new MarlinRobotArm();
        mra2.fromJSON(json);

        assertEquals(mra.getReportInterval(),mra2.getReportInterval());
        assertEquals(mra.getLimb().getSubject().getName(),mra2.getLimb().getSubject().getName());
        assertEquals(mra.getSolver().getSubject().getName(),mra2.getSolver().getSubject().getName());
    }
}