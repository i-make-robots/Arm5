package com.marginallyclever.robotoverlord.tools;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotoverlord.Viewport;
import com.marginallyclever.robotoverlord.components.CameraComponent;

import javax.vecmath.Point3d;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * Interface for tools that can be used to visually edit the scene.
 * @since 2.3.0
 * @author Dan Royer
 */
public interface EditorTool {

    /**
     * This method is called when the tool is activated. It receives the SelectedItems object containing the selected
     * entities and their initial world poses.
     *
     * @param selectedItems The selected items to be manipulated by the tool.
     */
    void activate(SelectedItems selectedItems);

    /**
     * This method is called when the tool is deactivated. It allows the tool to perform any necessary cleanup
     * actions before another tool takes over.
     */
    void deactivate();


    /**
     * Handles mouse input events for the tool.
     *
     * @param event The MouseEvent object representing the input event.
     */
    public void handleMouseEvent(MouseEvent event);

    /**
     * Handles keyboard input events for the tool.
     *
     * @param event The KeyEvent object representing the input event.
     */
    public void handleKeyEvent(KeyEvent event);

    /**
     * Updates the tool's internal state, if necessary.
     *
     * @param deltaTime Time elapsed since the last update.
     */
    void update(double deltaTime);

    /**
     * Renders any tool-specific visuals to the 3D scene.
     */
    void render(GL2 gl2);

    void setViewport(Viewport viewport);

    boolean isInUse();

    void cancelUse();

    Point3d getStartPoint();
}
