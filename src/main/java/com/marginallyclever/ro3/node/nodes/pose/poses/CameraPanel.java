package com.marginallyclever.ro3.node.nodes.pose.poses;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.security.InvalidParameterException;

public class CameraPanel extends JPanel {
    private final Camera camera;

    public CameraPanel() {
        this(new Camera());
    }

    public CameraPanel(Camera camera) {
        super(new GridBagLayout());
        this.camera = camera;
        this.setName(Camera.class.getSimpleName());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        SpinnerNumberModel farZModel = new SpinnerNumberModel(camera.getFarZ(), 0, 10000, 1);
        JSpinner farZSpinner = new JSpinner(farZModel);
        JSpinner nearZSpinner = new JSpinner(new SpinnerNumberModel(camera.getNearZ(), 0, 10000, 1));
        JSpinner fovSpinner = new JSpinner(new SpinnerNumberModel(camera.getFovY(), 1, 180, 1));

        // orthographic?
        JCheckBox ortho = new JCheckBox();
        ortho.addActionListener(e -> {
            camera.setDrawOrthographic(ortho.isSelected());
            var drawOrthographic = camera.getDrawOrthographic();
            farZSpinner.setEnabled(!drawOrthographic);
            nearZSpinner.setEnabled(!drawOrthographic);
            fovSpinner.setEnabled(!drawOrthographic);
        });
        ortho.setSelected(camera.getDrawOrthographic());
        PanelHelper.addLabelAndComponent(this,"Orthographic",ortho,gbc);
        gbc.gridy++;

        // fov
        fovSpinner.setValue(camera.getFovY());
        fovSpinner.addChangeListener(e -> {
            camera.setFovY( (double) fovSpinner.getValue() );
        });
        fovSpinner.setToolTipText("degrees");
        PanelHelper.addLabelAndComponent(this,"FOV",fovSpinner,gbc);
        gbc.gridy++;

        // near z
        nearZSpinner.addChangeListener(e -> {
            camera.setNearZ( (double)nearZSpinner.getValue() );
            var nearZ = camera.getNearZ();
            farZModel.setMinimum(nearZ + 1);
            if (camera.getFarZ() <= nearZ) {
                farZSpinner.setValue(nearZ + 1);
            }
        });
        nearZSpinner.setValue(camera.getNearZ());
        nearZSpinner.setToolTipText("cm");
        PanelHelper.addLabelAndComponent(this,"Near",nearZSpinner,gbc);
        gbc.gridy++;

        // far z
        farZSpinner.setValue(camera.getFarZ());
        farZSpinner.addChangeListener(e -> {
            camera.setFarZ( (double) farZSpinner.getValue() );
        });
        farZSpinner.setToolTipText("cm");
        PanelHelper.addLabelAndComponent(this,"Far",farZSpinner,gbc);
        gbc.gridy++;

        // can rotate
        JToggleButton canRotate = new JToggleButton("Yes");
        canRotate.addActionListener(e -> {
            camera.setCanRotate(canRotate.isSelected());
            updateRotateButton(canRotate);
        });
        canRotate.setSelected(camera.getCanRotate());
        updateRotateButton(canRotate);
        PanelHelper.addLabelAndComponent(this,"Can rotate",canRotate,gbc);
        gbc.gridy++;

        // can translate
        JToggleButton canTranslate = new JToggleButton("Yes");
        canTranslate.addActionListener(e -> {
            camera.setCanTranslate(canTranslate.isSelected());
            updateTranslateButton(canTranslate);
        });
        canTranslate.setSelected(camera.getCanTranslate());
        updateTranslateButton(canTranslate);
        PanelHelper.addLabelAndComponent(this,"Can translate",canTranslate,gbc);
        gbc.gridy++;

        addLookAtComponents(gbc);
    }

    private void updateTranslateButton(JToggleButton canTranslate) {
        canTranslate.setText(camera.getCanTranslate() ? "Yes" : "No");
        canTranslate.setToolTipText(camera.getCanTranslate() ? "Click to deny" : "Click to allow");
    }

    private void updateRotateButton(JToggleButton canRotate) {
        canRotate.setText(camera.getCanRotate() ? "Yes" : "No");
        canRotate.setToolTipText(camera.getCanRotate() ? "Click to deny" : "Click to allow");
    }

    private void addLookAtComponents(GridBagConstraints gbc) {
        var formatter = NumberFormatHelper.getNumberFormatter();

        JFormattedTextField tx = new JFormattedTextField(formatter);        tx.setValue(0);
        JFormattedTextField ty = new JFormattedTextField(formatter);        ty.setValue(0);
        JFormattedTextField tz = new JFormattedTextField(formatter);        tz.setValue(0);
        tx.setToolTipText("x");
        ty.setToolTipText("y");
        tz.setToolTipText("z");
        tx.setColumns(6);
        ty.setColumns(6);
        tz.setColumns(6);


        JButton button = new JButton("Set");
        button.addActionListener(e -> {
            Vector3d target = new Vector3d(
                    ((Number) tx.getValue()).doubleValue(),
                    ((Number) ty.getValue()).doubleValue(),
                    ((Number) tz.getValue()).doubleValue()
            );
            try {
                camera.lookAt(target);
            } catch (InvalidParameterException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        PanelHelper.addLabelAndComponent(this, "Look at", new JLabel(),gbc);
        gbc.gridy++;
        PanelHelper.addLabelAndComponent(this, "X", tx,gbc);
        gbc.gridy++;
        PanelHelper.addLabelAndComponent(this, "Y", ty,gbc);
        gbc.gridy++;
        PanelHelper.addLabelAndComponent(this, "Z", tz,gbc);
        gbc.gridy++;
        PanelHelper.addLabelAndComponent(this, "", button,gbc);
        gbc.gridy++;
    }
}
