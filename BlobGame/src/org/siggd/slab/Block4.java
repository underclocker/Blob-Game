package org.siggd.slab;

import org.siggd.Level;
import org.siggd.actor.Background;

import com.badlogic.gdx.math.Vector2;

public class Block4 extends Slab {

	public Block4() {
		super();
		nextSlabs.add(new SlabStock(Block4.class, 3f, 4f));
		nextSlabs.add(new SlabStock(Block0.class, 1f, 0f));
		nextSlabs.add(new SlabStock(Air4.class, 4f, 3f));
		nextSlabs.add(new SlabStock(Air2.class, 4f, 5f));
		nextSlabs.add(new SlabStock(Air0.class, 4f, 6f));
		nextSlabs.add(new SlabStock(Bounce0.class, 1f, 2f));
		nextSlabs.add(new SlabStock(Bounce2.class, 2f, 0f));
		nextSlabs.add(new SlabStock(Wind0.class, 3f, 0f));
		nextSlabs.add(new SlabStock(Wind2.class, 2f, 3f));
		nextSlabs.add(new SlabStock(Gear4.class, 2f, 2f));
	}

	@Override
	public void gen(Level l) {
		Vector2 pos = getOrigin();
		Background b = makeBackground("Block", l, pos);
		pos.y += 2;
		b = makeBackground("Block", l, pos);
		pos.y += 2;
		b = makeBackground("Block", l, pos);
		makeDot(l, getOrigin().add(-.5f, 5.5f));
		makeDot(l, getOrigin().add(.5f, 5.5f));

	}

}
