package com.marginallyclever.robotOverlord.robots.sixi3;

import javax.vecmath.Matrix4d;

import com.marginallyclever.robotOverlord.shape.Shape;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;
import com.marginallyclever.robotOverlord.uiExposedTypes.DoubleEntity;

/**
 * Math representation of one link in a robot arm described with DH parameters.
 * @author Dan Royer
 * @since 2021-02-24
 *
 */
public class Sixi3Bone {
	private String name = "";
	
	// D-H parameters combine to make this matrix which is relative to the parent.
	private Matrix4d pose = new Matrix4d();
	// length (mm) along previous Z to the common normal
	private double d;
	// length (mm) of the common normal. movement on X.  Assuming a revolute joint, this is the radius about previous Z
	private double r;
	// angle (degrees) about common normal, from old Z axis to new Z axis
	private double alpha;
	// angle (degrees) about previous Z, from old X to new X
	public double theta;
	
	private double angleMax, angleMin;
		
	// model and relative offset from DH origin
	public Shape shape;
	
	public DoubleEntity slider = new DoubleEntity("J",0);
		
	public Sixi3Bone() {}
	
	public void set(String name,double dd,double rr,double aa,double tt,double aMax,double aMin,String shapeFilename) {
		d=dd;
		r=rr;
		alpha=aa;
		theta=tt;
		angleMax=aMax;
		angleMin=aMin;
		shape = new Shape(name,shapeFilename);
	}
	
	public void setAngleWRTLimits(double newAngle) {
		// if max angle and min angle overlap then there is no limit on this joint.
		double bMiddle = getAngleMiddle();
		double bMax = Math.abs(angleMax-bMiddle);
		double bMin = Math.abs(angleMin-bMiddle);
		if(bMin+bMax<360) {
			// prevent pushing the arm to an illegal angle
			newAngle = Math.max(Math.min(newAngle, angleMax), angleMin);
		}
		
		theta = newAngle;
	}
	
	public void updateMatrix() {
		assert(!Double.isNaN(theta));
		assert(!Double.isNaN(alpha));
		assert(!Double.isNaN(r));
		assert(!Double.isNaN(d));
		double rt = Math.toRadians(theta);
		double ra = Math.toRadians(alpha);
		double ct = Math.cos(rt);
		double ca = Math.cos(ra);
		double st = Math.sin(rt);
		double sa = Math.sin(ra);
		
		pose.m00 = ct;		pose.m01 = -st*ca;		pose.m02 = st*sa;		pose.m03 = r*ct;
		pose.m10 = st;		pose.m11 = ct*ca;		pose.m12 = -ct*sa;		pose.m13 = r*st;
		pose.m20 = 0;		pose.m21 = sa;			pose.m22 = ca;			pose.m23 = d;
		pose.m30 = 0;		pose.m31 = 0;			pose.m32 = 0;			pose.m33 = 1;
	}

	public void getView(ViewPanel view) {
		view.addRange(slider,(int)angleMax,(int)angleMin);
	}
	
	public double getAngleMiddle() {
		return (angleMax+angleMin)/2;
	}

	public double getAngleMax() {
		return angleMax;
	}

	public double getAngleMin() {
		return angleMin;
	}

	public double getD() {
		return d;
	}

	public double getR() {
		return r;
	}

	public double getAlpha() {
		return alpha;
	}

	public double getTheta() {
		return theta;
	}

	public Matrix4d getPose() {
		return pose;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		slider.setName(name);
	}
}