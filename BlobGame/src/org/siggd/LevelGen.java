package org.siggd;

import org.siggd.slab.LowBlock;

public class LevelGen {
	private int mCurrentSlab = 0;

	public LevelGen() {
		Level l = Game.get().getLevel();
		createSlabs(l);
	}

	public void createSlabs(Level l) {
		for (int i = 0; i < 32; i++) {
			new LowBlock(i).gen(l);
		}
	}
}
