package com.marginallyclever.ro3.node.nodes.ode4j;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.node.nodes.ode4j.odebody.ODEBody;
import com.marginallyclever.ro3.node.nodes.ode4j.odebody.ODESphere;

import javax.swing.*;
import java.awt.*;

/**
 * A panel for editing an ODEBody.

 */
public class ODEBodyPanel extends JPanel {
    public ODEBodyPanel() {
        this(new ODESphere());  // I had to choose one non-abstract class to use here.
    }

    public ODEBodyPanel(ODEBody body) {
        super(new GridLayout(0,2));
        this.setName(ODEBody.class.getSimpleName());

        var formatter = NumberFormatHelper.getNumberFormatter();
        formatter.setMinimum(0.000);

        JFormattedTextField massQty = new JFormattedTextField(formatter);
        massQty.setValue(body.getMassQty());
        massQty.addPropertyChangeListener("value", e -> body.setMassQty( ((Number)massQty.getValue()).doubleValue() ));
        PanelHelper.addLabelAndComponent(this,"Mass",massQty);
    }
}
