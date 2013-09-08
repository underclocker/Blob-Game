package org.siggd.editor;

import org.siggd.Game;

public class WorldPropertyChangeCommand extends Command {
	private String key;
	private Object startValue;
	private Object endValue;
	
	/**
	 * A general Command to change the value of one property.
	 * @param a Target actor
	 * @param k Name or key of the prop
	 * @param start Value of the property before change
	 * @param end Desired final value for the property
	 */
	public WorldPropertyChangeCommand(String k, Object start, Object end){
		key = k;
		startValue = start;
		endValue = end;
		
	}
	
	@Override
	public void doit() {
		Game.get().getLevel().setProp(key, endValue);
	}

	@Override
	public void undo() {
		Game.get().getLevel().setProp(key, startValue);
	}

}
