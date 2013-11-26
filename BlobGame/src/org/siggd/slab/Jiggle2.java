package org.siggd.slab;

import org.siggd.Level;
import org.siggd.actor.JiggleBall;

import com.badlogic.gdx.math.Vector2;

public class Jiggle2 extends Slab {

	public Jiggle2() {
		super();
		nextSlabs.add(new SlabStock(Block2.class, 3f, 5f));
		nextSlabs.add(new SlabStock(Block0.class, 1f, 0f));
		nextSlabs.add(new SlabStock(Jiggle2.class, 4f, 2f));
		nextSlabs.add(new SlabStock(Jiggle1.class, 4f, 5f));
		nextSlabs.add(new SlabStock(Blockpoint5.class, 1f, 3f));
	}

	@Override
	public void gen(Level l) {
		Vector2 pos = getOrigin();
		JiggleBall jiggle = new JiggleBall(l, 0);
		jiggle.setProp("X", pos.x);
		jiggle.setProp("Y", 2f);
		l.addActor(jiggle);
	}
}
