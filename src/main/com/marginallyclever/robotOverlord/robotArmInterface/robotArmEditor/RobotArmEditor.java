package com.marginallyclever.robotOverlord.robotArmInterface.robotArmEditor;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.robots.sixi3.Sixi3FK;

public class RobotArmEditor extends JPanel {
	private static final long serialVersionUID = 1L;
	private Sixi3FK myArm;

	public RobotArmEditor(Sixi3FK arm) {
		super();
		myArm = arm;
		
		buildAll();
	}
	
	private void buildAll() {
		JTabbedPane tabs = new JTabbedPane();
		for(int i=0;i<myArm.getNumBones();++i) {
			tabs.add(Integer.toString(i), new RobotArmBoneEditorPanel(myArm.getBone(i)));
		}
		
		this.setLayout(new java.awt.BorderLayout());
		this.add(tabs,java.awt.BorderLayout.CENTER);
	}

	public static void main(String[] args) {
		Log.start();
		JFrame frame = new JFrame("RobotArmEditor");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {}
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new RobotArmEditor(new Sixi3FK()));
		frame.pack();
		frame.setVisible(true);
	}
}
