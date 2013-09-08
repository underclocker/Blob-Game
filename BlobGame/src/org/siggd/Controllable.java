package org.siggd;

import com.badlogic.gdx.controllers.PovDirection;

public interface Controllable {
	// TODO: RIGHTSTICK, DPAD

	// axiis: 0:up/down leftstick, 1:left/right leftstick, 2:up/down rightstick,
	// 3:left/right rightstick, 4:triggers

	// 0:a, 1:b, 2:x, 3:y, 4:lb, 5:rb, 6:select, 7:start, 8:leftstick,
	// 9:rightstick
	public void left();

	public void right();

	public void up();

	public void down();

	public void triggerLeft();

	public void triggerRight();

	public void downAction(int id);

	public void upAction(int id);

	// ignore 'id' to make it work for all dpads
	public void dpad(int id, PovDirection dir);
}
