package org.siggd.slab;

import org.siggd.Level;
import org.siggd.actor.Background;

import com.badlogic.gdx.math.Vector2;

public class Air2 extends Slab {

	public Air2() {
		super();
		nextSlabs.add(new SlabStock(Pillar0.class, 1f, 0f));
		nextSlabs.add(new SlabStock(Bounce0.class, 3f, 0f));
		nextSlabs.add(new SlabStock(Jiggle2.class, 4f, 4f));
		nextSlabs.add(new SlabStock(Jiggle1.class, 4f, 5f));
		nextSlabs.add(new SlabStock(Air0.class, 8f, 0f));
		nextSlabs.add(new SlabStock(Wind0.class, 4f, 0f));
	}

	@Override
	public void gen(Level l) {

	}
}
