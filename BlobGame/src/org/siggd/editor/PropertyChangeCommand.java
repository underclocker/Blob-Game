package org.siggd.editor;

import org.siggd.actor.Actor;
import org.siggd.actor.Background;

public class PropertyChangeCommand extends Command {
	private Actor target;
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
	public PropertyChangeCommand(Actor a, String k, Object start, Object end){
		target = a;
		key = k;
		startValue = start;
		endValue = end;
		
	}
	
	@Override
	public void doit() {
		if (target instanceof Background) {
			((Background)target).setInEditor();
		}
		target.setProp(key, endValue);
	}

	@Override
	public void undo() {
		target.setProp(key, startValue);
	}

}
