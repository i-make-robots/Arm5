package com.marginallyclever.robotoverlord.systems;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.EntityManager;
import com.marginallyclever.robotoverlord.components.DHComponent;
import com.marginallyclever.robotoverlord.components.RobotComponent;
import com.marginallyclever.robotoverlord.components.demo.CrabRobotComponent;
import com.marginallyclever.robotoverlord.components.demo.DogRobotComponent;
import com.marginallyclever.robotoverlord.components.path.GCodePathComponent;
import com.marginallyclever.robotoverlord.components.robot.robotarm.robotpanel.DHTable;
import com.marginallyclever.robotoverlord.components.robot.robotarm.robotpanel.RobotPanel;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ViewElementButton;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.*;

public class RobotROSystem implements ROSystem {
    private final EntityManager entityManager;

    public RobotROSystem(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void decorate(ComponentPanelFactory view, Component component) {
        if( component instanceof RobotComponent ) decorateRobot(view,component);
        if( component instanceof DHComponent ) decorateDH(view,component);
        if( component instanceof DogRobotComponent ) decorateDog(view,component);
        if( component instanceof CrabRobotComponent ) decorateCrab(view,component);
    }

    private void decorateDH(ComponentPanelFactory view, Component component) {
        DHComponent dh = (DHComponent)component;
        view.add(dh.isRevolute);
        view.add(dh.myD);
        view.add(dh.myR);
        view.add(dh.alpha);
        view.add(dh.theta);
        view.add(dh.jointMax);
        view.add(dh.jointMin);
        view.add(dh.jointHome);
    }

    private void decorateRobot(ComponentPanelFactory view, Component component) {
        RobotComponent robotComponent = (RobotComponent)component;

        view.add(robotComponent.gcodePath);

        robotComponent.findBones();

        ViewElementButton bOpen = view.addButton(Translator.get("RobotROSystem.controlPanel"));
        bOpen.addActionEventListener((evt)-> showControlPanel(bOpen,robotComponent));

        ViewElementButton bDHTable = view.addButton(Translator.get("RobotROSystem.DHTable"));
        bDHTable.addActionEventListener((evt)-> showDHTable(bDHTable,robotComponent));

        ViewElementButton bHome = view.addButton("Go home");
        bHome.addActionEventListener((evt)-> robotComponent.goHome());
    }

    private void showDHTable(JComponent parent,RobotComponent robotComponent) {
        JFrame parentFrame = (JFrame)SwingUtilities.getWindowAncestor(parent);

        JDialog frame = new JDialog(parentFrame, Translator.get("RobotROSystem.DHTable"));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(new DHTable(robotComponent));
        frame.pack();
        frame.setLocationRelativeTo(parentFrame);
        frame.setVisible(true);
    }

    private void showControlPanel(JComponent parent,RobotComponent robotComponent) {
        JFrame parentFrame = (JFrame)SwingUtilities.getWindowAncestor(parent);

        try {
            JDialog frame = new JDialog(parentFrame, Translator.get("RobotROSystem.controlPanel"));
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(new RobotPanel(robotComponent,getGCodePath(robotComponent)));
            frame.pack();
            frame.setLocationRelativeTo(parentFrame);
            frame.setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showConfirmDialog(parentFrame, ex.getMessage(), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        }
    }

    private GCodePathComponent getGCodePath(RobotComponent robotComponent) {
        String entityUniqueID = robotComponent.getGCodePathEntityUUID();
        if(entityUniqueID==null) return null;
        Entity entity = entityManager.findEntityByUniqueID(entityUniqueID);
        if(entity==null) return null;
        return entity.findFirstComponent(GCodePathComponent.class);
    }

    public void decorateDog(ComponentPanelFactory view,Component component) {
        DogRobotComponent dog = (DogRobotComponent)component;
        view.add(dog.standingRadius);
        view.add(dog.standingHeight);
        view.add(dog.turningStrideLength);
        view.add(dog.strideLength);
        view.add(dog.strideHeight);

        view.addComboBox(dog.modeSelector, DogRobotComponent.MODE_NAMES);
        view.add(dog.speedScale);
    }

    public void decorateCrab(ComponentPanelFactory view,Component component) {
        CrabRobotComponent crab = (CrabRobotComponent)component;
        view.add(crab.standingRadius);
        view.add(crab.standingHeight);
        view.add(crab.turningStrideLength);
        view.add(crab.strideLength);
        view.add(crab.strideHeight);

        view.addComboBox(crab.modeSelector, crab.MODE_NAMES);
        view.add(crab.speedScale);
    }
}
