package org.siggd.slab;

import org.siggd.Level;

public class Air0 extends Slab {

	public Air0() {
		super();
		nextSlabs.add(new SlabStock(Block0.class, 1f, 0f));
	}

	@Override
	public void gen(Level l) {
		makeDot(l, getOrigin().add(0, 2));
	}
}
