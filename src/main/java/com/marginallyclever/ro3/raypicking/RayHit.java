package com.marginallyclever.ro3.raypicking;

import com.marginallyclever.ro3.node.nodes.pose.MeshInstance;

import javax.vecmath.Vector3d;

/**
 * A ray hit is a record of a ray hitting a {@link MeshInstance} at a certain distance.
 * @param target the MeshInstance that the {@link com.marginallyclever.convenience.Ray} intersected.
 * @param distance the distance from the {@link com.marginallyclever.convenience.Ray} origin to the point of contact.
 * @param normal the normal of the {@link com.marginallyclever.ro3.mesh.Mesh} at the point of contact, in world space.
 * @author Dan Royer
 * @since 2.5.0
 */
public record RayHit(MeshInstance target, double distance, Vector3d normal) {}
