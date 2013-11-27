package org.siggd.slab;

import org.siggd.Level;
import org.siggd.actor.Background;
import org.siggd.actor.Gear;

import com.badlogic.gdx.math.Vector2;

public class Gear4 extends Slab {

	public Gear4() {
		super();
		nextSlabs.add(new SlabStock(Air4.class, 4f, 0f));
		nextSlabs.add(new SlabStock(Air2.class, 4f, 6f));
	}

	@Override
	public void gen(Level l) {
		Vector2 pos = getOrigin();
		Background b = makeBackground("Block", l, pos);
		pos.y += 2;
		b = makeBackground("Block", l, pos);
		pos.y += 2;
		b = makeBackground("Block", l, pos);
		Gear g = new Gear(l,0);
		g.setProp("Safety", 0);
		g.setX(pos.x);
		g.setY(pos.y);
		g.setProp("Rotation Speed", -.5f);
		l.addActor(g);
	}

}
