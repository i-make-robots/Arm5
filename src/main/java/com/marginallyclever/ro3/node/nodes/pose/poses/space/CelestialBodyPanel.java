package com.marginallyclever.ro3.node.nodes.pose.poses.space;

import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import java.awt.*;

public class CelestialBodyPanel extends JPanel {
    public final CelestialBody body;

    public CelestialBodyPanel() {
        this(new CelestialBody());
    }

    public CelestialBodyPanel(CelestialBody body) {
        super(new GridLayout(0,2));
        this.body = body;

        var mass = PanelHelper.addNumberField("Mass",body.mass);
        PanelHelper.addLabelAndComponent(this,"Mass",mass);

        var radius = PanelHelper.addNumberField("Radius",body.radius);
        PanelHelper.addLabelAndComponent(this,"Radius",radius);

        var rotationalPeriod = PanelHelper.addNumberField("rotationalPeriod",body.rotationalPeriod);  // hours
        PanelHelper.addLabelAndComponent(this,"rotational period",rotationalPeriod);
        var perihelion = PanelHelper.addNumberField("perihelion",body.perihelion);  // 10^6 km
        PanelHelper.addLabelAndComponent(this,"perihelion",perihelion);
        var aphelion = PanelHelper.addNumberField("aphelion",body.aphelion);  // 10^6 km
        PanelHelper.addLabelAndComponent(this,"aphelion",aphelion);
        var orbitalPeriod = PanelHelper.addNumberField("orbitalPeriod",body.orbitalPeriod);  // days
        PanelHelper.addLabelAndComponent(this,"orbital period",orbitalPeriod);
        var orbitalVelocity = PanelHelper.addNumberField("orbitalVelocity",body.orbitalVelocity);  // km/s
        PanelHelper.addLabelAndComponent(this,"orbital velocity",orbitalVelocity);
        var orbitalInclination = PanelHelper.addNumberField("orbitalInclination",body.orbitalInclination);  // degrees
        PanelHelper.addLabelAndComponent(this,"orbital inclination",orbitalInclination);
        var orbitalEccentricity = PanelHelper.addNumberField("orbitalEccentricity",body.orbitalEccentricity);
        PanelHelper.addLabelAndComponent(this,"orbital eccentricity",orbitalEccentricity);
        var obliquityToOrbit = PanelHelper.addNumberField("obliquityToOrbit",body.obliquityToOrbit); // degrees
        PanelHelper.addLabelAndComponent(this,"obliquity to orbit",obliquityToOrbit);

        mass.addPropertyChangeListener("value", e->updateSize(mass,radius));
        radius.addPropertyChangeListener("value", e->updateSize(mass,radius));
    }

    private void updateSize(JFormattedTextField mass, JFormattedTextField radius) {
        body.mass = ((Number)mass.getValue()).doubleValue();
        body.radius = ((Number)radius.getValue()).doubleValue();
        body.updateSize();
    }
}
