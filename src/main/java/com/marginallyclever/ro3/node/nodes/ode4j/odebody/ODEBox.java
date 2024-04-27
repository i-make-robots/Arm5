package com.marginallyclever.ro3.node.nodes.ode4j.odebody;

import com.marginallyclever.ro3.mesh.shapes.Box;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.ode4j.ODE4JHelper;
import com.marginallyclever.ro3.node.nodes.ode4j.ODEWorldSpace;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import org.json.JSONObject;
import org.ode4j.ode.DBox;

import javax.swing.*;
import java.util.List;

import static org.ode4j.ode.OdeHelper.createBox;

/**
 * Wrapper for a ODE4J Box.
 */
public class ODEBox extends ODEBody {
    private double sizeX=5.0, sizeY=5.0, sizeZ=5.0;

    public ODEBox() {
        this("ODE Box");
    }

    public ODEBox(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new ODEBoxPanel(this));
        super.getComponents(list);
    }

    @Override
    protected void onFirstUpdate() {
        super.onFirstUpdate();

        ODEWorldSpace physics = ODE4JHelper.guaranteePhysicsWorld();
        geom = createBox(physics.getODESpace(), sizeX, sizeY, sizeZ);
        geom.setBody(body);

        mass.setBoxTotal(ODE4JHelper.volumeBox(sizeX,sizeY,sizeZ), sizeX, sizeY, sizeZ);
        body.setMass(mass);

        if(findFirstChild(MeshInstance.class)==null) addChild(new MeshInstance());
        if(findFirstChild(Material.class)==null) addChild(new Material());
        updateSize();
    }

    public double getSizeX() {
        return sizeX;
    }

    public double getSizeY() {
        return sizeY;
    }

    public double getSizeZ() {
        return sizeZ;
    }

    public void setSizeX(double size) {
        if(size<=0) throw new IllegalArgumentException("Size must be greater than zero.");
        this.sizeX = size;
        updateSize();
    }

    public void setSizeY(double size) {
        if(size<=0) throw new IllegalArgumentException("Size must be greater than zero.");
        this.sizeY = size;
        updateSize();
    }

    public void setSizeZ(double size) {
        if(size<=0) throw new IllegalArgumentException("Size must be greater than zero.");
        this.sizeZ = size;
        updateSize();
    }

    private void updateSize() {
        if(geom==null) return;

        ((DBox)geom).setLengths(sizeX, sizeY, sizeZ);
        geom.setBody(body);

        mass.setBoxTotal(mass.getMass(), sizeX, sizeY, sizeZ);
        body.setMass(mass);

        var meshInstance = findFirstChild(MeshInstance.class);
        if(meshInstance!=null) {
            meshInstance.setMesh(new Box(sizeX,sizeY,sizeZ));
        }
    }

    @Override
    public JSONObject toJSON() {
        var json= super.toJSON();
        json.put("sizeX", sizeX);
        json.put("sizeY", sizeY);
        json.put("sizeZ", sizeZ);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {
        super.fromJSON(json);
        if(json.has("sizeX")) sizeX = json.getDouble("sizeX");
        if(json.has("sizeY")) sizeY = json.getDouble("sizeY");
        if(json.has("sizeZ")) sizeZ = json.getDouble("sizeZ");
        updateSize();
    }
}
