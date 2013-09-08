package org.siggd.actor.meta;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Set;

import org.reflections.Reflections;
import org.siggd.Level;
import org.siggd.actor.Actor;

/**
 * This class enumerates the available objects, and creates a "dummy" object of
 * each
 * 
 * @author mysterymath
 * 
 */
public class ActorEnum {
	// A fake level for the dummy objects
	private Level mFakeLevel;

	/**
	 * Constructor
	 */
	public ActorEnum() {
		// Create fake world
		mFakeLevel = new Level(null);

		// Enumerate actors
		Reflections r = new Reflections("org.siggd.actor");
		Set<Class<? extends Actor>> s = r.getSubTypesOf(Actor.class);

		// Create one of each type of actor
		for (Class c : s) {
			mFakeLevel.addActor(makeActor(c, mFakeLevel, mFakeLevel.getId()));
		}
		mFakeLevel.flushActorQueue();
	}

	/**
	 * Gets the default properties of the specified actor class
	 * 
	 * @param c
	 *            The class to get properties for, use getClass()
	 * @return The requested properties, or null if the specified class does not
	 *         exist
	 */
	public HashMap<String, Object> getProperties(Class c) {
		for (Actor a : mFakeLevel.getActors()) {
			if (a.getClass().equals(c)) {
				return a.getProperties();
			}
		}
		// This type of actor does not exist in the Enum
		return null;
	}

	/**
	 * Loads all resources needed by the actors
	 */
	public void loadActorResources() {
		mFakeLevel.loadResources();
	}

	/**
	 * 
	 * @param c
	 *            Type of Actor requested
	 * @return Actor of the requested type in the fakeLevel
	 */
	public Actor getActor(Class c) {
		for (Actor a : mFakeLevel) {
			if (a.getClass().equals(c)) {
				return a;
			}
		}
		return null;
	}

	/**
	 * Makes an actor from a Class object
	 */
	public static Actor makeActor(Class c, Level l, long id) {
		Constructor cons = null;
		try {
			cons = c.getConstructor(Level.class, long.class);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Error: Cannot find construcor for Actor: " + c.getName());
		}

		try {
			return (Actor) cons.newInstance(l, id);
		} catch (Exception e) {
			throw new RuntimeException("Error: Unable to instantiate Actor: " + c.getName());
		}
	}

	/**
	 * Get the fake level within the actor enumerator
	 * 
	 * @return the fake level
	 * @return
	 */
	public Level getFakeLevel() {
		return mFakeLevel;
	}
}
