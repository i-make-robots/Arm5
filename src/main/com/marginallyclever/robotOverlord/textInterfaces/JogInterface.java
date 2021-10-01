package com.marginallyclever.robotOverlord.textInterfaces;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.vecmath.Matrix4d;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.robots.sixi3.ApproximateJacobian;
import com.marginallyclever.robotOverlord.robots.sixi3.Sixi3IK;

public class JogInterface extends JPanel {
	private static final long serialVersionUID = 1L;
	private Sixi3IK mySixi3;
	private CartesianReportPanel eeReport, eeTargetReport;

	public JogInterface(Sixi3IK sixi3) {
		super();
		
		mySixi3 = sixi3;
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.NORTHWEST;

		c.weightx = 1;
		this.add(new AngleReportPanel(mySixi3), c);
		c.gridx++;
		c.weightx = 0;
		this.add(new AngleDrivePanel(mySixi3), c);
		c.gridx--;
		c.gridy++;
		c.weightx = 1;
		this.add(eeReport=new CartesianReportPanel("RobotUI.EndEffector"), c);
		c.gridy++;
		this.add(eeTargetReport=new CartesianReportPanel("RobotUI.EndEffectorTarget"), c);
		c.gridy--;
		c.gridx++;
		c.gridheight=2;
		c.weightx = 0;
		this.add(new CartesianDrivePanel(mySixi3), c);
		c.gridheight=1;
		c.gridx--;
		c.gridy+=2;
		c.gridwidth = 2;
		c.weightx = 1;
		this.add(new JacobianReportPanel(mySixi3), c);
		c.gridy++;
		c.weighty = 1;
		this.add(new JPanel(), c);

		mySixi3.addPropertyChangeListener( (e)-> updateReports() );
		
		updateReports();
	}
	
	private void updateReports() {
		Matrix4d m0=mySixi3.getEndEffector();
		eeReport.updateReport(m0);
		Matrix4d m1=mySixi3.getEndEffectorTarget();
		eeTargetReport.updateReport(m1);
		double [] cartesianDistance = MatrixHelper.getCartesianBetweenTwoMatrixes(m0, m1);
		ApproximateJacobian aj = mySixi3.getApproximateJacobian();
		try {
			double [] jointDistance = aj.getJointFromCartesian(cartesianDistance);
			System.out.println(jointDistance.toString());
		} catch(Exception e) {
			System.out.println("Failed to calculate jointDistance.");
		}
	}


	public static void main(String[] args) {
		Log.start();
		JFrame frame = new JFrame("JogControlPanel");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {}
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new JogInterface(new Sixi3IK()));
		frame.pack();
		frame.setVisible(true);
	}
}
