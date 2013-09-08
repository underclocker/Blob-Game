package org.siggd.editor;

/**
 * Represents an Editor action, for the purposes of Undo/Redo
 *  
 * @author mysterymath
 *
 */
public abstract class Command {
	public Command() {
		
	}
	
	/**
	 * Perform the action
	 */
	public abstract void doit();
	
	/**
	 * Un-perform the action
	 */
	public abstract void undo();
}
