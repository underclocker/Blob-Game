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
		mDrawable.mDrawables.add(new DebugActorLinkDrawable(this, null, null, Color.RED, Color.GREEN));
		setProp("Friction", (Float) .4f);
		setProp("DirectionX", (Float) 1f);
		setProp("DirectionY", (Float) 0f);
		setProp("Safety", (Integer) (1));
		setProp("Grabbable", (Integer) 0);
		setProp("Hold Time", (Integer) (0));
	}

	public boolean inputActive() {
		Object val = mInputSrc.observe();
		return (val instanceof Boolean) && (Boolean)val;
	}

	/**
	 * Load resources needed by the actor
	 */
	@Override
	public void loadResources() {
		AssetManager man = Game.get().getAssetManager();
		man.load(mTex, Texture.class);
	}

	/**
	 * Load bodies needed by the actor
	 */
	@Override
	public void loadBodies() {
	}

	@Override
	public void update() {
		// if there is input, poll it for permission to update
		if ((mInputSrc == null || inputActive())
				&& mHold >= Convert.getInt(getProp("Hold Time"))) {
			super.update();
			float xDir = Convert.getFloat(getProp("DirectionX"));
			float yDir = Convert.getFloat(getProp("DirectionY"));
			mBody.setLinearVelocity(new Vector2(xDir, yDir));
		} else {
			mHold++;
			mBody.setLinearVelocity(new Vector2(0, 0));
		}

		// If we could potentially crush something, stop
		if (Convert.getInt(getProp("Safety")) == 1
				&& CrushSensor.canCrush(mLevel.getContactHandler(), this)) {
			mBody.setLinearVelocity(new Vector2(0, 0));
		}

		Iterable<StableContact> contacts = Game.get().getLevel().getContactHandler()
				.getContacts(this);
		Iterable<Actor> actors = ContactHandler.getActors(contacts);
		boolean foundRedirector = false;
		for (Actor a : actors) {
			if (a instanceof Redirector) {
				foundRedirector = true;
				break;
			}
		}
		if (!foundRedirector) {
			mHold = 0;
			setProp("Hold Time", (Integer) 0);
		}
	}

	/**
	 * Dispose of the actor's resources
	 */
	@Override
	public void dispose() {
		AssetManager man = Game.get().getAssetManager();
		if(man.containsAsset(mTex)) {
			man.unload(mTex);
		}
	}

	////////////////
	// Properties
	////////////////

	@Override
	public void postLoad() {
	}

	IObservable mInputSrc;
	@Override
	public Actor inputSrc() {
		return (Actor)mInputSrc;
	}

	@Override
	public void inputSrc(Actor inputSrc) {
		mInputSrc = (inputSrc instanceof IObservable) ? (IObservable)inputSrc : null;
	}
}
