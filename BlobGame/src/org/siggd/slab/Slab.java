package org.siggd.slab;

import java.util.ArrayList;

import org.siggd.Level;
import org.siggd.LevelGen;
import org.siggd.actor.Background;

import com.badlogic.gdx.math.Vector2;

public abstract class Slab {
	int mNumber;
	ArrayList<SlabStock> nextSlabs = new ArrayList<SlabStock>();

	public Slab() {
	}

	public void setNumber(int number) {
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

	public Vector2 getOrigin() {
		return new Vector2(mNumber * 2f, 0);
	}

	public SlabStock getNextSlab() {
		float total = 0;
		for (SlabStock s : nextSlabs) {
			total += s.getWeight();
		}
		float rand = (float) Math.random() * total;
		for (SlabStock s : nextSlabs) {
			rand -= s.getWeight();
			if (rand <= 0) {
				return s;
			}
		}
		return null;
	}
}
