package com.marginallyclever.ro3.node.nodes.environment;

import com.marginallyclever.convenience.swing.Dial;
import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.apps.viewport.renderpass.DrawMeshes;

import javax.swing.*;
import java.awt.*;

/**
 * {@link EnvironmentPanel} adjusts settings for a {@link Environment}.
 */
public class EnvironmentPanel extends JPanel {
    private final Environment environment;
    private final Dial timeOfDay = new Dial();
    private final Dial declination = new Dial();
    private final JButton selectSunColor = new JButton();
    private final JButton selectAmbientColor = new JButton();

    public EnvironmentPanel() {
        this(new Environment());
    }

    public EnvironmentPanel(Environment environment) {
        super(new BorderLayout());
        setName("Environment");
        this.environment = environment;
        JPanel container = buildPanel();
        add(container, BorderLayout.NORTH);
    }

    private JPanel buildPanel() {
        var container = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.fill = GridBagConstraints.BOTH;

        setSunColor(environment.getSunlightColor());
        timeOfDay.setValue(environment.getTimeOfDay()-90);
        declination.setValue(environment.getDeclination());

        // TODO the lighting settings below here should be per-scene.
        // ambient color
        PanelHelper.addColorChooser(container,"Ambient",Color.DARK_GRAY,this::setAmbientColor,gbc);
        // sun color
        PanelHelper.addColorChooser(container,"Sun color",Color.WHITE,this::setSunColor,gbc);

        gbc.weighty = 1.0;
        // sun position
        gbc.gridy++;
        PanelHelper.addLabelAndComponent(container, "Time of day (24h)", timeOfDay,gbc);
        timeOfDay.addActionListener(e->updateSunPosition());
        timeOfDay.setPreferredSize(new Dimension(100,100));

        // sun position
        gbc.gridy++;
        PanelHelper.addLabelAndComponent(container, "Declination (+/-90)", declination,gbc);
        declination.addActionListener(e->{
            if(declination.getValue()>90) declination.setValue(90);
            if(declination.getValue()<-90) declination.setValue(-90);
            updateSunPosition();
        });
        declination.setPreferredSize(new Dimension(100,100));

        return container;
    }

    private void setAmbientColor(Color color) {
        selectAmbientColor.setBackground(color);
        environment.setAmbientColor(color);
    }

    private void setSunColor(Color color) {
        selectSunColor.setBackground(color);
        environment.setSunlightColor(color);
    }

    private void updateSunPosition() {
        environment.setDeclination(declination.getValue());
        environment.setTimeOfDay(timeOfDay.getValue()+90);
    }
}
