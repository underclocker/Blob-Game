package org.siggd.slab;

import org.siggd.Level;
import org.siggd.actor.Background;

import com.badlogic.gdx.math.Vector2;

public class Pillar2 extends Slab {

	public Pillar2() {
		super();
		nextSlabs.add(new SlabStock(Air0.class, 1f, 0f));
		nextSlabs.add(new SlabStock(Air2.class, 1f, 0f));
		nextSlabs.add(new SlabStock(Block0.class, 1f, 0f));
		nextSlabs.add(new SlabStock(Blockpoint5.class, 1f, 0f));
		nextSlabs.add(new SlabStock(Bounce0.class, 1f, 4f));
		nextSlabs.add(new SlabStock(Jiggle2.class, 8f, 8f));
		nextSlabs.add(new SlabStock(Jiggle3.class, 8f, 4f));
		nextSlabs.add(new SlabStock(Wind2.class, 3f, 2f));
	}

	@Override
	public void gen(Level l) {
		Vector2 pos = getOrigin();
		Background b = makeBackground("Block", l, pos);
		pos.y += 2;
		b = makeBackground("Pillar", l, pos);
	}

}
