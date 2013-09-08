package org.siggd.editor;

import org.siggd.Game;
import org.siggd.actor.Actor;
import org.siggd.actor.meta.ActorEnum;

public class AddCommand extends Command {
	private Class crass;	//type of actor
	private Actor mActor;	//be careful that it isn't null
	private long id;		//used for undo
	private float mX;
	private float mY;
	
	public AddCommand(Class c, float x, float y){
		crass = c;
		mX = x;
		mY = y;
	}
	
	@Override
	public void doit() {
		mActor = ActorEnum.makeActor(crass, Game.get().getLevel(), Game.get().getLevel().getId());
		mActor.loadBodies();
		mActor.setProp("X", mX);
		mActor.setProp("Y", mY);
		id = mActor.getId();
		Game.get().getLevel().addActor(mActor);
		Game.get().getLevel().flushActorQueue();
	}
	
	@Override
	public void undo() {
		Game.get().getLevel().removeActor(id);
	}
	public Actor getActor(){
		return mActor;
	}
}
