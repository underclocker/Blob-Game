package org.siggd.slab;

import org.siggd.Level;
import org.siggd.actor.Background;

import com.badlogic.gdx.math.Vector2;

public class Block0 extends Slab {

	public Block0() {
		super();
		nextSlabs.add(new SlabStock(Block0.class, 1f, 0f));
		nextSlabs.add(new SlabStock(Blockpoint5.class, 3f, 0f));
		nextSlabs.add(new SlabStock(Bounce0.class, 6f, 1f));
		nextSlabs.add(new SlabStock(Jiggle1.class, 3f, 2f));
		nextSlabs.add(new SlabStock(Air0.class, 1f, 2f));
		nextSlabs.add(new SlabStock(Wind0.class, 8f, 0f));
	}

	@Override
	public void gen(Level l) {
		Vector2 pos = getOrigin();
		Background b = makeBackground("Block", l, pos);
	}

}
