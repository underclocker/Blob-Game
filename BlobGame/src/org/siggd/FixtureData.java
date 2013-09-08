package org.siggd;

import java.util.ArrayList;

import org.siggd.actor.Blob;

public class FixtureData {
	public boolean mIsParticle;
	public ArrayList<Blob> mViolatedBlobs;

	public FixtureData(boolean isparticle) {
		mIsParticle = isparticle;
	}

	public void addViolator(Blob blob) {
		if (!mViolatedBlobs.contains(blob)) {
			mViolatedBlobs.add(blob);
		}
	}
}
