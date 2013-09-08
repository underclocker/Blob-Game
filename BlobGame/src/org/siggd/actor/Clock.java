package org.siggd.actor;

import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.Timer;
import org.siggd.Trigger;
import org.siggd.view.BodySprite;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Clock extends Actor implements Trigger {
	private String mTex;
	private Timer mTimer;

	public Clock(Level level, long id) {
		super(level, id);
		mName = "Clock";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 64, BodyType.StaticBody, origin, false);
		mBody.setFixedRotation(true);
		mBody.setActive(false);
		mTimer = new Timer();
		mTimer.setTrigger(this);
		setProp("Output", (Integer) 0);
		// invisible when running
		setProp("Visible", (Integer) 0);
		// gate should always be on top
		setProp("Layer", Integer.MAX_VALUE);
		setProp("Period", (Integer) 1000000);
		mDrawable.mDrawables.add(new BodySprite(mBody, origin, mTex));
	}

	@Override
	public void setProp(String name, Object val) {
		if (name.equals("Period")) {
			mTimer.setTimer(Convert.getInt(val));
		}
		super.setProp(name, val);
	}

	@Override
	public void update() {
		if (mTimer.isPaused()) {
			// start timer
			mTimer.reset();
		}
		mTimer.update();
	}

	@Override
	public void loadResources() {
		AssetManager man = Game.get().getAssetManager();
		man.load(mTex, Texture.class);
	}

	@Override
	public void loadBodies() {
	}

	@Override
	public void postLoad() {
	}

	@Override
	public void trigger() {
		if (Convert.getInt(getProp("Output")) == 1) {
			setProp("Output", (Integer) 0);
		} else {
			setProp("Output", (Integer) 1);
		}
		mTimer.reset();
	}

}
