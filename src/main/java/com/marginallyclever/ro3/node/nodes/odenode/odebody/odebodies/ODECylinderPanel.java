package com.marginallyclever.ro3.node.nodes.odenode.odebody.odebodies;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.physics.ODE4JHelper;

import javax.swing.*;
import java.awt.*;
import java.util.function.DoubleConsumer;

/**
 * A panel for editing an ODEBox.
 */
public class ODECylinderPanel extends JPanel {
    public ODECylinderPanel() {
        this(new ODECylinder());
    }

    public ODECylinderPanel(ODECylinder body) {
        super(new GridLayout(0,2));
        this.setName(ODECylinder.class.getSimpleName());

        addField("Radius", body.getRadius(), body::setRadius);
        addField("Length", body.getLength(), body::setLength);

        JButton setMassByVolume = new JButton("Set");
        setMassByVolume.addActionListener(e -> {
            body.setMassQty(ODE4JHelper.volumeCylinder(body.getRadius(),body.getLength()));
        });
        PanelHelper.addLabelAndComponent(this,"Mass by Volume",setMassByVolume);
    }

    private void addField(String label, double originalValue, DoubleConsumer setSize) {
        var formatter = NumberFormatHelper.getNumberFormatterDouble();
        formatter.setMinimum(0.001);

        JFormattedTextField field = new JFormattedTextField(formatter);
        field.setValue(originalValue);
        field.addPropertyChangeListener("value", e -> setSize.accept( ((Number)field.getValue()).doubleValue() ));
        PanelHelper.addLabelAndComponent(this,label,field);
    }
}
