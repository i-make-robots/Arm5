package com.marginallyclever.robotoverlord.components;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import org.json.JSONException;
import org.json.JSONObject;

import javax.vecmath.Matrix4d;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * A Pose component contains the local transform of an Entity - its position, rotation, and scale relative to its
 * parent.
 * @author Dan Royer
 * @since 2022-08-04
 */
@ComponentDependency(components={PoseComponent.class})
public class DHComponent extends RenderComponent implements PropertyChangeListener {
    private final DoubleParameter myD = new DoubleParameter("D",0.0);
    private final DoubleParameter myR = new DoubleParameter("R",0.0);
    private final DoubleParameter alpha = new DoubleParameter("Alpha",0.0);
    private final DoubleParameter theta = new DoubleParameter("Theta",0.0);
    private final DoubleParameter thetaMax = new DoubleParameter("Theta max",0.0);
    private final DoubleParameter thetaMin = new DoubleParameter("Theta min",0.0);
    private final DoubleParameter thetaHome = new DoubleParameter("Theta home",0.0);

    public DHComponent() {
        super();
        myD.addPropertyChangeListener(this);
        myR.addPropertyChangeListener(this);
        alpha.addPropertyChangeListener(this);
        theta.addPropertyChangeListener(this);
        setVisible(false);
    }

    @Override
    public void update(double dt) {
        super.update(dt);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jo = super.toJSON();
        jo.put("D",myD.toJSON());
        jo.put("R",myR.toJSON());
        jo.put("Alpha", alpha.toJSON());
        jo.put("Theta", theta.toJSON());
        jo.put("ThetaMax", thetaMax.toJSON());
        jo.put("ThetaMin", thetaMin.toJSON());
        jo.put("ThetaHome", thetaHome.toJSON());
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo) throws JSONException {
        super.parseJSON(jo);
        myD.parseJSON(jo.getJSONObject("D"));
        myR.parseJSON(jo.getJSONObject("R"));
        alpha.parseJSON(jo.getJSONObject("Alpha"));
        theta.parseJSON(jo.getJSONObject("Theta"));
        if(jo.has("ThetaMax")) thetaMax.parseJSON(jo.getJSONObject("ThetaMax"));
        if(jo.has("ThetaMin")) thetaMin.parseJSON(jo.getJSONObject("ThetaMin"));
        if(jo.has("ThetaHome")) thetaHome.parseJSON(jo.getJSONObject("ThetaHome"));
        refreshLocalMatrix();
    }

    @Override
    public void getView(ViewPanel view) {
        super.getView(view);

        view.add(myD);
        view.add(myR);
        view.add(alpha);
        view.add(theta);
        view.add(thetaMax);
        view.add(thetaMin);
        view.add(thetaHome);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        refreshLocalMatrix();
    }

    private void refreshLocalMatrix() {
        setLocalMatrix(getLocalMatrix());
        //updateChildAdjustmentNode();
    }

    private void updateChildAdjustmentNode() {
        getEntity().getChildren().forEach((e)->{
            OriginAdjustComponent adj = e.findFirstComponent(OriginAdjustComponent.class);
            if(adj!=null) {
                adj.adjust();
            }
        });
    }

    private void setLocalMatrix(Matrix4d localMatrix) {
        Entity entity = getEntity();
        if(entity==null) return;

        PoseComponent pose = getEntity().findFirstComponent(PoseComponent.class);
        if(pose==null) {
            pose = new PoseComponent();
            getEntity().addComponent(pose);
        }

        pose.setLocalMatrix4(localMatrix);
    }

    /**
     * @return the local transform of this entity, calculated from its D,R, alpha, and theta.
     */
    private Matrix4d getLocalMatrix() {
        Matrix4d m = new Matrix4d();
        double rt = Math.toRadians(theta.get());
        double ra = Math.toRadians(alpha.get());
        double ct = Math.cos(rt);
        double ca = Math.cos(ra);
        double st = Math.sin(rt);
        double sa = Math.sin(ra);

        double r = myR.get();

        m.m00 = ct;		m.m01 = -st*ca;		m.m02 = st*sa;		m.m03 = r*ct;
        m.m10 = st;		m.m11 = ct*ca;		m.m12 = -ct*sa;		m.m13 = r*st;
        m.m20 = 0;		m.m21 = sa;			m.m22 = ca;			m.m23 = myD.get();
        m.m30 = 0;		m.m31 = 0;			m.m32 = 0;			m.m33 = 1;

        return m;
    }

    @Override
    public String toString() {
        return super.toString()
                +",d="+myD.get()
                +",r="+myR.get()
                +",alpha="+ alpha.get()
                +",theta="+ theta.get()
                +",thetaMax="+ thetaMax.get()
                +",thetaMin="+ thetaMin.get()
                +",thetaHome="+ thetaHome.get()
                +",\n";
    }

    public void setAngleWRTLimits(double t) {
        // if max angle and min angle overlap then there is no limit on this joint.
        double max = thetaMax.get();
        double min = thetaMin.get();
        double angle = t;

        double bMiddle = (max+min)/2.0;
        double bMax = Math.abs(max-bMiddle);
        double bMin = Math.abs(min-bMiddle);
        if(bMin+bMax<360) {
            // prevent pushing the arm to an illegal angle
            angle = Math.max(Math.min(angle, max), min);
        }

        theta.set(angle % 360);
    }

    /**
     * @return the local pose of this entity.
     */
    public Matrix4d getLocal() {
        PoseComponent pose = getEntity().findFirstComponent(PoseComponent.class);
        if(pose==null) return null;
        return pose.getLocal();
    }

    public void set(double d, double r, double alpha, double theta, double tMax, double tMin) {
        this.myD.set(d);
        this.myR.set(r);
        this.alpha.set(alpha);
        this.theta.set(theta);
        this.thetaMax.set(tMax);
        this.thetaMin.set(tMin);
        refreshLocalMatrix();
    }

    public void setD(double d) {
        myD.set(d);
    }

    public double getD() { return myD.get(); }

    public void setR(double r) {
        myR.set(r);
    }

    public double getR() {
        return myR.get();
    }

    public void setAlpha(double a) {
        alpha.set(a);
    }

    public double getAlpha() { return alpha.get(); }

    public void setTheta(double angle) {
        theta.set(angle);
    }

    public double getTheta() {
        return theta.get();
    }

    public void setThetaMax(double v) {
        thetaMax.set(v);
    }

    public double getThetaMax() {
        return thetaMax.get();
    }

    public void setThetaMin(double v) {
        thetaMin.set(v);
    }

    public double getThetaMin() {
        return thetaMin.get();
    }

    public double getThetaHome() {
        return thetaHome.get();
    }

    public void setThetaHome(double t) {
        thetaHome.set(t);
    }


    @Override
    public void render(GL2 gl2) {
        boolean lit = OpenGLHelper.disableLightingStart(gl2);
        boolean tex = OpenGLHelper.disableTextureStart(gl2);
        int onTop = OpenGLHelper.drawAtopEverythingStart(gl2);

        gl2.glPushMatrix();
            Matrix4d m = getLocal();
            m.invert();
            MatrixHelper.applyMatrix(gl2,m);

            double rt = Math.toRadians(theta.get());
            double ct = Math.cos(rt);
            double st = Math.sin(rt);
            double r = myR.get();

            gl2.glBegin(GL2.GL_LINES);
                gl2.glColor3d(0, 0, 1);
                gl2.glVertex3d(0, 0, 0);
                gl2.glVertex3d(0, 0, myD.get());

                gl2.glColor3d(1, 0, 0);
                gl2.glVertex3d(0, 0, myD.get());
                gl2.glVertex3d(r*ct, r*st, myD.get());
            gl2.glEnd();
        gl2.glPopMatrix();

        OpenGLHelper.drawAtopEverythingEnd(gl2, onTop);
        OpenGLHelper.disableTextureEnd(gl2, tex);
        OpenGLHelper.disableLightingEnd(gl2, lit);
    }
}
