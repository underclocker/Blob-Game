package org.siggd.slab;

import org.siggd.Level;
import org.siggd.actor.Background;

import com.badlogic.gdx.math.Vector2;

public class Blockpoint5 extends Slab {

	public Blockpoint5() {
		super();
		nextSlabs.add(new SlabStock(Air0.class, 1f, 1f));
		nextSlabs.add(new SlabStock(Blockpoint5.class, 1f, 0f));
		nextSlabs.add(new SlabStock(Block0.class, 1f, 0f));
		nextSlabs.add(new SlabStock(Bounce0.class, 3f, 0f));
		nextSlabs.add(new SlabStock(Wind0.class, 3f, 1f));
	}

	@Override
	public void gen(Level l) {
		Vector2 pos = getOrigin();
		pos.y += .5f;
		Background b = makeBackground("Block", l, pos);
		makeDot(l, getOrigin().add(-.5f, 2f));
		makeDot(l, getOrigin().add(.5f, 2f));
	}
}
