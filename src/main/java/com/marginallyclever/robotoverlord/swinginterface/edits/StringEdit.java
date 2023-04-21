package com.marginallyclever.robotoverlord.swinginterface.edits;

import com.marginallyclever.robotoverlord.parameters.AbstractParameter;

/**
 * Undoable action to select a string.
 * <p>
 * Some Entities have string (text) parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class StringEdit extends AbstractEntityEdit<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public StringEdit(AbstractParameter<String> e, String newValue) {
		super(e, newValue);
	}
}
