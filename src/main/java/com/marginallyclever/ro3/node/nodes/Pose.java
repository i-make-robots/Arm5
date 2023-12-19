package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.robotoverlord.swing.CollapsiblePanel;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;

public class Pose extends Node {
    private final Matrix4d local = MatrixHelper.createIdentityMatrix4();
    private MatrixHelper.EulerSequence rotationIndex = MatrixHelper.EulerSequence.YXZ;

    public Pose() {
        this("Pose");
    }

    public Pose(String name) {
        super(name);
    }

    public Matrix4d getLocal() {
        return local;
    }

    public void setLocal(Matrix4d m) {
        local.set(m);
    }

    public Matrix4d getWorld() {
        // search up the tree to find the world transform.
        Pose p = findParent(Pose.class);
        if(p==null) {
            return new Matrix4d(local);
        }
        Matrix4d parentWorld = p.getWorld();
        parentWorld.mul(local);
        return parentWorld;
    }

    /**
     * Build a Swing Component that represents this Node.
     * @param list the list to add components to.
     */
    public void getComponents(List<JComponent> list) {
        CollapsiblePanel panel = new CollapsiblePanel(Pose.class.getSimpleName());
        list.add(panel);
        JPanel pane = panel.getContentPane();

        pane.setLayout(new GridLayout(0, 2));

        NumberFormat format = NumberFormat.getNumberInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Double.class);
        formatter.setAllowsInvalid(true);
        formatter.setCommitsOnValidEdit(true);

        addTranslationComponents(pane,formatter);
        addRotationComponents(pane,formatter);

        super.getComponents(list);
    }

    private void addTranslationComponents(JPanel pane, NumberFormatter formatter) {
        JFormattedTextField tx = new JFormattedTextField(formatter);        tx.setValue(local.m03);
        JFormattedTextField ty = new JFormattedTextField(formatter);        ty.setValue(local.m13);
        JFormattedTextField tz = new JFormattedTextField(formatter);        tz.setValue(local.m23);

        tx.addPropertyChangeListener("value", e -> local.m03 = ((Number) tx.getValue()).doubleValue() );
        ty.addPropertyChangeListener("value", e -> local.m13 = ((Number) ty.getValue()).doubleValue() );
        tz.addPropertyChangeListener("value", e -> local.m23 = ((Number) tz.getValue()).doubleValue() );

        addLabelAndComponent(pane, "Translation", new JLabel());
        addLabelAndComponent(pane, "X", tx);
        addLabelAndComponent(pane, "Y", ty);
        addLabelAndComponent(pane, "Z", tz);
    }

    private void addRotationComponents(JPanel pane, NumberFormatter formatter) {
        Vector3d r = getRotationEuler();

        JFormattedTextField rx = new JFormattedTextField(formatter);        rx.setValue(r.x);
        JFormattedTextField ry = new JFormattedTextField(formatter);        ry.setValue(r.y);
        JFormattedTextField rz = new JFormattedTextField(formatter);        rz.setValue(r.z);

        String [] names = new String[MatrixHelper.EulerSequence.values().length];
        int i=0;
        for(MatrixHelper.EulerSequence s : MatrixHelper.EulerSequence.values()) {
            names[i++] = "Euler "+s.toString();
        }
        JComboBox<String> rotationType = new JComboBox<>(names);
        rotationType.setSelectedIndex(rotationIndex.ordinal());
        rotationType.addPropertyChangeListener("selectedIndex", e -> {
            rotationIndex = MatrixHelper.EulerSequence.values()[rotationType.getSelectedIndex()];
        });

        rx.addPropertyChangeListener("value", e -> {
            Vector3d r2 = getRotationEuler();
            r2.x = ((Number) rx.getValue()).doubleValue();
            setRotationEuler(r2, rotationIndex);
        });
        ry.addPropertyChangeListener("value", e -> {
            Vector3d r2 = getRotationEuler();
            r2.y = ((Number) ry.getValue()).doubleValue();
            setRotationEuler(r2, rotationIndex);
        });
        rz.addPropertyChangeListener("value", e -> {
            Vector3d r2 = getRotationEuler();
            r2.z = ((Number) rz.getValue()).doubleValue();
            setRotationEuler(r2, rotationIndex);
        });

        addLabelAndComponent(pane, "Rotation", new JLabel());
        addLabelAndComponent(pane, "Type", rotationType);
        addLabelAndComponent(pane, "X", rx);
        addLabelAndComponent(pane, "Y", ry);
        addLabelAndComponent(pane, "Z", rz);
    }

    /**
     * @return the rotation of this pose using Euler angles in degrees.
     */
    public Vector3d getRotationEuler() {
        Vector3d r = MatrixHelper.matrixToEuler(local);
        r.scale(180.0/Math.PI);
        return r;
    }

    /**
     * Set the rotation of this pose using Euler angles.
     *
     * @param r Euler angles in degrees.
     * @param orderOfRotation the order of rotation.
     */
    public void setRotationEuler(Vector3d r, MatrixHelper.EulerSequence orderOfRotation) {
        Vector3d p = getPosition();
        Vector3d rRad = new Vector3d(r);
        rRad.scale(Math.PI/180.0);
        local.set(MatrixHelper.eulerToMatrix(rRad, orderOfRotation));
        setPosition(p);
    }

    public Vector3d getPosition() {
        return new Vector3d(local.m03,local.m13,local.m23);
    }

    public void setPosition(Vector3d p) {
        local.m03 = p.x;
        local.m13 = p.y;
        local.m23 = p.z;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();

        double[] localArray = MatrixHelper.matrix4dToArray(local);
        json.put("local", new JSONArray(localArray));
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("local")) {
            JSONArray localArray = from.getJSONArray("local");
            double[] localData = new double[16];
            for (int i = 0; i < 16; i++) {
                localData[i] = localArray.getDouble(i);
            }
            local.set(localData);
        }
    }
}
