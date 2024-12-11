package com.marginallyclever.ro3.apps.viewport;

import com.marginallyclever.convenience.swing.Dial;
import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.apps.App;
import com.marginallyclever.ro3.apps.viewport.renderpass.DrawMeshes;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.security.InvalidParameterException;

/**
 * {@link ViewportSettingsPanel} adjusts settings for a {@link Viewport}.
 */
public class ViewportSettingsPanel extends App {
    private final Viewport viewport;
    private final NumberFormatter formatter = NumberFormatHelper.getNumberFormatterDouble();
    private final JFormattedTextField movementScale = new JFormattedTextField(formatter);
    private final JToggleButton hardwareAccelerated = new JToggleButton();
    private final JToggleButton doubleBuffered = new JToggleButton();
    private final JToggleButton originShift = new JToggleButton();
    private final JToggleButton verticalSync = new JToggleButton();
    private final JComboBox<Integer> fsaaSamples = new JComboBox<>(new Integer[]{1, 2, 4, 8});
    private final Dial timeOfDay = new Dial();
    private final Dial declination = new Dial();
    private final JButton selectSunColor = new JButton();
    private final JButton selectAmbientColor = new JButton();

    public ViewportSettingsPanel() {
        this(new Viewport());
    }
    public ViewportSettingsPanel(Viewport viewport) {
        super(new BorderLayout());

        if(viewport==null) throw new InvalidParameterException("viewport cannot be null");

        setName("Viewport");
        this.viewport = viewport;
        JPanel container = buildPanel();
        add(new JScrollPane(container),BorderLayout.CENTER);
    }

    private JPanel buildPanel() {
        var container = new JPanel(new GridBagLayout());

        setMovementScale(viewport.getUserMovementScale());
        setHardwareAccelerated(viewport.isHardwareAccelerated());
        setViewportDoubleBuffered(viewport.isDoubleBuffered());
        setVerticalSync(viewport.isVerticalSync());
        setFSAASamples(viewport.getFsaaSamples());

        // this only allows parameters from one render pass.
        // TODO: add other passes?
        DrawMeshes meshes = getDrawMeshes();
        if(meshes!=null) {
            setSunColor(meshes.getSunlightColor());
            timeOfDay.setValue(meshes.getTimeOfDay()-90);
            declination.setValue(meshes.getDeclination());
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.fill = GridBagConstraints.BOTH;

        PanelHelper.addLabelAndComponent(container, "Mouse scale", movementScale,gbc);
        formatter.setMinimum(0.001);
        movementScale.setValue(1.0);
        movementScale.addPropertyChangeListener("value", evt -> setMovementScale((Double) evt.getNewValue()));

        setHardwareAccelerated(viewport.isHardwareAccelerated());
        setViewportDoubleBuffered(viewport.isDoubleBuffered());
        setOriginShift(viewport.isOriginShift());
        setVerticalSync(viewport.isVerticalSync());

        gbc.gridy++;
        PanelHelper.addLabelAndComponent(container, "Hardware Accelerated", hardwareAccelerated,gbc);
        gbc.gridy++;
        PanelHelper.addLabelAndComponent(container, "Double Buffered", doubleBuffered,gbc);
        gbc.gridy++;
        PanelHelper.addLabelAndComponent(container, "Origin Shift", originShift,gbc);
        gbc.gridy++;
        PanelHelper.addLabelAndComponent(container, "Vertical Sync", verticalSync,gbc);

        hardwareAccelerated.addActionListener(evt -> setHardwareAccelerated(hardwareAccelerated.isSelected()));
        doubleBuffered.addActionListener(evt -> setViewportDoubleBuffered(doubleBuffered.isSelected()));
        originShift.addActionListener(evt -> setOriginShift(originShift.isSelected()));
        verticalSync.addActionListener(evt -> setVerticalSync(verticalSync.isSelected()));

        gbc.gridy++;
        PanelHelper.addLabelAndComponent(container, "FSAA Samples", fsaaSamples,gbc);
        fsaaSamples.addActionListener(evt -> setFSAASamples((Integer) fsaaSamples.getSelectedItem()));

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

    @Override
    public void removeNotify() {
        super.removeNotify();
        viewport.savePrefs();
        var dm = getDrawMeshes();
        if(dm!=null) dm.savePrefs();
    }

    private void setAmbientColor(Color color) {
        var dm = getDrawMeshes();
        if(dm==null) return;
        dm.setAmbientColor(color);
        selectAmbientColor.setBackground(color);
    }

    private void setSunColor(Color color) {
        var dm = getDrawMeshes();
        if(dm==null) return;
        dm.setSunlightColor(color);
        selectSunColor.setBackground(color);
    }

    private void updateSunPosition() {
        DrawMeshes meshes = getDrawMeshes();
        if(meshes==null) return;

        meshes.setDeclination(declination.getValue());
        meshes.setTimeOfDay(timeOfDay.getValue()+90);
    }

    private void setVerticalSyncLabel() {
        verticalSync.setText( (viewport != null && viewport.isVerticalSync() ? "On" : "Off") );
    }

    private void setViewportDoubleBufferedLabel() {
        doubleBuffered.setText( (viewport != null && viewport.isDoubleBuffered() ? "On" : "Off") );
    }

    private void setOriginShiftLabel() {
        originShift.setText( (viewport != null && viewport.isOriginShift() ? "On" : "Off") );
    }

    private void setHardwareAcceleratedLabel() {
        hardwareAccelerated.setText( (viewport != null && viewport.isHardwareAccelerated() ? "On" : "Off") );
    }

    private void setHardwareAccelerated(boolean selected) {
        viewport.setHardwareAccelerated(selected);
        hardwareAccelerated.setSelected(selected);
        setHardwareAcceleratedLabel();
    }

    private void setViewportDoubleBuffered(boolean selected) {
        viewport.setDoubleBuffered(selected);
        doubleBuffered.setSelected(selected);
        setViewportDoubleBufferedLabel();
    }

    private void setOriginShift(boolean selected) {
        viewport.setOriginShift(selected);
        originShift.setSelected(selected);
        setOriginShiftLabel();
    }

    private void setVerticalSync(boolean selected) {
        viewport.setVerticalSync(selected);
        verticalSync.setSelected(selected);
        setVerticalSyncLabel();
    }

    private void setFSAASamples(Integer value) {
        viewport.setFsaaSamples(value);
        fsaaSamples.setSelectedItem(value);
    }

    private void setMovementScale(double v) {
        if (viewport != null) viewport.setUserMovementScale(v);
    }

    private DrawMeshes getDrawMeshes() {
        for(var rp : viewport.renderPasses.getList()) {
            if(rp instanceof DrawMeshes) {
                return (DrawMeshes)rp;
            }
        }
        return null;
    }
}
