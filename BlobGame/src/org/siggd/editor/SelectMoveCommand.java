package org.siggd.editor;

import org.siggd.actor.Actor;

import com.badlogic.gdx.math.Vector2;

public class SelectMoveCommand extends Command {
	Actor victim;
	Vector2 start;
	Vector2 end;
	/**
	 * 
	 * @param a	Actor subjected to this action
	 * @param s Start position of the Actor
	 */
	public SelectMoveCommand(Actor a, Vector2 s){
		victim = a;
		start = s;
	}
	/**
	 * Sets the end position of the Actor.
	 * @param e End position
	 */
	public void setEnd(Vector2 e){
		end = e;
	}
	/**
	 * 
	 * @return Actor that is subjected to this command.
	 */
	public Actor getVictim(){
		return victim;
	}
	@Override
	public void doit() {
		victim.setProp("X", end.x);
		victim.setProp("Y", end.y);
	}

	@Override
	public void undo() {
		victim.setProp("X", start.x);
		victim.setProp("Y", start.y);

	}

}
