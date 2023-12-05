package com.marginallyclever.robotoverlord.swing.actions;

import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.components.shapes.Sphere;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.Project;
import com.marginallyclever.robotoverlord.components.shapes.Box;
import com.marginallyclever.robotoverlord.swing.UnicodeIcon;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.translator.Translator;

import javax.swing.*;
import javax.vecmath.Vector3d;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Clear the world and start anew. This action is not an undoable action.
 * @author Dan Royer
 *
 */
public class ProjectClearAction extends AbstractAction {
	private final Project project;

	public ProjectClearAction(Project project) {
		super(Translator.get("SceneClearAction.name"));
		this.project = project;
		putValue(SMALL_ICON,new UnicodeIcon("🌱"));
		putValue(SHORT_DESCRIPTION, Translator.get("SceneClearAction.shortDescription"));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK) );
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Component source = (Component) e.getSource();
		JFrame parentFrame = (JFrame)SwingUtilities.getWindowAncestor(source);

        int result = JOptionPane.showConfirmDialog(
                parentFrame,
                Translator.get("Are you sure?"),
                (String)this.getValue(AbstractAction.NAME),
                JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
			doIt();
        }
	}

	public void doIt() {
		clearScene();
		UndoSystem.reset();
		addDefaultEntities();
		Clipboard.setSelectedEntity(project.getEntityManager().getRoot());
	}

	public void clearScene() {
		project.clear();
	}

	public void addDefaultEntities() {
		Entity background = new Entity("Background");
		background.addComponent(new Background());
		project.getEntityManager().addEntityToParent(background, project.getEntityManager().getRoot());
		Entity sky = new Entity("Sky");
		Sphere skySphere = new Sphere();
		sky.addComponent(skySphere);
		skySphere.radius.set(-100.0);
		MaterialComponent material = sky.getComponent(MaterialComponent.class);
		material.texture.set("/skybox/industrial_sunset_02_puresky_4k.png");
		material.setLit(false);
		material.drawOnBottom.set(true);
		project.getEntityManager().addEntityToParent(sky, background);
		sky.getComponent(PoseComponent.class).setRotation(new Vector3d(90,0,0));

		Entity mainCamera = new Entity("Main Camera");
		CameraComponent camera = new CameraComponent();
		mainCamera.addComponent(camera);
		PoseComponent pose = mainCamera.getComponent(PoseComponent.class);
		pose.setPosition(new Vector3d(25,20,15));
		camera.lookAt(new Vector3d(0,0,0));
		project.getEntityManager().addEntityToParent(mainCamera, project.getEntityManager().getRoot());

		Entity light0 = new Entity("Light");
		light0.addComponent(pose = new PoseComponent());
		light0.addComponent(new LightComponent());
		project.getEntityManager().addEntityToParent(light0,mainCamera);
		pose.setPosition(new Vector3d(0,0,50));

		Entity box = new Entity("box");
		box.addComponent(new Box());
		project.getEntityManager().addEntityToParent(box, project.getEntityManager().getRoot());
	}
}
