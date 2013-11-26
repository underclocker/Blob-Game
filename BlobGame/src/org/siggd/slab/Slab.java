package org.siggd.slab;

import org.siggd.Level;
import org.siggd.actor.Background;

import com.badlogic.gdx.math.Vector2;

public abstract class Slab {
	int mNumber;

	public Slab(int number) {
		mNumber = number;
	}

	public void gen(Level l) {
	};

	public Background makeBackground(String name, Level l, Vector2 pos) {
		Background b = new Background(l, 0);
		b.setProp("Body", name);
		b.setProp("X", pos.x);
		b.setProp("Y", pos.y);
		b.setFriction(.3f);
		b.loadResources();
		b.loadBodies();
		l.addActor(b);
		return b;
	}
	public Vector2 getOrigin(){
		return new Vector2(mNumber*2f,0);
	}
}
