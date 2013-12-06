package org.siggd;

import com.badlogic.gdx.controllers.Controller;

public class Player {
	// Wraps around a Controllable and a Controller to do input, see also:
	// PlayerListener
	public enum ControlType {
		Controller, WASD, Arrows
	}

	public Controllable mActor;
	public int id;
	public ControlType controltype;
	public Controller controller;
	public boolean mLeader;
	public boolean active;
	public boolean mustache;

	public Player(int i) {
		id = i;
		mLeader = false;
		// Players start active
		active = true;
		mustache = Math.random() < (Level.COMPLETE ? .1f : .001f);
	}
}
