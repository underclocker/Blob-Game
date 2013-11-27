package org.siggd.slab;

import org.siggd.Level;
import org.siggd.actor.Background;
import org.siggd.actor.Wind;

import com.badlogic.gdx.math.Vector2;

public class Wind0 extends Slab {

	public Wind0() {
		super();
		nextSlabs.add(new SlabStock(Jiggle3.class, 1f, 6f));
		nextSlabs.add(new SlabStock(Jiggle2.class, 1f, 5f));
		nextSlabs.add(new SlabStock(Air0.class, 1f, 1f));
		nextSlabs.add(new SlabStock(Air2.class, 1f, 4f));
		nextSlabs.add(new SlabStock(Block2.class, 2f, 0f));
		nextSlabs.add(new SlabStock(Block4.class, 2f, 4f));
		nextSlabs.add(new SlabStock(Gear4.class, 2f, 2f));
	}

	@Override
	public void gen(Level l) {
		Vector2 pos = getOrigin();
		Background b = makeBackground("Block", l, pos);
		pos.y += 1;
		b = makeBackground("Outpipe colored", l, pos);
		b.setAngle(90);
		Wind wind = new Wind(l, 0);
		wind.setProp("Wind Strength", 11f);
		wind.setX(pos.x);
		wind.setY(2f);
		l.addActor(wind);
		wind = new Wind(l, 0);
		wind.setProp("Wind Strength", 11f);
		wind.setX(pos.x);
		wind.setY(3f);
		l.addActor(wind);
	}

}
