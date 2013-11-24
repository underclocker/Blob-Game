package org.siggd;

/**
 * This class implements a simple one-shot timer. A timeout time is specified,
 * and a triggered event is fired that amount of time after reset() is called.
 * The timer can also be paused/unpause, and it's status queried. Times are
 * specified in number of calls to level.update()
 * 
 * @author mysterymath
 * 
 */
public class Timer {
	// Current time of the timer (beginning at 0)
	public int mCurTime;
	// Time to trigger
	public int mTrigTime;
	// Event to trigger
	private Trigger mTrig;
	// True if the timer is paused
	private boolean mIsPaused;

	public Timer() {
		mIsPaused = true;
	}

	public boolean isPaused() {
		return mIsPaused;
	}

	/**
	 * Sets the Timer, starts paused.
	 * 
	 * @param time
	 *            The timeout time
	 */
	public void setTimer(int time) {
		mTrigTime = time;
	}

	/**
	 * Sets the event to trigger (or null if none)
	 * 
	 * @param trig
	 *            The trigger
	 */
	public void setTrigger(Trigger trig) {
		mTrig = trig;
	}

	/**
	 * Pauses the timer
	 */
	public void pause() {
		mIsPaused = true;
	}

	/**
	 * Unpauses the timer
	 */
	public void unpause() {
		mIsPaused = false;
	}

	/**
	 * Unpauses and resets the timer
	 */
	public void reset() {
		unpause();
		mCurTime = 0;
	}

	/**
	 * Returns true if the timer has triggered
	 */
	public boolean isTriggered() {
		return mCurTime >= mTrigTime;
	}

	/**
	 * Gets the percent completion of the timer
	 */
	public float getCompletion() {
		if (mTrigTime == 0) {
			return 1;
		}

		return (float) mCurTime / mTrigTime;
	}

	/**
	 * Updates the timer
	 */
	public void update() {
		if (!mIsPaused && ++mCurTime >= mTrigTime && mTrig != null) {
			mTrig.trigger();
		}
	}
}
