package org.siggd.slab;

import org.siggd.Level;

public class Air4 extends Slab {

	public Air4() {
		super();
		nextSlabs.add(new SlabStock(Air0.class, 12f, 3f));
		nextSlabs.add(new SlabStock(Air2.class, 12f, 2f));
		nextSlabs.add(new SlabStock(Block0.class, 1f, 2f));
		nextSlabs.add(new SlabStock(Block2.class, 4f, 0f));
		nextSlabs.add(new SlabStock(Pillar0.class, 1f, 2f));
		nextSlabs.add(new SlabStock(Bounce0.class, 1f, 4f));
		nextSlabs.add(new SlabStock(Bounce2.class, 3f, 4f));
		nextSlabs.add(new SlabStock(Jiggle3.class, 6f, 6f));
		nextSlabs.add(new SlabStock(Jiggle1.class, 1f, 8f));
		nextSlabs.add(new SlabStock(Wind2.class, 1f, 3f));
		nextSlabs.add(new SlabStock(Gear4.class, 2f, 3f));
	}

	@Override
	public void gen(Level l) {
		makeDot(l, getOrigin().add(0, 6));
	}
}
