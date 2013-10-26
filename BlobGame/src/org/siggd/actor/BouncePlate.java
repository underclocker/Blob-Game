package org.siggd.actor;

import java.util.ArrayList;

import org.siggd.ContactHandler;
import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.StableContact;
import org.siggd.view.BodySprite;
import org.siggd.view.CompositeDrawable;
import org.siggd.view.Drawable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class BouncePlate extends Actor {
	private String mTex;
	private String mActiveTex;
	private Drawable mActiveDrawable;
	private Drawable mDefaultDrawable;
	private int mTimer = 0;
	private int mRate = 5;
	private String mBouncePlateFile = "data/sfx/bounceplate.wav";

	public BouncePlate(Level level, long id) {
		super(level, id);

		mName = "BouncePlate";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		mActiveTex = mTex.substring(0, mTex.length() - 4) + "active.png";

		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 256, BodyType.KinematicBody, origin, false);
		mDefaultDrawable = new BodySprite(mBody, origin, mTex);
		((CompositeDrawable) mDrawable).mDrawables.add(mDefaultDrawable);
		mActiveDrawable = new BodySprite(mBody, origin, mActiveTex);

		setProp("Output", (Integer) 0);
		setProp("Restitution", (Float) 0.25f);
		setProp("Stroke Length", (Float) 8f);
	}

	/**
	 * Load resources needed by the actor
	 */
	@Override
	public void loadResources() {
		AssetManager man = Game.get().getAssetManager();
		man.load(mTex, Texture.class);
		man.load(mActiveTex, Texture.class);
		man.load(mBouncePlateFile, Sound.class);
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
		mBody.setLinearVelocity(Vector2.Zero);
		float stroke = Convert.getFloat(getProp("Stroke Length"));
		int output = Convert.getInt(getProp("Output")) * 2 - 1;
		mTimer += output;
		if (bodies.size() > 0 && output == -1 && mTimer <= -mRate) {
			setProp("Output", (Integer) 1);
			((CompositeDrawable) mDrawable).mDrawables.remove(mDefaultDrawable);
			((CompositeDrawable) mDrawable).mDrawables.add(mActiveDrawable);
			mBody.setLinearVelocity(new Vector2(0, stroke).rotate(Convert.getDegrees(mBody
					.getAngle())));
			mTimer = -mRate;
			AssetManager man = Game.get().getAssetManager();
			Sound sound;
			long soundID;
			if (man.isLoaded(mBouncePlateFile)) {
				sound = man.get(mBouncePlateFile, Sound.class);
				soundID = sound.play();
				sound.setPitch(soundID, 1.25f);
				sound.setVolume(soundID, .45f);

			}
		} else if (mTimer >= mRate && output == 1) {
			setProp("Output", (Integer) 0);
			((CompositeDrawable) mDrawable).mDrawables.remove(mActiveDrawable);
			((CompositeDrawable) mDrawable).mDrawables.add(mDefaultDrawable);
			mBody.setLinearVelocity(new Vector2(0, -stroke).rotate(Convert.getDegrees(mBody
					.getAngle())));
			mTimer = mRate;
		}
	}

	@Override
	public void postLoad() {
	}
}
