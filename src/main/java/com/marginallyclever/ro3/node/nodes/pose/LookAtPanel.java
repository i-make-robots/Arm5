package com.marginallyclever.ro3.node.nodes.pose;

import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;
import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import java.awt.*;

public class LookAtPanel extends JPanel {
    public LookAtPanel(LookAt lookAt) {
        super(new GridLayout(0,2));
        this.setName(LookAt.class.getSimpleName());

        NodeSelector<Pose> selector = new NodeSelector<>(Pose.class,lookAt.getTarget());
        selector.addPropertyChangeListener("subject", (evt) -> {
            lookAt.setTarget(selector.getSubject());
        } );
        PanelHelper.addLabelAndComponent(this,"Target",selector);
    }
}
