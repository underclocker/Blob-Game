package org.siggd.slab;

import org.siggd.Level;
import org.siggd.actor.Background;
import org.siggd.actor.BouncePlate;

import com.badlogic.gdx.math.Vector2;

public class Bounce0 extends Slab {

	public Bounce0() {
		super();
		nextSlabs.add(new SlabStock(Air0.class, 1f));
		nextSlabs.add(new SlabStock(Blockpoint5.class, 1f));
	}

	@Override
	public void gen(Level l) {
		Vector2 pos = getOrigin();
		Background b = makeBackground("Block", l, pos);
		BouncePlate bp = new BouncePlate(l, 0);
		bp.setProp("Stroke Length", 6f);
		bp.setX(pos.x);
		bp.setY(.85f);
		l.addActor(bp);
	}

}
