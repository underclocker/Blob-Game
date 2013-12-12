package org.siggd.slab;

import org.siggd.Level;
import org.siggd.actor.JiggleBall;

import com.badlogic.gdx.math.Vector2;

public class Jiggle3 extends Slab {

	public Jiggle3() {
		super();
		nextSlabs.add(new SlabStock(Block2.class, 3f, 2f));
		nextSlabs.add(new SlabStock(Block0.class, 1f, 0f));
		nextSlabs.add(new SlabStock(Jiggle3.class, 4f, 1f));
		nextSlabs.add(new SlabStock(Jiggle2.class, 4f, 5f));
		nextSlabs.add(new SlabStock(Blockpoint5.class, 1f, 3f));
		nextSlabs.add(new SlabStock(Air2.class, 8f, 3f));
		nextSlabs.add(new SlabStock(Wind2.class, 4f, 3f));
	}

	@Override
	public void gen(Level l) {
		Vector2 pos = getOrigin();
		JiggleBall jiggle = new JiggleBall(l, 0);
		jiggle.setProp("X", pos.x);
		jiggle.setProp("Y", 3f);
		l.addActor(jiggle);
		makeDot(l, getOrigin().add(0f, 4f));
	}
}
