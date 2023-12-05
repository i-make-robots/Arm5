package com.marginallyclever.robotoverlord.components;

import com.github.sarxos.webcam.Webcam;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Projects one camera feed into the world on a flat surface.
 * @author Dan Royer
 * @since 2.10.0
 */
public class ProjectorComponent  extends ShapeComponent {
    private BufferedImage image;
    private Texture texture;
    private MaterialComponent myMaterial;
    private final Webcam webcam = Webcam.getDefault();

    public ProjectorComponent() {
        super();

        myMesh = new Mesh();
        webcam.open();
    }

    @Override
    public void render(GL3 gl) {
        if(myMaterial == null) {
            myMaterial = this.getEntity().getComponent(MaterialComponent.class);
        }
        updateTexture(gl);
        if(myMaterial != null && texture!=null) {
            myMaterial.texture.setTexture(texture);
        }

        myMesh.setRenderStyle(GL3.GL_QUADS);
        int v = 50;
        myMesh.addVertex(-v, -v, 0);
        myMesh.addVertex(v, -v, 0);
        myMesh.addVertex(v, v, 0);
        myMesh.addVertex(-v, v, 0);
        myMesh.addTexCoord(0, 0);
        myMesh.addTexCoord(1, 0);
        myMesh.addTexCoord(1, 1);
        myMesh.addTexCoord(0, 1);

        super.render(gl);
    }

    private void updateTexture(GL3 gl) {
        GLProfile profile = gl.getGLProfile();
        if(profile==null) return;

        image = captureFrame();

        if (texture == null) {
            texture = AWTTextureIO.newTexture(profile, image, false);
        } else {
            texture.updateImage(gl, AWTTextureIO.newTextureData(profile, image, false));
        }
    }

    /**
     * @return one frame of video from the default camera.
     */
    private BufferedImage captureFrame() {
        return webcam.getImage();
    }
}
