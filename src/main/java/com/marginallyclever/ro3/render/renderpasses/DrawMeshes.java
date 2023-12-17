package com.marginallyclever.ro3.render.renderpasses;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLContext;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.MeshInstance;
import com.marginallyclever.ro3.render.RenderPass;
import com.marginallyclever.robotoverlord.systems.render.ShaderProgram;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import java.util.ArrayList;
import java.util.List;

public class DrawMeshes implements RenderPass {
    private static final Logger logger = LoggerFactory.getLogger(DrawMeshes.class);
    private int activeStatus = ALWAYS;

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
        return "Meshes";
    }

    @Override
    public void draw(ShaderProgram shader) {
        GL3 gl3 = GLContext.getCurrentGL().getGL3();

        // find all MeshInstance nodes in Registry
        List<Node> toScan = new ArrayList<>(Registry.scene.getChildren());
        while(!toScan.isEmpty()) {
            Node node = toScan.remove(0);

            if(node instanceof MeshInstance meshInstance) {
                // if they have a mesh, draw it.
                Mesh mesh = meshInstance.getMesh();
                if(mesh==null) continue;

                final boolean[] hasTexture = {false};
                // set the texture to the first sibling that is a material and has a texture
                meshInstance.getParent().getChildren().stream()
                        .filter(n -> n instanceof Material)
                        .map(n -> (Material) n)
                        .filter(m -> m.getTexture() != null)
                        .findFirst()
                        .ifPresent(m -> {
                            m.getTexture().use(shader);
                            hasTexture[0] = true;
                        });
                if(!hasTexture[0]) {
                    gl3.glDisable(GL3.GL_TEXTURE_2D);
                    shader.set1i(gl3,"useTexture",0);
                }

                // set the model matrix
                Matrix4d w = meshInstance.getWorld();
                w.transpose();
                shader.setMatrix4d(gl3,"modelMatrix",w);
                // draw it
                mesh.render(gl3);

                OpenGLHelper.checkGLError(gl3,logger);
            }

            toScan.addAll(node.getChildren());
        }
    }
}
