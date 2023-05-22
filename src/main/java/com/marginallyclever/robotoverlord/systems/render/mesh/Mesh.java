package com.marginallyclever.robotoverlord.systems.render.mesh;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.AABB;
import com.marginallyclever.convenience.helpers.IntersectionHelper;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.robotoverlord.RayHit;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * {@link Mesh} contains the vertex, normal, maybe color, and maybe texture data for a 3D model.
 * It uses Vertex Buffer Objects to optimize rendering large collections of triangles.
 * @author Dan Royer
 */
public class Mesh {
	public static final int NUM_BUFFERS=5;  // verts, normals, colors, textureCoordinates, index
	public static final int BYTES_PER_INT = Integer.SIZE/8;
	public static final int BYTES_PER_FLOAT = Float.SIZE/8;

	public final transient List<Float> vertexArray = new ArrayList<>();
	public final transient List<Float> normalArray = new ArrayList<>();
	public final transient List<Float> colorArray = new ArrayList<>();
	public final transient List<Float> textureArray = new ArrayList<>();
	public final transient List<Integer> indexArray = new ArrayList<>();

	private transient boolean hasNormals = false;
	private transient boolean hasColors = false;
	private transient boolean isTransparent = false;
	private transient boolean hasTextures = false;
	private transient boolean hasIndexes = false;
	private transient boolean isDirty = false;
	private transient boolean isLoaded = false;

	private transient int[] VAO;
	private transient int[] VBO;

	public int renderStyle = GL2.GL_TRIANGLES;
	private String fileName = null;

	// bounding limits
	protected final AABB AABB = new AABB();

	public Mesh() {
		super();
		AABB.setShape(this);
	}
	
	/**
	 * Remove all vertexes, normals, colors, texture coordinates, etc.
	 * on the next call to systems() the mesh will be rebuilt to nothing.
	 */
	public void clear() {
		vertexArray.clear();
		normalArray.clear();
		colorArray.clear();
		textureArray.clear();
		indexArray.clear();
		isDirty=true;
	}

	public void setSourceName(String filename) {
		this.fileName = filename;
	}
	
	public String getSourceName() {
		return fileName;
	}

	public boolean isLoaded() {
		return isLoaded;
	}
	
	public void setLoaded(boolean loaded) {
		isLoaded=loaded;
	}

	public boolean isTransparent() {
		return isTransparent;
	}

	public void unload(GL2 gl2) {
		if(!isLoaded) return;
		isLoaded=false;
		destroyBuffers(gl2);
	}
	
	private void createBuffers(GL2 gl2) {
		VAO = new int[1];
		gl2.glGenVertexArrays(NUM_BUFFERS, VAO, 0);

		VBO = new int[NUM_BUFFERS];
		gl2.glGenBuffers(NUM_BUFFERS, VBO, 0);
	}

	private void destroyBuffers(GL2 gl2) {
		if(VBO != null) {
			gl2.glDeleteBuffers(NUM_BUFFERS, VBO, 0);
			VBO = null;
		}
		if(VAO != null) {
			gl2.glDeleteVertexArrays(NUM_BUFFERS, VAO, 0);
			VAO = null;
		}
	}
	
	/**
	 * Regenerate the optimized rendering buffers for the fixed function pipeline.
	 * Also recalculate the bounding box.
	 * @param gl2 the OpenGL context
	 */
	private void updateBuffers(GL2 gl2) {
		long numVertexes = getNumVertices();

		gl2.glBindVertexArray(VAO[0]);

		setupArray(gl2,0,3,numVertexes,vertexArray);
		if(hasNormals ) setupArray(gl2,1,3,numVertexes,normalArray );
		if(hasColors  ) setupArray(gl2,2,4,numVertexes,colorArray  );
		if(hasTextures) setupArray(gl2,3,2,numVertexes,textureArray);
		
		if(hasIndexes) {
			IntBuffer indexes = IntBuffer.allocate(indexArray.size());
			for (Integer integer : indexArray) indexes.put(integer);
			indexes.rewind();

			gl2.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, VBO[4]);
			gl2.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, (long) indexArray.size() *BYTES_PER_INT, indexes, GL2.GL_STATIC_DRAW);
		}

		gl2.glBindVertexArray(0);
	}

	private void bindArray(GL2 gl2, int attribIndex, int size) {
		gl2.glEnableVertexAttribArray(attribIndex);
		gl2.glBindBuffer(GL.GL_ARRAY_BUFFER, VBO[attribIndex]);
		gl2.glVertexAttribPointer(attribIndex,size,GL2.GL_FLOAT,false,0,0);
	}

	private void setupArray(GL2 gl2, int attribIndex, int size, long numVertexes,List<Float> list) {
		FloatBuffer data = FloatBuffer.allocate(list.size());
		for( Float f : list ) data.put(f);
		data.rewind();

		bindArray(gl2,attribIndex,size);
		gl2.glBufferData(GL2.GL_ARRAY_BUFFER, numVertexes*size*BYTES_PER_FLOAT, data, GL2.GL_STATIC_DRAW);
	}

	public void render(GL2 gl2) {
		if(!isLoaded) {
			isLoaded=true;
			isDirty=true;
		}
		if(isDirty) {
			if(VBO==null) createBuffers(gl2);
			updateBuffers(gl2);
			isDirty=false;
		}

		gl2.glBindVertexArray(VAO[0]);

		bindArray(gl2,0,3);
		if(hasNormals) bindArray(gl2,1,3);
		if(hasColors) bindArray(gl2,2,4);
		if(hasTextures) bindArray(gl2,3,2);

		if (hasIndexes) {
			gl2.glDrawElements(renderStyle, indexArray.size(), GL2.GL_UNSIGNED_INT, 0);
		} else {
			gl2.glDrawArrays(renderStyle, 0, getNumVertices());
		}

		for(int i=0;i<NUM_BUFFERS;++i) {
			gl2.glDisableVertexAttribArray(i);
		}

		gl2.glBindVertexArray(0); // Unbind the VAO
	}
	
	public void drawNormals(GL2 gl2) {
		if(!hasNormals) return;
		
		double scale=2;
		
		gl2.glBegin(GL2.GL_LINES);
		for(int i=0;i<vertexArray.size();i+=3) {
			double px = vertexArray.get(i);
			double py = vertexArray.get(i+1);
			double pz = vertexArray.get(i+2);
			gl2.glVertex3d(px, py, pz);

			double nx = normalArray.get(i);
			double ny = normalArray.get(i+1);
			double nz = normalArray.get(i+2);
			
			gl2.glVertex3d( px + nx*scale, 
							py + ny*scale,
							pz + nz*scale);
		}
		gl2.glEnd();
	}
	
	public void addNormal(float x,float y,float z) {
		normalArray.add(x);
		normalArray.add(y);
		normalArray.add(z);
		hasNormals=true;
	}
	
	public void addVertex(float x,float y,float z) {
		vertexArray.add(x);
		vertexArray.add(y);
		vertexArray.add(z);
	}
	
	public void addColor(float r,float g,float b,float a) {
		colorArray.add(r);
		colorArray.add(g);
		colorArray.add(b);
		colorArray.add(a);
		if(a!=1) isTransparent=true;
		hasColors=true;
	}
	
	public void addTexCoord(float x,float y) {
		textureArray.add(x);
		textureArray.add(y);
		hasTextures =true;
	}
	
	public void addIndex(int n) {
		indexArray.add(n);
		hasIndexes=true;
	}
	
	/**
	 * Force recalculation of the the minimum bounding box to contain this STL file.
	 * Done automatically every time updateBuffers() is called.
	 * Meaningless if there is no vertexArray of points.
	 */
	public void updateCuboid() {
		Point3d boundBottom = new Point3d(Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE);
		Point3d boundTop = new Point3d(-Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE);
		
		// transform and calculate
		Iterator<Float> fi = vertexArray.iterator();
		double x,y,z;
		while(fi.hasNext()) {
			x = fi.next();
			y = fi.next();
			z = fi.next();
			boundTop.x = Math.max(x, boundTop.x);
			boundTop.y = Math.max(y, boundTop.y);
			boundTop.z = Math.max(z, boundTop.z);
			boundBottom.x = Math.min(x, boundBottom.x);
			boundBottom.y = Math.min(y, boundBottom.y);
			boundBottom.z = Math.min(z, boundBottom.z);
		}
		AABB.setBounds(boundTop, boundBottom);
	}

	public AABB getCuboid() {
		return AABB;
	}
	
	public int getNumTriangles() {
		return vertexArray.size()/9;
	}

	public int getNumVertices() {
		return (vertexArray==null) ? 0 : vertexArray.size()/3;
	}

	public Vector3d getVertex(int t) {
		t*=3;
		double x = vertexArray.get(t++); 
		double y = vertexArray.get(t++); 
		double z = vertexArray.get(t++); 
		return new Vector3d(x,y,z);
	}

	public Vector3d getNormal(int t) {
		t*=3;
		double x = normalArray.get(t++);
		double y = normalArray.get(t++);
		double z = normalArray.get(t++);
		return new Vector3d(x,y,z);
	}

	public boolean isDirty() {
		return isDirty;
	}

	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}

	public boolean getHasNormals() {
		return hasNormals;
	}

	public boolean getHasColors() {
		return hasColors;
	}

	public boolean getHasTextures() {
		return hasTextures;
	}

	public boolean getHasIndexes() {
		return hasIndexes;
	}

	public RayHit intersect(Ray ray) {
		if (hasIndexes) {
			return intersectWithIndexes(ray);
		} else {
			return intersectWithoutIndexes(ray);
		}
	}

	public void setRenderStyle(int style) {
		renderStyle = style;
	}

	public int getRenderStyle() {
		return renderStyle;
	}

	private RayHit intersectWithIndexes(Ray ray) {
		int a=0,b=0,c=0;
		double nearest = Double.MAX_VALUE;
		for(int i=0;i<indexArray.size();i+=3) {
			int i0 = indexArray.get(i);
			int i1 = indexArray.get(i+1);
			int i2 = indexArray.get(i+2);
			Vector3d v0 = getVertex(i0);
			Vector3d v1 = getVertex(i1);
			Vector3d v2 = getVertex(i2);
			double t = IntersectionHelper.rayTriangle(ray, v0, v1, v2);
			if(nearest > t) {
				nearest = t;
				a=i0;
				b=i1;
				c=i2;
			}
		}
		if(nearest<Double.MAX_VALUE) {
			Vector3d normal = new Vector3d(getNormal(a));
			normal.add(getNormal(b));
			normal.add(getNormal(c));
			normal.normalize();
			return new RayHit(null,nearest,normal);
		}
		return new RayHit(null,Double.MAX_VALUE,new Vector3d(0,0,0));
	}

	private RayHit intersectWithoutIndexes(Ray ray) {
		int a=0,b=0,c=0;
		double nearest = Double.MAX_VALUE;
		for(int i=0;i<getNumTriangles();i+=3) {
			Vector3d v0 = getVertex(i);
			Vector3d v1 = getVertex(i+1);
			Vector3d v2 = getVertex(i+2);
			double t = IntersectionHelper.rayTriangle(ray, v0, v1, v2);
			if(nearest > t) {
				nearest = t;
				a=i;
				b=i+1;
				c=i+2;
			}
		}
		if(nearest<Double.MAX_VALUE) {
			Vector3d normal = new Vector3d(getNormal(a));
			normal.add(getNormal(b));
			normal.add(getNormal(c));
			normal.normalize();
			return new RayHit(null,nearest,normal);
		}
		return new RayHit(null,Double.MAX_VALUE,new Vector3d(0,0,0));
	}
}
