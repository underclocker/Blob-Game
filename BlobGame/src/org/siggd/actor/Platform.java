package org.siggd.actor;

import org.siggd.ContactHandler;
import org.siggd.Convert;
import org.siggd.CrushSensor;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.StableContact;
import org.siggd.actor.meta.IObservable;
import org.siggd.actor.meta.IObserver;
import org.siggd.view.BodySprite;
import org.siggd.view.DebugActorLinkDrawable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

/**
 * This class can be used as a convenient shortcut for any of the bodies in
 * bodies.json. Just set the Body property to the appropriate body, and the
 * actor will become that body.
 * 
 * @author mysterymath
 * 
 */
public class Platform extends Actor implements IObserver {
	private String mTex;
	public int mHold;
	public Redirector mRedirector;
	private int mUncrushDelay;
	private String mSoundFile = "data/sfx/blublubluh.wav";
	private int mSoundDelay = -300;

	/**
	 * Constructor. No non-optional parameters may be added to this constructor.
	 * This should contain only properties, and code that MUST run before later
	 * init,
	 * 
	 * @param level
	 *            The level that contains this actor
	 * @param id
	 *            The id
	 */
	public Platform(Level level, long id) {
		super(level, id);
		mName = "Platform";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 256, BodyType.KinematicBody, origin, false);
		mBody.setFixedRotation(true);
		mDrawable.mDrawables.add(new BodySprite(mBody, origin, mTex));
		mDrawable.mDrawables.add(new DebugActorLinkDrawable(this, null, null, Color.RED,
				Color.GREEN));
		setProp("Friction", (Float) .4f);
		setProp("DirectionX", (Float) 1f);
		setProp("DirectionY", (Float) 0f);
		setProp("Safety", (Integer) (1));
		setProp("Grabbable", (Integer) 0);
		setProp("Hold Time", (Integer) (0));
		setProp("Return Target", (Integer) (-1));
		mUncrushDelay = 0;
	}

	public boolean inputActive() {
		Object val = mInputSrc.observe();
		return (val instanceof Boolean) && (Boolean) val;
	}

	/**
	 * Load resources needed by the actor
	 */
	@Override
	public void loadResources() {
		AssetManager man = Game.get().getAssetManager();
		man.load(mTex, Texture.class);
		man.load(mSoundFile, Sound.class);
	}

	/**
	 * Load bodies needed by the actor
	 */
	@Override
	public void loadBodies() {
	}

	@Override
	public void update() {
		mSoundDelay++;
		// if there is input, poll it for permission to update

		Iterable<StableContact> contacts = Game.get().getLevel().getContactHandler()
				.getContacts(this);
		Iterable<Actor> actors = ContactHandler.getActors(contacts);
		Redirector mOldRedir = mRedirector;
		mRedirector = null;
		for (Actor a : actors) {
			if (a instanceof Redirector) {
				mRedirector = (Redirector) a;
				break;
			}
		}
		if (mRedirector != null && mRedirector != mOldRedir) {
			mHold = 0;
		}

		if ((mInputSrc == null || inputActive()) && mHold >= Convert.getInt(getProp("Hold Time"))) {
			super.update();
			float xDir;
			float yDir;
			yDir = Convert.getFloat(getProp("DirectionY"));
			xDir = Convert.getFloat(getProp("DirectionX"));
			mBody.setLinearVelocity(new Vector2(xDir, yDir));
		} else {
			mHold++;
			float xDir = 0f;
			float yDir = 0f;
			if (mInputSrc != null && mInputSrc instanceof SwingingCan) {
				if (mRedirector != null
						&& mRedirector.equals(Game.get().getLevel()
								.getActorById(Convert.getInt(getProp("Return Target"))))) {
					if (!inputActive()) {
						mHold--;
					}
				} else if (mHold >= Convert.getInt(getProp("Hold Time"))) {
					yDir = Convert.getFloat(getProp("DirectionY"));
					xDir = Convert.getFloat(getProp("DirectionX"));
				}
			}
			mBody.setLinearVelocity(new Vector2(xDir, yDir));
		}

		// If we could potentially crush something, stop
		if (Convert.getInt(getProp("Safety")) == 1
				&& CrushSensor.canCrush(mLevel.getContactHandler(), this)) {
			if (inputSrc() instanceof SwingingCan) {
				if (mUncrushDelay <= 0) {
					float yDir = -Convert.getFloat(getProp("DirectionY"));
					float xDir = -Convert.getFloat(getProp("DirectionX"));
					setProp("DirectionX", xDir);
					setProp("DirectionY", yDir);
					mBody.setLinearVelocity(new Vector2(xDir, yDir));
					mUncrushDelay = 30;
				}
			} else {
				mBody.setLinearVelocity(new Vector2(0, 0));
			}
		}
		mUncrushDelay--;

		if (mBody.getLinearVelocity().len2() > 0 && Game.get().getLevel().musicTick()) {
			if (mSoundDelay >= 0) {
				AssetManager man = Game.get().getAssetManager();
				Sound sound;
				long soundID;
				if (man.isLoaded(mSoundFile)) {
					sound = man.get(mSoundFile, Sound.class);
					soundID = sound.play();
					sound.setVolume(soundID, .35f);
					sound.setPitch(soundID, 1f);
				}
			}
		}
	}

	/**
	 * Dispose of the actor's resources
	 */
	@Override
	public void dispose() {
		AssetManager man = Game.get().getAssetManager();
		if (man.containsAsset(mTex)) {
			man.unload(mTex);
		}
	}

	// //////////////
	// Properties
	// //////////////

	@Override
	public void postLoad() {
	}

	IObservable mInputSrc;

	@Override
	public Actor inputSrc() {
		return (Actor) mInputSrc;
	}

	@Override
	public void inputSrc(Actor inputSrc) {
		mInputSrc = (inputSrc instanceof IObservable) ? (IObservable) inputSrc : null;
	}
}
