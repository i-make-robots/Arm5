package com.marginallyclever.robotoverlord.swinginterface.edits;

import com.marginallyclever.robotoverlord.parameters.AbstractParameter;

/**
 * Undoable action to select a boolean.
 * <p>
 * Some Entities have string (text) parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class BooleanEdit extends AbstractParameterEdit<Boolean> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BooleanEdit(AbstractParameter<Boolean> e, Boolean newValue) {
		super(e, newValue);
	}
}
