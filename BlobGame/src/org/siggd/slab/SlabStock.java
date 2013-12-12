package org.siggd.slab;

import org.siggd.LevelGen;

public class SlabStock {
	public Class mName;
	public float mWeight;
	public float mDifficulty;

	public SlabStock(Class name, float weight, float difficulty) {
		mName = name;
		mWeight = weight;
		mDifficulty = difficulty;
	}

	public float getWeight() {
		/*
		 * float delta = (mDifficulty - LevelGen.Difficulty); float weight =
		 * mWeight * (1 / (1 + delta * delta)); if (mDifficulty -
		 * LevelGen.Difficulty > 3) weight = 0; return weight;
		 */
		return mDifficulty <= LevelGen.Difficulty ? mWeight : 0;
	}
}
