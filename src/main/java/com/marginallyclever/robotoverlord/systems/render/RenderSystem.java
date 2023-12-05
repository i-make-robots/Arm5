package com.marginallyclever.robotoverlord.systems.render;

import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.components.program.ProgramPathComponent;
import com.marginallyclever.robotoverlord.components.shapes.*;
import com.marginallyclever.robotoverlord.parameters.TextureParameter;
import com.marginallyclever.robotoverlord.parameters.swing.ComponentSwingViewFactory;
import com.marginallyclever.robotoverlord.renderpanel.TextureFactory;
import com.marginallyclever.robotoverlord.systems.EntitySystem;
import com.marginallyclever.robotoverlord.systems.render.gcodepath.PathFactory;
import com.marginallyclever.robotoverlord.systems.render.mesh.load.MeshFactory;

import javax.swing.filechooser.FileFilter;
import java.util.ArrayList;

/**
 * A system that decorates and manages various {@link RenderComponent}s.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class RenderSystem implements EntitySystem {
    /**
     * Get the Swing view of this component.
     *
     * @param view      the factory to use to create the panel
     * @param component the component to visualize
     */
    @Override
    public void decorate(ComponentSwingViewFactory view, Component component) {
        if(component instanceof CameraComponent) decorateCamera(view,component);
        if(component instanceof RenderComponent) decorateRender(view, component);
        if(component instanceof GCodePathComponent) decorateGCodePath(view, component);
        if(component instanceof MeshFromFile) decorateMeshFromFile(view, component);
        if(component instanceof Sphere) decorateSphere(view,component);
        if(component instanceof Grid) decorateGrid(view,component);
        if(component instanceof Box) decorateBox(view, component);
        if(component instanceof Cylinder) decorateCylinder(view, component);
        if(component instanceof Decal) decorateDecal(view, component);
        if(component instanceof LightComponent) decorateLight(view,component);
        if(component instanceof MaterialComponent) decorateMaterial(view,component);
        if(component instanceof LinearPatternComponent) decorateLinearPattern(view,component);

        if(component instanceof ProgramPathComponent) decoratePath(view, component);
        else if(component instanceof ShapeComponent) decorateShape(view, component);
    }

    private void decorateCamera(ComponentSwingViewFactory view, Component component) {
        CameraComponent camera = (CameraComponent)component;
        view.add(camera.orbitDistance).setReadOnly(true);
        view.add(camera.pan).setReadOnly(true);
        view.add(camera.tilt).setReadOnly(true);
    }

    private void decorateRender(ComponentSwingViewFactory view, Component component) {
        RenderComponent renderComponent = (RenderComponent) component;
        view.add(renderComponent.isVisible);
    }

    private void decorateBox(ComponentSwingViewFactory view, Component component) {
        Box boxComponent = (Box) component;
        view.add(boxComponent.width);
        view.add(boxComponent.length);
        view.add(boxComponent.height);
    }

    private void decorateCylinder(ComponentSwingViewFactory view, Component component) {
        Cylinder cylinderComponent = (Cylinder) component;
        view.add(cylinderComponent.height);
        view.add(cylinderComponent.radius0);
        view.add(cylinderComponent.radius1);
    }

    private void decorateShape(ComponentSwingViewFactory view, Component component) {
        ShapeComponent shapeComponent = (ShapeComponent) component;
        view.add(shapeComponent.numVertices).setReadOnly(true);
        view.add(shapeComponent.hasNormals).setReadOnly(true);
        view.add(shapeComponent.hasColors).setReadOnly(true);
        view.add(shapeComponent.hasUVs).setReadOnly(true);
    }

    public void decorateGCodePath(ComponentSwingViewFactory view, Component component) {
        GCodePathComponent pathComponent = (GCodePathComponent)component;
        ArrayList<FileFilter> filters = PathFactory.getAllExtensions();
        view.addFilename(pathComponent.filename,filters);
        view.addButton("Reload").addActionEventListener(e -> pathComponent.reload());
        view.add(pathComponent.numCommands).setReadOnly(true);
        view.add(pathComponent.distanceMeasured).setReadOnly(true);
        view.add(pathComponent.getCommand).addPropertyChangeListener((e)->pathComponent.updateLocation());
    }

    public void decoratePath(ComponentSwingViewFactory view, Component component) {
        ProgramPathComponent programPathComponent = (ProgramPathComponent) component;
        ArrayList<FileFilter> filters = PathFactory.getAllExtensions();
        view.add(programPathComponent.moveSpeed);
        view.addComboBox(programPathComponent.moveType, ProgramPathComponent.MOVE_TYPE_NAMES);
    }

    public void decorateLinearPattern(ComponentSwingViewFactory view, Component component) {
        LinearPatternComponent patternComponent = (LinearPatternComponent) component;
        view.addComboBox(patternComponent.spacingType, LinearPatternComponent.SPACING_TYPE_NAMES);
        view.add(patternComponent.measure);
        view.add(patternComponent.quantity);
    }

    private void decorateMeshFromFile(ComponentSwingViewFactory view, Component component) {
        MeshFromFile meshComponent = (MeshFromFile) component;
        view.addFilename(meshComponent.filename,MeshFactory.getAllExtensions());
        meshComponent.filename.addPropertyChangeListener(e -> meshComponent.load());
        view.addButton("Reload").addActionEventListener(e -> meshComponent.reload());
    }

    private void decorateSphere(ComponentSwingViewFactory view, Component component) {
        Sphere sphereComponent = (Sphere) component;
        view.add(sphereComponent.detail);
        view.add(sphereComponent.radius);
    }

    private void decorateDecal(ComponentSwingViewFactory view, Component component) {
        Decal decalComponent = (Decal) component;
        view.add(decalComponent.height);
        view.add(decalComponent.width);
    }

    private void decorateGrid(ComponentSwingViewFactory view, Component component) {
        Grid sphereComponent = (Grid) component;
        view.add(sphereComponent.width);
        view.add(sphereComponent.length);
        view.add(sphereComponent.snap);
    }

    private void decorateLight(ComponentSwingViewFactory view, Component component) {
        LightComponent light = (LightComponent)component;
        view.add(light.isDirectional);
        view.addComboBox(light.preset, LightComponent.PRESET_NAMES);
        view.add(light.diffuse);
        view.add(light.specular);
        view.add(light.ambient);
        view.add(light.cutoff);
        view.add(light.exponent);
        view.add(light.attenuationConstant);
        view.add(light.attenuationLinear);
        view.add(light.attenuationQuadratic);
    }

    /**
     * Get the Swing view of this component.
     *
     * @param view      the factory to use to create the panel
     * @param component the component to visualize
     */
    public void decorateMaterial(ComponentSwingViewFactory view, Component component) {
        MaterialComponent material = (MaterialComponent)component;
        view.add(material.isLit);
        view.add(material.emission);
        view.add(material.ambient);
        view.add(material.diffuse);
        view.add(material.specular);
        view.addRange(material.shininess, 128, 0);
        view.add(material.drawOnTop);
        view.add(material.drawOnBottom);
        view.addFilename(material.texture, TextureFactory.getFilters());
    }

    /**
     * Update the system over time.
     * @param dt the time step in seconds.
     */
    public void update(double dt) {}
}
