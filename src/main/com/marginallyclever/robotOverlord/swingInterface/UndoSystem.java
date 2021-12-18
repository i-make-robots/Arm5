package com.marginallyclever.robotOverlord.swingInterface;

import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoManager;

import com.marginallyclever.robotOverlord.swingInterface.actions.RedoAction;
import com.marginallyclever.robotOverlord.swingInterface.actions.UndoAction;

public class UndoSystem {
	private static UndoManager undoManager = new UndoManager();
	private static UndoAction commandUndo = new UndoAction(undoManager);
	private static RedoAction commandRedo = new RedoAction(undoManager);
	
	public void start() {
        commandUndo.setRedoCommand(commandRedo);
    	commandRedo.setUndoCommand(commandUndo);
	}
	
	public static UndoAction getCommandUndo() {
		return commandUndo;
	}

	public static RedoAction getCommandRedo() {
		return commandRedo;
	}

	public static void addEvent(Object src, AbstractUndoableEdit edit) {
		undoManager.undoableEditHappened(new UndoableEditEvent(src,edit));
		getCommandUndo().updateUndoState();
		getCommandRedo().updateRedoState();
	}
}
