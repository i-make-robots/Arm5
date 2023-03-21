package com.marginallyclever.robotoverlord.swinginterface;

import com.marginallyclever.robotoverlord.swinginterface.actions.RedoAction;
import com.marginallyclever.robotoverlord.swinginterface.actions.UndoAction;

import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoManager;

/**
 * A singleton to manage undo/redo actions.
 * @author Dan Royer
 */
public class UndoSystem {
	private static final UndoManager undoManager = new UndoManager();
	private static final UndoAction commandUndo = new UndoAction(undoManager);
	private static final RedoAction commandRedo = new RedoAction(undoManager);

	/**
	 * Start the undo system.  This is called by the main frame after the menu bar is created.
	 */
	public static void start() {
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

	public static void reset() {
		undoManager.discardAllEdits();
		getCommandUndo().updateUndoState();
		getCommandRedo().updateRedoState();
	}
}
