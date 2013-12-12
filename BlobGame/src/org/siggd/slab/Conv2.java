package org.siggd.slab;

import org.siggd.Level;
import org.siggd.actor.Background;
import org.siggd.actor.ConveyorBelt;

import com.badlogic.gdx.math.Vector2;

public class Conv2 extends Slab {

	public Conv2() {
		super();
		nextSlabs.add(new SlabStock(Jiggle3.class, 1f, 4f));
		nextSlabs.add(new SlabStock(Air4.class, 2f, 3f));
		nextSlabs.add(new SlabStock(Block4.class, 4f, 0f));	}

	@Override
	public void gen(Level l) {
		Vector2 pos = getOrigin();
		Background b = makeBackground("Block", l, pos);
		pos.y += 2;
		b = makeBackground("Block", l, pos);
		ConveyorBelt cb = new ConveyorBelt(l, 0);
		cb.setX(pos.x-1);
		cb.setY(pos.y);
		cb.setAngle(90f);
		cb.setProp("Anticlockwise", 0);
		cb.setProp("Speed", .95f);
		l.addActor(cb);
	}

}
