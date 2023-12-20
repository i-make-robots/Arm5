package com.marginallyclever.ro3.render.renderpasses;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.ResourceHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.nodes.Camera;
import com.marginallyclever.ro3.render.RenderPass;
import com.marginallyclever.robotoverlord.systems.render.ShaderProgram;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

/**
 * Draws each {@link Camera} as a pyramid approximating the perspective view frustum.
 */
public class DrawCameras implements RenderPass {
    private static final Logger logger = LoggerFactory.getLogger(DrawCameras.class);
    private int activeStatus = ALWAYS;
    private final Mesh mesh = new Mesh();
    private ShaderProgram shader;
    private int canvasWidth,canvasHeight;

    public DrawCameras() {
        // add mesh to a list that can be unloaded and reloaded as needed.
        mesh.setRenderStyle(GL3.GL_LINES);
        Vector3d a = new Vector3d(-1,-1,-1);
        Vector3d b = new Vector3d( 1,-1,-1);
        Vector3d c = new Vector3d( 1, 1,-1);
        Vector3d d = new Vector3d(-1, 1,-1);
        mesh.addColor(0,0,0,1);        mesh.addVertex(0,0,0);        mesh.addColor(0,0,0,1);        mesh.addVertex((float)a.x, (float)a.y, (float)a.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex(0,0,0);        mesh.addColor(0,0,0,1);        mesh.addVertex((float)b.x, (float)b.y, (float)b.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex(0,0,0);        mesh.addColor(0,0,0,1);        mesh.addVertex((float)c.x, (float)c.y, (float)c.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex(0,0,0);        mesh.addColor(0,0,0,1);        mesh.addVertex((float)d.x, (float)d.y, (float)d.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex((float)a.x, (float)a.y, (float)a.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex((float)b.x, (float)b.y, (float)b.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex((float)b.x, (float)b.y, (float)b.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex((float)c.x, (float)c.y, (float)c.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex((float)c.x, (float)c.y, (float)c.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex((float)d.x, (float)d.y, (float)d.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex((float)d.x, (float)d.y, (float)d.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex((float)a.x, (float)a.y, (float)a.z);
    }

    /**
     * @return NEVER, SOMETIMES, or ALWAYS
     */
    @Override
    public int getActiveStatus() {
        return activeStatus;
    }

    /**
     * @param status NEVER, SOMETIMES, or ALWAYS
     */
    @Override
    public void setActiveStatus(int status) {
        activeStatus = status;
    }

    /**
     * @return the localized name of this overlay
     */
    @Override
    public String getName() {
        return "Cameras";
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL3 gl3 = glAutoDrawable.getGL().getGL3();
        try {
            shader = new ShaderProgram(gl3,
                    ResourceHelper.readResource(this.getClass(), "mesh.vert"),
                    ResourceHelper.readResource(this.getClass(), "mesh.frag"));
        } catch(Exception e) {
            logger.error("Failed to load shader", e);
        }
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {
        GL3 gl3 = glAutoDrawable.getGL().getGL3();
        shader.delete(gl3);
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height)  {
        canvasWidth = width;
        canvasHeight = height;
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {}

    @Override
    public void draw() {
        Camera camera = Registry.getActiveCamera();
        if(camera==null) return;

        GL3 gl3 = GLContext.getCurrentGL().getGL3();
        shader.use(gl3);
        shader.setMatrix4d(gl3,"projectionMatrix",camera.getChosenProjectionMatrix(canvasWidth,canvasHeight));
        shader.setMatrix4d(gl3,"viewMatrix",camera.getViewMatrix());
        Vector3d cameraWorldPos = MatrixHelper.getPosition(camera.getWorld());
        shader.setVector3d(gl3,"cameraPos",cameraWorldPos);  // Camera position in world space
        shader.setVector3d(gl3,"lightPos",cameraWorldPos);  // Light position in world space
        shader.setVector3d(gl3,"lightColor",new Vector3d(1,1,1));  // Light color
        shader.set4f(gl3,"objectColor",1,1,1,1);
        shader.setVector3d(gl3,"specularColor",new Vector3d(0.5,0.5,0.5));
        shader.setVector3d(gl3,"ambientLightColor",new Vector3d(0.2,0.2,0.2));
        shader.set1i(gl3,"useVertexColor",1);
        shader.set1i(gl3,"useLighting",0);
        shader.set1i(gl3,"diffuseTexture",0);
        gl3.glDisable(GL3.GL_DEPTH_TEST);
        gl3.glDisable(GL3.GL_TEXTURE_2D);

        for(Camera cam : Registry.cameras.getList() ) {
            // set modelView to world
            Matrix4d w = cam.getWorld();
            w.transpose();
            shader.setMatrix4d(gl3,"modelMatrix",w);
            mesh.render(gl3);
        }

        gl3.glEnable(GL3.GL_DEPTH_TEST);
    }
}
