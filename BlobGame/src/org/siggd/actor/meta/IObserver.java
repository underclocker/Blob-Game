package org.siggd.actor.meta;

import org.siggd.actor.Actor;

/**
 * Interface for an actor that can query input from another actor.
 * @author mysterymath
 *
 */
public interface IObserver {
	@Prop (name = "Target Input")
	public Actor inputSrc();
	
	@Prop (name = "Target Input")
	public void inputSrc(Actor inputSrc);
}
