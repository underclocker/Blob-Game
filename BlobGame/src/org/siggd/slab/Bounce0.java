package org.siggd.slab;

import org.siggd.Level;
import org.siggd.actor.Background;
import org.siggd.actor.BouncePlate;

import com.badlogic.gdx.math.Vector2;

public class Bounce0 extends Slab {

	public Bounce0() {
		super();
		nextSlabs.add(new SlabStock(Air0.class, 3f, 0f));
		nextSlabs.add(new SlabStock(Air2.class, 7f, 4f));
		nextSlabs.add(new SlabStock(Blockpoint5.class, 1f, 0f));
		nextSlabs.add(new SlabStock(Block2.class, 6f, 0f));
		nextSlabs.add(new SlabStock(Pillar2.class, 6f, 2f));
		nextSlabs.add(new SlabStock(Jiggle3.class, 3f, 2f));
		nextSlabs.add(new SlabStock(Bounce2.class, 3f, 3f));
	}

	@Override
	public void gen(Level l) {
		Vector2 pos = getOrigin();
		Background b = makeBackground("Block", l, pos);
		BouncePlate bp = new BouncePlate(l, 0);
		bp.setProp("Stroke Length", 5.5f);
		bp.setX(pos.x);
		bp.setY(.95f);
		l.addActor(bp);
	}

}
