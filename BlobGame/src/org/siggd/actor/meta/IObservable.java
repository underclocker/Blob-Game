package org.siggd.actor.meta;

/**
 * Interface for an Actor that can exposes observable information.
 * @author mysterymath
 *
 */
public interface IObservable {
	/**
	 * Returns an observation of this object
	 * @return An object representing the observation
	 */
	public Object observe();
}
