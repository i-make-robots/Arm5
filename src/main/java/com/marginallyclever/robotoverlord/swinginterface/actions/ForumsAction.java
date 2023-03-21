package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;

/**
 * Go to the online help forums. This action is not undoable.
 * @author Admin
 *
 */
public class ForumsAction extends AbstractAction implements ActionListener {
	private final String FORUM_URL = "https://discord.gg/p2NRZFxXbh";
	
	public ForumsAction() {
		super(Translator.get("Online help"));
        putValue(SHORT_DESCRIPTION, Translator.get("Go to the forums"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			java.awt.Desktop.getDesktop().browse(URI.create(this.FORUM_URL));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
