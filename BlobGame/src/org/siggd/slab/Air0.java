package org.siggd.slab;

import org.siggd.Level;
import org.siggd.actor.Background;

import com.badlogic.gdx.math.Vector2;

public class Air0 extends Slab {

	public Air0() {
		super();
		nextSlabs.add(new SlabStock(Block0.class, 1f));
	}

	@Override
	public void gen(Level l) {

	}
}
