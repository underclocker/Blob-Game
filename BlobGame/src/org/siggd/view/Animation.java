package org.siggd.view;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * An animation, implemented in terms of contained BodySprites
 * 
 * @author mysterymath
 * 
 */
public class Animation {
	public int mTicksPerFrame = 5;
	public boolean mLoop = true;

	private int mCurTick = 0;
	private ArrayList<BodySprite> mFrames = new ArrayList<BodySprite>();

	private Body mBody;
	private Vector2 mOrigin;

	/**
	 * Constructor
	 * 
	 * @param body
	 *            The body to which to attach the animation
	 * @param origin
	 */
	public Animation(Body body, Vector2 origin) {
		mBody = body;
		mOrigin = origin;
	}

	/**
	 * Adds a frame to the animation
	 * 
	 * @param mTexName
	 */
	public void addFrame(String mTexName) {
		mFrames.add(new BodySprite(mBody, mOrigin, mTexName));
	}
	
	/**
	 * Clears frames of the Animation
	 * 
	 */
	public void clearFrames() {
		mFrames.clear();
	}
	/**
	 * Updates the animation
	 */
	public void update() {
		mCurTick++;

		// Do looping
		int frameNum = mCurTick / mTicksPerFrame;
		if (mLoop && frameNum >= mFrames.size()) {
			reset();
		}
	}

	/**
	 * Returns the current frame, as a BodySprite
	 * 
	 * @return the current frame, as a BodySprite
	 */
	public BodySprite getCurFrame() {
		int frameNum = mCurTick / mTicksPerFrame;
		return (frameNum >= mFrames.size()) ? mFrames.get(mFrames.size() - 1) : mFrames
				.get(frameNum);
	}

	public void setCurFrame(int frame) {
		mCurTick = frame;
	}

	/**
	 * Resets the animation.
	 */
	public void reset() {
		mCurTick = 0;
	}
}
