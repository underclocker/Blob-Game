package org.siggd.actor;

import java.util.ArrayList;

import org.box2dLight.PointLight;
import org.siggd.ContactHandler;
import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.StableContact;
import org.siggd.actor.meta.IObservable;
import org.siggd.view.BodySprite;
import org.siggd.view.Drawable;
import org.siggd.view.LevelView;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;

public class PressurePlate extends Actor implements IObservable {
	private String mTex;
	private String mActiveTex;
	private Drawable mActiveDrawable;
	private Drawable mDefaultDrawable;
	private PointLight mPointLight;
	private int mRestTime = 0;
	private int mDelay = 8;

	public PressurePlate(Level level, long id) {
		super(level, id);
		mName = "PressurePlate";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		mActiveTex = mTex.substring(0, mTex.length() - 4) + "active.png";
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 128, BodyType.StaticBody, origin, false);
		mBody.setFixedRotation(true);
		mDefaultDrawable = new BodySprite(mBody, origin, mTex);
		mDrawable.mDrawables.add(mDefaultDrawable);
		mActiveDrawable = new BodySprite(mBody, origin, mActiveTex);
		if (LevelView.mUseLights) {
			mPointLight = new PointLight(Game.get().getLevelView().getRayHandler(), 16);
			mPointLight.setDistance(1.5f);
			mPointLight.attachToBody(mBody, 0, 0);
			mPointLight.setSoftnessLenght(1f);
			mPointLight.setXray(true);
			mPointLight.setStaticLight(true);
		}
		// TODO: set light active based on if actor is dummy or not
		setProp("X", -10000);
		setProp("Restitution", 0f);
		setState(false);
	}

	/**
	 * Load resources needed by the actor
	 */
	@Override
	public void loadResources() {
		AssetManager man = Game.get().getAssetManager();
		man.load(mTex, Texture.class);
		man.load(mActiveTex, Texture.class);
	}

	/**
	 * Load body used by the actor
	 */
	public void loadBodies() {

	}

	@Override
	public void update() {
		Iterable<StableContact> contacts = Game.get().getLevel().getContactHandler()
				.getContacts(this);
		ArrayList<Body> bodies = (ArrayList<Body>) ContactHandler.getBodies(contacts);
		boolean active = false;

		for (Body b : bodies) {
			if (b.getUserData() instanceof Dot)
				continue;
			boolean isSensor = false;
			Array<Fixture> fixtures = b.getFixtureList();
			int i = 0;
			for (Fixture f : fixtures) {
				if (f.isSensor()) {
					isSensor = true;
				}
				i++;
			}
			if (isSensor)
				continue;
			else {
				active = true;
				break;
			}
		}
		setState(active);
	}

	@Override
	public void setProp(String name, Object val) {
		if (name.equals("Active")) {
			if (mPointLight != null)
				mPointLight.setActive(Convert.getInt(val) != 0);
		}
		super.setProp(name, val);
	}

	@Override
	public void postLoad() {
	}

	@Override
	public void dispose() {
	}

	@Override
	public Object observe() {
		return getState();
	}

	public boolean getState() {
		return mState && mRestTime <= 0;
	}

	private void setState(boolean state) {
		if (mPointLight != null)
			mPointLight.setColor(state ? 0 : .3f, state ? .3f : 0, 0, .8f);
		if (state == mState) {
			mRestTime--;
			return;
		} else {
			mRestTime = mDelay;
		}
		if (state) {
			mDrawable.mDrawables.remove(mDefaultDrawable);
			mDrawable.mDrawables.add(mActiveDrawable);
		} else {
			mDrawable.mDrawables.remove(mActiveDrawable);
			mDrawable.mDrawables.add(mDefaultDrawable);
		}
		mState = state;

	}

	private boolean mState = false; // This value MUST only be modified through
									// setState, or the lights won't work.
}
