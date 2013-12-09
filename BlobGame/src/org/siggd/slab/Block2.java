package org.siggd.slab;

import org.siggd.Level;
import org.siggd.actor.Background;

import com.badlogic.gdx.math.Vector2;

public class Block2 extends Slab {

	public Block2() {
		super();
		nextSlabs.add(new SlabStock(Block2.class, 1f, 0f));
		nextSlabs.add(new SlabStock(Block0.class, 3f, 0f));
		nextSlabs.add(new SlabStock(Blockpoint5.class, 1f, 0f));
		nextSlabs.add(new SlabStock(Wind0.class, 3f, 2f));
		nextSlabs.add(new SlabStock(Wind2.class, 4f, 0f));
		nextSlabs.add(new SlabStock(Air0.class, 4f, 1f));
		nextSlabs.add(new SlabStock(Air2.class, 9f, 3f));
		nextSlabs.add(new SlabStock(Bounce0.class, 2f, 2f));
		nextSlabs.add(new SlabStock(Bounce2.class, 5f, 0f));
		nextSlabs.add(new SlabStock(Jiggle3.class, 8f, 6f));
		nextSlabs.add(new SlabStock(Jiggle2.class, 5f, 7f));
	}

	@Override
	public void gen(Level l) {
		Vector2 pos = getOrigin();
		Background b = makeBackground("Block", l, pos);
		pos.y += 2;
		b = makeBackground("Block", l, pos);
		makeDot(l, getOrigin().add(-.5f, 3.5f));
		makeDot(l, getOrigin().add(.5f, 3.5f));

	}

}
