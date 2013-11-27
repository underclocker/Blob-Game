package org.siggd.slab;

import org.siggd.Level;
import org.siggd.actor.Background;
import org.siggd.actor.Wind;

import com.badlogic.gdx.math.Vector2;

public class Wind2 extends Slab {

	public Wind2() {
		super();
		nextSlabs.add(new SlabStock(Jiggle3.class, 1f, 4f));
		nextSlabs.add(new SlabStock(Air4.class, 2f, 3f));
		nextSlabs.add(new SlabStock(Block4.class, 4f, 0f));
	}

	@Override
	public void gen(Level l) {
		Vector2 pos = getOrigin();
		Background b = makeBackground("Block", l, pos);
		pos.y += 2;
		b = makeBackground("Block", l, pos);
		pos.y += 1;
		b = makeBackground("Outpipe colored", l, pos);
		b.setAngle(90);
		Wind wind = new Wind(l, 0);
		wind.setProp("Wind Strength", 10f);
		wind.setX(pos.x);
		wind.setY(4f);
		l.addActor(wind);
		wind = new Wind(l, 0);
		wind.setProp("Wind Strength", 10f);
		wind.setX(pos.x);
		wind.setY(5f);
		l.addActor(wind);
	}

}
