package org.siggd.actor;

import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.actor.meta.IObservable;
import org.siggd.view.BodySprite;
import org.siggd.view.DebugActorLinkDrawable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class AndGate extends Actor implements IObservable {
	private String mTex;
	private boolean mPropagate;
	private int mPropagateVal;

	public AndGate(Level level, long id) {
		super(level, id);
		mPropagate = false;
		mPropagateVal = 0;
		mName = "And Gate";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 andigin = new Vector2();
		mBody = makeBody(mName, 64, BodyType.StaticBody, andigin, false);
		mBody.setFixedRotation(true);
		mBody.setActive(false);
		setProp("Output", (Integer) 0);
		setProp("Input A", (Integer) (-1));
		setProp("Input B", (Integer) (-1));
		setProp("Visible", (Integer) 0);
		// gate should always be ontop
		setProp("Layer", Integer.MAX_VALUE);
		// gfx
		// magic number in pixels, based on textured
		mDrawable.mDrawables.add(new DebugActorLinkDrawable(this, "Input A", "Output", Color.RED,
				Color.GREEN, new Vector2(-12, -46)));
		mDrawable.mDrawables.add(new DebugActorLinkDrawable(this, "Input B", "Output", Color.RED,
				Color.GREEN, new Vector2(12, -46)));
		mDrawable.mDrawables.add(new BodySprite(mBody, andigin, mTex));
	}

	private void andInput() {
		boolean out = true;
		Actor A = mLevel.getActorById(Convert.getInt(getProp("Input A")));
		Actor B = mLevel.getActorById(Convert.getInt(getProp("Input B")));
		if (A != null) {
			out &= (Boolean)((IObservable)A).observe();
		}
		if (B != null) {
			out &= (Boolean)((IObservable)B).observe();
		}
		mPropagate = out;
		int tempVal = out ? 1 : 0;
		if (mPropagateVal != tempVal) {
			mPropagateVal = tempVal;
		}

	}

	@Override
	public void update() {
		andInput();
		if (mPropagate) {
			// creates a propagation delay
			setProp("Output", (Integer) mPropagateVal);
			mPropagate = false;
		}
		andInput();
	}

	@Override
	public void loadResources() {
		AssetManager man = Game.get().getAssetManager();
		man.load(mTex, Texture.class);
	}

	@Override
	public void loadBodies() {
		// TODO Auto-generated method stub

	}

	@Override
	public void postLoad() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object observe() {
		// TODO Auto-generated method stub
		return mPropagate;
	}

}
