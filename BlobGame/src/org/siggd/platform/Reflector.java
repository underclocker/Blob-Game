package org.siggd.platform;

import java.util.Set;

import org.siggd.actor.Actor;

public interface Reflector {
	public Set<Class<? extends Actor>> getActorSubTypes();
}
