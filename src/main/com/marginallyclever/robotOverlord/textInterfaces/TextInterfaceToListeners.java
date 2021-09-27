package com.marginallyclever.robotOverlord.textInterfaces;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class TextInterfaceToListeners extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7996257740483513358L;
	private JTextField commandLine = new JTextField(60);
	private JButton send = new JButton("Send");
		
	public TextInterfaceToListeners() {
		setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.fill= GridBagConstraints.HORIZONTAL;
		c.weightx=1;
		add(commandLine,c);
		
		c.gridx=1;
		c.fill=GridBagConstraints.NONE;
		c.weightx=0;
		add(send,c);
		
		commandLine.addActionListener((e)->sendNow());
		send.addActionListener((e)->sendNow());
	}

	public void sendCommand(String str) {
		notifyListeners(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,str));
	}
	
	public void sendNow() {
		sendCommand(commandLine.getText());
		commandLine.setText("");
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("TextInterfaceToListeners");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {}
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new TextInterfaceToListeners());
		frame.pack();
		frame.setVisible(true);
	}

	public void setCommand(String msg) {
		commandLine.setText(msg);
	}
	
	public String getCommand() {
		return commandLine.getText();
	}
	
	@Override
	public void setEnabled(boolean state) {
		super.setEnabled(state);
		commandLine.setEnabled(state);
		send.setEnabled(state);
	}

	// OBSERVER PATTERN

	private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
	public void addActionListener(ActionListener a) {
		listeners.add(a);
	}
	
	public void removeActionListener(ActionListener a) {
		listeners.remove(a);
	}
	
	private void notifyListeners(ActionEvent e) {
		for( ActionListener a : listeners ) {
			a.actionPerformed(e);
		}
	}
}
