package org.siggd.editor;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import org.siggd.Game;
import org.siggd.Level;
import org.siggd.actor.Actor;
import org.siggd.actor.meta.ActorEnum;

public class RemoveCommand extends Command {
	private HashMap<String, Object> properties;	//used when actor is recreated
	private long id;	
	private Class c;	//subtype of actor
	/**
	 * 
	 * @param a Actor to be removed.
	 */
	public RemoveCommand(Actor a){
		properties = a.getProperties();
		id = a.getId();
		c = a.getClass();
	}
	
	@Override
	public void doit() {
		Game.get().getLevel().removeActor(id);
	}

	@Override
	public void undo() {
		Actor a = ActorEnum.makeActor(c, Game.get().getLevel(), id);
		if(a!=null){
			a.setProperties(properties);
			Game.get().getLevel().addActor(a);
		}
	}

}
