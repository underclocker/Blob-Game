package org.siggd.slab;

import org.siggd.Level;
import org.siggd.actor.Background;
import org.siggd.actor.BouncePlate;

import com.badlogic.gdx.math.Vector2;

public class Bounce2 extends Slab {

	public Bounce2() {
		super();
		nextSlabs.add(new SlabStock(Air4.class, 8f, 3f));
		nextSlabs.add(new SlabStock(Air2.class, 8f, 0f));
		nextSlabs.add(new SlabStock(Block2.class, 4f, 1f));
		nextSlabs.add(new SlabStock(Jiggle3.class, 2f, 5f));
		nextSlabs.add(new SlabStock(Block4.class, 4f, 0f));
	}

	@Override
	public void gen(Level l) {
		Vector2 pos = getOrigin();
		Background b = makeBackground("Block", l, pos);
		pos.y += 2;
		b = makeBackground("Block", l, pos);
		BouncePlate bp = new BouncePlate(l, 0);
		bp.setProp("Stroke Length", 5.5f);
		bp.setX(pos.x);
		bp.setY(pos.y + .95f);
		l.addActor(bp);
	}

}
