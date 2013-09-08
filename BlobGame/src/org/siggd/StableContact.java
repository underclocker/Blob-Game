package org.siggd;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;

public class StableContact {
	private Fixture mA;
	private Fixture mB;

	public StableContact(Contact c) {
		mA = c.getFixtureA();
		mB = c.getFixtureB();
	}

	public StableContact(StableContact sc, boolean flip) {
		mA = (flip) ? sc.getFixtureB() : sc.getFixtureA();
		mB = (flip) ? sc.getFixtureA() : sc.getFixtureB();
	}

	public Fixture getFixtureA() {
		return mA;
	}

	public Fixture getFixtureB() {
		return mB;
	}
}
