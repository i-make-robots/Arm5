package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * go back one step in the undo/redo history.
 * @author Dan Royer
 */
public class UndoAction extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(UndoAction.class);
	private final UndoManager undo;
	private RedoAction redoAction;
	
    public UndoAction(UndoManager undo) {
        super(Translator.get("UndoAction.name"));
    	this.undo=undo;
        setEnabled(false);
        
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
    }

	@Override
    public void actionPerformed(ActionEvent e) {
        try {
            undo.undo();
        } catch (CannotUndoException ex) {
            logger.info("Unable to undo: " + ex);
            ex.printStackTrace();
        }
        updateUndoState();
        if(redoAction!=null) redoAction.updateRedoState();
    }

    public void updateUndoState() {
        if (undo.canUndo()) {
            setEnabled(true);
            putValue(Action.NAME, undo.getUndoPresentationName());
        } else {
            setEnabled(false);
            putValue(Action.NAME, "Undo");
        }
    }
    
    public void setRedoCommand(RedoAction redoCommand) {
    	this.redoAction=redoCommand;
    }
}
