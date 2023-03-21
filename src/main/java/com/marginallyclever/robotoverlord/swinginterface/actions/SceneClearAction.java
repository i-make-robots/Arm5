package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.Scene;
import com.marginallyclever.robotoverlord.UnicodeIcon;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.LightComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.*;
import javax.swing.undo.UndoManager;
import javax.vecmath.Vector3d;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Clear the world and start anew. This action is not an undoable action.
 * @author Dan Royer
 *
 */
public class SceneClearAction extends AbstractAction {
	private final RobotOverlord ro;

	public SceneClearAction(RobotOverlord ro) {
		super(Translator.get("SceneClearAction.name"));
		this.ro = ro;
		putValue(Action.SMALL_ICON,new UnicodeIcon("🌱"));
		putValue(Action.SHORT_DESCRIPTION, Translator.get("SceneClearAction.shortDescription"));
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK) );
	}

	@Override
	public void actionPerformed(ActionEvent e) {
        int result = JOptionPane.showConfirmDialog(
                ro.getMainFrame(),
                Translator.get("Are you sure?"),
                (String)this.getValue(AbstractAction.NAME),
                JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
			clearScene();
			UndoSystem.reset();
			addDefaultEntities();
        }
	}

	public void clearScene() {
		Scene scene = ro.getScene();
		scene.removeAllEntities();
	}

	public void addDefaultEntities() {
		Scene scene = ro.getScene();
		PoseComponent pose = new PoseComponent();
		CameraComponent camera = new CameraComponent();
		scene.addComponent(new PoseComponent());
		Entity mainCamera = new Entity("Main Camera");
		mainCamera.addComponent(pose);
		mainCamera.addComponent(camera);
		scene.addEntity(mainCamera);
		pose.setPosition(new Vector3d(0,-10,-5));
		camera.lookAt(new Vector3d(0,0,0));

		Entity light0 = new Entity("Light");
		light0.addComponent(pose = new PoseComponent());
		light0.addComponent(new LightComponent());
		mainCamera.addEntity(light0);
		pose.setPosition(new Vector3d(0,0,50));
	}
}
