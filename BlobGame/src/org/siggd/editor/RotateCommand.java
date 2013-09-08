package org.siggd.editor;

import org.siggd.Convert;
import org.siggd.actor.Actor;

public class RotateCommand extends Command {
	private float mDegrees;
	private Actor mActor;

	public RotateCommand(Actor actor, float degrees) {
		mActor = actor;
		mDegrees = degrees;
	}

	@Override
	public void doit() {
		float a = Convert.getFloat(mActor.getProp("Angle"))+mDegrees;
		mActor.setProp("Angle", a);

	}

	@Override
	public void undo() {
		float a = Convert.getFloat(mActor.getProp("Angle"))-mDegrees;
		mActor.setProp("Angle", a);
	}

}
