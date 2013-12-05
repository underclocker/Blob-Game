package org.siggd.slab;

import org.siggd.Level;
import org.siggd.actor.JiggleBall;

import com.badlogic.gdx.math.Vector2;

public class Jiggle1 extends Slab {

	public Jiggle1() {
		super();
		nextSlabs.add(new SlabStock(Block0.class, 1f, 0f));
		nextSlabs.add(new SlabStock(Jiggle1.class, 2f, 2f));
		nextSlabs.add(new SlabStock(Blockpoint5.class, 1f, 3f));
		nextSlabs.add(new SlabStock(Wind0.class, 1f, 2f));
		nextSlabs.add(new SlabStock(Conv2.class, 3f, 6f));

	}

	@Override
	public void gen(Level l) {
		Vector2 pos = getOrigin();
		JiggleBall jiggle = new JiggleBall(l, 0);
		jiggle.setProp("X", pos.x);
		jiggle.setProp("Y", 1f);
		l.addActor(jiggle);
	}
}
