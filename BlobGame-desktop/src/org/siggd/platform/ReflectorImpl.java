package org.siggd.platform;

import java.util.Set;

import org.reflections.Reflections;
import org.siggd.actor.Actor;

public class ReflectorImpl implements Reflector {
	public Set<Class<? extends Actor>> getActorSubTypes() {
		Reflections r = new Reflections("org.siggd.actor");
		return r.getSubTypesOf(Actor.class);
	}
}
