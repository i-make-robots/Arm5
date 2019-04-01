package com.marginallyclever.robotOverlord.dhRobot;

import javax.vecmath.Matrix4d;

import com.marginallyclever.robotOverlord.model.Model;

/**
 * Denavit–Hartenberg parameters
 * @author Dan Royer
 * @see https://en.wikipedia.org/wiki/Denavit%E2%80%93Hartenberg_parameters
 */
public class DHLink {
	// offset along previous Z to the common normal
	public double d;
	
	// angle about previous Z, from old X to new X
	public double theta;
	
	// length of the common normal. Assuming a revolute joint, this is the radius about previous Z
	public double r;
	
	// angle about common normal, from old Z axis to new Z axis
	public double alpha;
	
	// computed matrix based on the D-H parameters
	public Matrix4d pose;

	// computed matrix based on the D-H parameters
	public Matrix4d poseCumulative;

	// 3D model to render at this link
	public Model model;
	
	// any combination of the READ_ONLY_* flags, used to control the GUI.
	public int flags;
	
	public final static int READ_ONLY_D		= 1;
	public final static int READ_ONLY_THETA	= 1<<1;
	public final static int READ_ONLY_R		= 1<<2;
	public final static int READ_ONLY_ALPHA	= 1<<3;
	
	public double angleMin,angleMax;
	public double maxVelocity;
	public double maxAcceleration;
	public double maxTorque;
		
	
	public DHLink() {
		flags=0;
		d=0;
		theta=0;
		r=0;
		alpha=0;
		pose = new Matrix4d();
		poseCumulative = new Matrix4d();
		model=null;
		angleMin=-90;
		angleMax=90;
		maxVelocity=Double.MAX_VALUE;
		maxAcceleration=Double.MAX_VALUE;
		maxTorque=Double.MAX_VALUE;
	}
	
	/**
	 * Equivalent to (n-1)T(n) = TransZ(n-1)(dn) * RotZ(n-1)(theta) * TransX(n)(r) * RotX(alpha)
	 */
	public void refreshPoseMatrix() {
		double ct = Math.cos(Math.toRadians(theta));
		double ca = Math.cos(Math.toRadians(alpha));
		double st = Math.sin(Math.toRadians(theta));
		double sa = Math.sin(Math.toRadians(alpha));
		
		pose.m00 = ct;
		pose.m01 = -st*ca;
		pose.m02 = st*sa;
		pose.m03 = r*ct;
		
		pose.m10 = st;
		pose.m11 = ct*ca;
		pose.m12 = -ct*sa;
		pose.m13 = r*st;
		
		pose.m20 = 0;
		pose.m21 = sa;
		pose.m22 = ca;
		pose.m23 = d;
		
		pose.m30 = 0;
		pose.m31 = 0;
		pose.m32 = 0;
		pose.m33 = 1;
	}
}
