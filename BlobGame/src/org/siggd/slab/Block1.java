package org.siggd.slab;

import org.siggd.Level;
import org.siggd.actor.Background;

import com.badlogic.gdx.math.Vector2;

public class Block1 extends Slab {

	public Block1() {
		super();
		nextSlabs.add(new SlabStock(Air0.class, 1f));
		nextSlabs.add(new SlabStock(Block1.class, 1f));
		nextSlabs.add(new SlabStock(Block0.class, 1f));
	}

	@Override
	public void gen(Level l) {
		Vector2 pos = getOrigin();
		pos.y += 1;
		Background b = makeBackground("Block", l, pos);
	}

}
