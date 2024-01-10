package com.marginallyclever.ro3.mesh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * {@link MeshSmoother} will smooth the normals of a {@link Mesh}.
 */
public class MeshSmoother {
	private static final Logger logger = LoggerFactory.getLogger(MeshSmoother.class);

	/**
	 * Smooth normals.  Find points within vertexEpsilon of each other, sharing normals within normalEpsilon 
	 * of each other, and then smooths the nromals (makes them the same, an average of the normals considered).
	 * Note: Modified the original model.
	 * 
	 * @param model the model containing the data to smooth. 
	 * @param vertexEpsilon how close should points be to be considered one and the same.  typically ~0.001
	 * @param normalEpsilon how close should normals be to be merged. 0...2 larger values more smoothing.
	 */
	public static void smoothNormals(Mesh model, float vertexEpsilon, float normalEpsilon) {
		float vertexEpsilonSquared = vertexEpsilon * vertexEpsilon;
		float normalEpsilonSquared = normalEpsilon * normalEpsilon;

		int numFaces = model.vertexArray.size()/3;
		ArrayList<Integer> indexList = new ArrayList<Integer>();
		boolean [] skip = new boolean[numFaces];

		int i,j;
		for(i=0;i<numFaces;++i) {
			if(skip[i]) continue;

			if((i%100)==0)  logger.info("Smoothing "+i+"/"+numFaces);

			// find vertices that are in the same position
			float p1x = model.vertexArray.get(i * 3);
			float p1y = model.vertexArray.get(i*3+1);
			float p1z = model.vertexArray.get(i*3+2);

			float n1x = model.normalArray.get(i * 3);
			float n1y = model.normalArray.get(i*3+1);
			float n1z = model.normalArray.get(i*3+2);

			indexList.clear();
			indexList.add(i);
			
			for(j=i+1;j<numFaces;++j) {
				if(skip[j]) continue;

				float p2x = model.vertexArray.get(j * 3);
				float p2y = model.vertexArray.get(j*3+1);
				float p2z = model.vertexArray.get(j*3+2);
				//if(Math.abs(p1x-p2x)>vertexEpsilonSquared) continue;
				//if(Math.abs(p1y-p2y)>vertexEpsilonSquared) continue;
				//if(Math.abs(p1z-p2z)>vertexEpsilonSquared) continue;
				
				if( lengthDifferenceSquared(p1x,p1y,p1z,p2x,p2y,p2z) <= vertexEpsilonSquared ) {

					float n2x = model.normalArray.get(j * 3);
					float n2y = model.normalArray.get(j*3+1);
					float n2z = model.normalArray.get(j*3+2);
					if( lengthDifferenceSquared(n1x,n1y,n1z,n2x,n2y,n2z) <= normalEpsilonSquared ) {
						indexList.add(j);
					}
				}
			}
			
			if(indexList.size()>1) {
				n1x=0;
				n1y=0;
				n1z=0;

				int size = indexList.size();
				int k;
				for(k=0;k<size;++k) {
					j = indexList.get(k)*3;
					n1x += model.normalArray.get(j);
					n1y += model.normalArray.get(j+1);
					n1z += model.normalArray.get(j+2);
				}
				float len = length(n1x,n1y,n1z);
				n1x /= len;
				n1y /= len;
				n1z /= len;

				for(k=0;k<size;++k) {
					j = indexList.get(k);
					skip[j]=true;
					j*=3;
					model.normalArray.set(j, n1x);
					model.normalArray.set(j+1, n1y);
					model.normalArray.set(j+2, n1z);
				}
			}
		}
		model.setDirty(true);
	}
	
	private static float lengthDifferenceSquared(float p1x,float p1y,float p1z,float p2x,float p2y,float p2z) {
		float dx = p2x-p1x;
		float dy = p2y-p1y;
		float dz = p2z-p1z;

		return lengthSquared(dx,dy,dz);
	}
	
	
	private static float lengthSquared(float dx,float dy,float dz) {
		return dx*dx+dy*dy+dz*dz;
	}
	
	private static float length(float dx,float dy,float dz) {
		return (float)Math.sqrt(lengthSquared(dx,dy,dz));
	}
}
