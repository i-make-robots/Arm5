package com.marginallyclever.robotoverlord.components;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import org.json.JSONObject;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;

/**
 * A gripper is a component that can grab and hold things.  The jaws rotate around a point.
 * @author Dan Royer
 * @since 2.11.0
 */
public class GripperComponentRotary extends GripperComponentAbstract {
    public GripperComponentRotary() {
        super();
    }

    /**
     * @return the center of the {@link GripperComponentJaw} child entities in world space.
     */
    public List<Point3d> getPoints() {
        List<Entity> children = getEntity().getChildren();
        List<Point3d> results = new ArrayList<>();
        for(Entity child : children) {
            if(child.getComponent(GripperComponentJaw.class)==null) continue;
            Matrix4d pose = child.getComponent(PoseComponent.class).getWorld();
            results.add(new Point3d(MatrixHelper.getPosition(pose)));
        }
        return results;
    }

    @Override
    public void render(GL3 gl) {
        List<Entity> children = getEntity().getChildren();
        if(children.size()<2) return;

        myMesh.setRenderStyle(GL3.GL_LINES);
        myMesh.clear();
        for(GripperComponentJaw jaw : getJaws()) {
            Matrix4d m = jaw.getEntity().getComponent(PoseComponent.class).getLocal();
            Vector3d p = MatrixHelper.getPosition(m);
            Vector3d z = MatrixHelper.getZAxis(m);
            double d = (jaw.openDistance.get() - jaw.closeDistance.get());
            z.scaleAdd(d,z,p);

            myMesh.addColor(1.0f,0.0f,0.5f,1.0f);  myMesh.addVertex((float)p.x,(float)p.y,(float)p.z);
            myMesh.addColor(1.0f,0.5f,1.0f,1.0f);  myMesh.addVertex((float)z.x,(float)z.y,(float)z.z);
        }
        myMesh.render(gl);
    }
}
