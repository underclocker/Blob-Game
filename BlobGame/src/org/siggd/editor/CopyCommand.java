package org.siggd.editor;

import java.util.HashMap;

import org.siggd.Game;
import org.siggd.actor.Actor;
import org.siggd.actor.meta.ActorEnum;

import com.badlogic.gdx.math.Vector2;

public class CopyCommand extends Command {
	
	private HashMap<String, Object> properties;	//used when actor is recreated
	private long id;
	private Actor mCopy;		//copy of the given actor
	private Class classage;
	/**
	 * 
	 * @param a Actor to be copied when ctrl and mouse is clicked and dragged.
	 */
	public CopyCommand(Actor ack){
		properties = ack.getProperties();
		classage = ack.getClass();
	}
	
	@Override
	public void doit() {
		mCopy = ActorEnum.makeActor(classage, Game.get().getLevel(), Game.get().getLevel().getId());
		id = mCopy.getId();
		if(properties.containsKey("Body")){
			mCopy.setProp("Body", properties.get("Body"));
		}
		mCopy.loadBodies();
		for(String s : properties.keySet()){
			mCopy.setProp(s, properties.get(s));		//apply old actor's properties to new actor
		}
		mCopy.getProperties().put("ID", id);			//Forcing the id of the new actor to change because it should not be the same
		Game.get().getLevel().addActor(mCopy);
		Game.get().getLevel().flushActorQueue();
	}
	
	@Override
	public void undo() {
		Game.get().getLevel().removeActor(id);
	}
	
	public Actor getActor(){
		return mCopy;
	}

}
