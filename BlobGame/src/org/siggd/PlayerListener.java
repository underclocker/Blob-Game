package org.siggd;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector3;

public class PlayerListener implements ControllerListener {
	private Player getPlayer(Controller c) {
		return Game.get().getPlayer(c);
	}

	public int indexOf(Controller controller) {
		return Controllers.getControllers().indexOf(controller, true);
	}

	@Override
	public boolean accelerometerMoved(Controller c, int id, Vector3 value) {
		// TODO use?
		return false;
	}

	@Override
	public boolean axisMoved(Controller c, int id, float value) {
		// TODO: change blob's movement here?
		if (Math.abs(value) < .2) {
			// filter out small moves
			return false;
		}
		//DebugOutput.finest(this, "Controller #" + indexOf(c) + ", axis " + id + ": " + value);
		Player p = getPlayer(c);
		if (p == null || p.mActor == null)
			return false;

		if (id == 4) {
			if (value > 0) {
				p.mActor.triggerLeft();
			} else {
				p.mActor.triggerRight();
			}
		}
		return false;
	}

	@Override
	public boolean buttonDown(Controller c, int id) {
		int realId = ControllerFilterAPI.getFilteredId(c, id);
		//DebugOutput.finest(this, "Controller #" + indexOf(c) + ", button " + id + " down"
		//		+ ", masked to " + realId);
		Player p = getPlayer(c);
		if (p == null || p.mActor == null)
			return false;
		p.mActor.downAction(realId);
		return false;
	}

	@Override
	public boolean buttonUp(Controller c, int id) {
		int realId = ControllerFilterAPI.getFilteredId(c, id);
		//DebugOutput.finest(this, "Controller #" + indexOf(c) + ", button " + id + " up"
		//		+ ", masked to " + realId);
		Player p = getPlayer(c);
		if (p == null || p.mActor == null)
			return false;
		p.mActor.upAction(realId);
		return false;
	}

	@Override
	public void connected(Controller c) {
		// doesn't work on desktop (yet) 3/29/2013
	}

	@Override
	public void disconnected(Controller c) {
		// doesn't work on desktop (yet) 3/29/2013
	}

	@Override
	public boolean povMoved(Controller c, int id, PovDirection value) {

		//DebugOutput.finest(this, "Controller #" + indexOf(c) + ", pov " + id + ": " + value);
		// TODO:DPAD

		Player p = getPlayer(c);
		if (p == null || p.mActor == null)
			return false;
		p.mActor.dpad(id, value);

		return false;
	}

	@Override
	public boolean xSliderMoved(Controller c, int id, boolean value) {
		//DebugOutput.finest(this, "Controller #" + indexOf(c) + ", x slider " + id + ": " + value);
		// TODO: what is this?
		return false;
	}

	@Override
	public boolean ySliderMoved(Controller c, int id, boolean value) {
		//DebugOutput.finest(this, "Controller #" + indexOf(c) + ", y slider " + id + ": " + value);
		// TODO: what is this?
		return false;
	}
}
