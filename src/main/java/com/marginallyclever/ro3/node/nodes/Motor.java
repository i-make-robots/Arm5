package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;
import com.marginallyclever.ro3.node.NodePanelHelper;
import com.marginallyclever.ro3.node.NodePath;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * A {@link Motor} is a {@link Node} that can be attached to a {@link HingeJoint}.  It will then drive the joint
 * according to the motor's settings.
 */
public class Motor extends Node {
    private final NodePath<HingeJoint> hinge = new NodePath<>(this,HingeJoint.class);

    public Motor() {
        this("Motor");
    }

    public Motor(String name) {
        super(name);
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        if(hinge.getSubject()!=null) {
            // change Hinge values to affect the Pose.
            // TODO DC motors, alter the hinge to apply force to the joint.
            // TODO Stepper motors, simulate moving in fixed steps.
        }
    }

    @Override
    public void getComponents(List<JPanel> list) {
        JPanel pane = new JPanel(new GridLayout(0,2));
        list.add(pane);
        pane.setName(Motor.class.getSimpleName());

        NodeSelector<HingeJoint> selector = new NodeSelector<>(HingeJoint.class,hinge.getSubject());
        selector.addPropertyChangeListener("subject", (evt) ->{
            hinge.setRelativePath(this,selector.getSubject());
        });
        NodePanelHelper.addLabelAndComponent(pane, "Hinge", selector);

        super.getComponents(list);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        json.put("version",1);
        if(hinge.getSubject()!=null) json.put("hinge",hinge.getPath());
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        int version = from.has("version") ? from.getInt("version") : 0;
        if(from.has("hinge")) {
            if(version==1) {
                hinge.setPath(from.getString("hinge"));
            } else if(version==0) {
                HingeJoint joint = this.getRootNode().findNodeByID(from.getString("hinge"), HingeJoint.class);
                hinge.setRelativePath(this, joint);
            }
        }
    }

    public HingeJoint getHinge() {
        return hinge.getSubject();
    }

    /**
     * Set the hinge this motor will drive.  the hinge must be in the same node tree as this motor.
     * @param hinge the hinge this motor will drive.
     */
    public void setHinge(HingeJoint hinge) {
        this.hinge.setRelativePath(this, hinge);
    }

    public boolean hasHinge() {
        return getHinge()!=null;
    }
}
