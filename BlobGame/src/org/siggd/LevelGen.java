package org.siggd;

import org.siggd.slab.Block0;
import org.siggd.slab.Slab;
import org.siggd.slab.SlabStock;

public class LevelGen {
	public static int Difficulty = 0;

	public LevelGen() {
		Level l = Game.get().getLevel();
		createSlabs(l);
	}

	public void createSlabs(Level l) {
		Slab slab;
		SlabStock slabStock = new SlabStock(Block0.class, 0, 0);
		for (int i = 0; i < 32; i++) {
			try {
				slab = (Slab) slabStock.mName.getConstructor().newInstance();
				slab.setNumber(i);
				slab.gen(l);
				slabStock = slab.getNextSlab();
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}
}
