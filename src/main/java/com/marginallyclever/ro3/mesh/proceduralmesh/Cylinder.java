package com.marginallyclever.ro3.mesh.proceduralmesh;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MathHelper;
import com.marginallyclever.ro3.mesh.Mesh;
import org.json.JSONObject;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * <p>{@link Cylinder} is a {@link Mesh}.  It has a diameter of 1 and a height of 1.
 * The origin is at the center of the cylinder.</p>
 */
public class Cylinder extends ProceduralMesh {
    public static final int RESOLUTION_CIRCULAR = 32;
    public static final int RESOLUTION_LENGTH = 5;

    public float radius0 = 0.5f;
    public float radius1 = 0.5f;
    public float height = 1;

    public Cylinder() {
        this(2, 0.5f, 0.5f);
    }

    public Cylinder(double height, double radius0, double radius1) {
        super();

        this.radius0 = (float)radius0;
        this.radius1 = (float)radius1;
        this.height = (float)height;
        updateModel();
    }

    @Override
    public String getEnglishName() {
        return "Cylinder";
    }

    @Override
    public void updateModel() {
        this.clear();
        this.setRenderStyle(GL3.GL_TRIANGLES);
        addCylinder(height, radius0, radius1);

        var rBig = Math.max(radius0,radius1);
        boundingBox.setBounds(new Point3d(rBig,rBig,height/2),new Point3d(-rBig,-rBig,-height/2));
        fireMeshChanged();
    }

    private void addCylinder(float height, float radius0,float radius1) {
        float halfHeight = height / 2;
        if(radius0>0) addFace(-halfHeight, radius0);
        if(radius1>0) addFace(halfHeight, radius1);
        addTube(-halfHeight, halfHeight, radius0,radius1);
    }

    private void addFace(float z, float r) {
        float sign = z > 0 ? 1 : -1;
        for (int i = 0; i < RESOLUTION_CIRCULAR; ++i) {
            this.addVertex(0, 0, z);
            this.addTexCoord(0.5f,0.5f);
            this.addNormal(0, 0, sign);

            addCirclePoint(r, i, RESOLUTION_CIRCULAR, z);
            addCirclePoint(r, i + sign, RESOLUTION_CIRCULAR, z);
        }
    }

    // points on the end caps
    private void addCirclePoint(float r, float i, int resolution, float z) {
        float sign = z > 0 ? 1 : -1;
        double a = MathHelper.interpolate(0,Math.PI*2.0, (double)i/(double)resolution);
        float x = (float)Math.cos(a);
        float y = (float)Math.sin(a);
        this.addVertex(x*r,y*r,z);
        this.addTexCoord(0.5f+x*0.5f,0.5f+y*0.5f);
        this.addNormal(0,0,sign);
    }

    private void addTube(float h0, float h1, float r0, float r1) {
        float rStart = r0;
        float hStart = h0;

        float diff = (r0-r1)/(h1-h0);

        for (int i = 0; i < RESOLUTION_LENGTH; ++i) {
            float rEnd = MathHelper.interpolate(r0, r1, (double)(i+1) / (double)RESOLUTION_LENGTH);
            float hEnd = MathHelper.interpolate(h0, h1, (double)(i+1) / (double)RESOLUTION_LENGTH);
            addTubeSegment(hStart,hEnd,rStart,rEnd, diff);
            rStart = rEnd;
            hStart = hEnd;
        }
    }

    // the wall of the cylinder
    private void addTubeSegment(float z0,float z1,float r0, float r1, float diff) {
        for(int i = 0; i< RESOLUTION_CIRCULAR; ++i) {
            addTubePoint(diff,r1, i, z1);
            addTubePoint(diff,r0, i, z0);
            addTubePoint(diff,r1, i+1, z1);

            addTubePoint(diff,r0, i, z0);
            addTubePoint(diff,r0, i+1, z0);
            addTubePoint(diff,r1, i+1, z1);
        }
    }

    // points on the wall
    private void addTubePoint(float diff,float radius, int i,float z) {
        double a = Math.PI*2.0 * (double)i/(double)RESOLUTION_CIRCULAR;
        float x = (float)Math.cos(a);
        float y = (float)Math.sin(a);
        Vector3d n = new Vector3d(x,y,diff);
        n.normalize();
        this.addVertex(x*radius, y*radius, z);
        this.addTexCoord(0.5f+x*0.5f,0.5f+y*0.5f);
        this.addNormal((float)n.x, (float)n.y, (float)n.z);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        json.put("height", height);
        json.put("radius0", radius0);
        json.put("radius1", radius1);
        return json;
    }

    @Override
    public void fromJSON(JSONObject jo) {
        super.fromJSON(jo);
        if(jo.has("height")) height = jo.getFloat("height");
        if(jo.has("radius0")) radius0 = jo.getFloat("radius0");
        if(jo.has("radius1")) radius1 = jo.getFloat("radius1");
        updateModel();
    }

    /**
     * Sets both radius's and the length of the cylinder.
     * @param radius
     * @param length
     */
    public void setRadiusAndLength(double radius, double length) {
        if(radius<=0) throw new IllegalArgumentException("Radius must be greater than zero.");
        if(length<=0) throw new IllegalArgumentException("Length must be greater than zero.");
        this.radius0 = (float)radius;
        this.radius1 = (float)radius;
        this.height = (float)length;
        updateModel();
    }

    /**
     * Sets the length of the cylinder.  does not update the mesh.
     * @param length
     */
    public void setLength(double length) {
        if(length<=0) throw new IllegalArgumentException("Length must be greater than zero.");
        this.height = (float)length;
    }

    /**
     * Sets the radius of the cylinder.  does not update the mesh.
     * @param radius
     */
    public void setRadius(double radius) {
        if(radius<=0) throw new IllegalArgumentException("Radius must be greater than zero.");
        this.radius0 = (float)radius;
        this.radius1 = (float)radius;
    }
}
