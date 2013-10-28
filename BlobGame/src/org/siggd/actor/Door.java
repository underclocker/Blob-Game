package org.siggd.actor;

import org.siggd.ContactHandler;
import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.StableContact;
import org.siggd.actor.meta.IObservable;
import org.siggd.actor.meta.IObserver;
import org.siggd.actor.meta.Prop;
import org.siggd.view.BodySprite;
import org.siggd.view.CompositeDrawable;
import org.siggd.view.DebugActorLinkDrawable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

/**
 * This class can be used as a convenient shortcut for any of the bodies in
 * bodies.json. Just set the Body property to the appropriate body, and the
 * actor will become that body.
 * 
 * @author mysterymath
 * 
 */
public class Door extends Actor implements IObserver {
	private String mTex;
	private int mCamDelayMax = 120;
	private int mCamDelay = 0;

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
	public Door(Level level, long id) {
		super(level, id);
		mName = "Door";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 256, BodyType.KinematicBody, origin, false);
		mBody.setFixedRotation(true);

		((CompositeDrawable) mDrawable).mDrawables.add(new BodySprite(mBody, origin, mTex));
		((CompositeDrawable) mDrawable).mDrawables.add(new DebugActorLinkDrawable(this, null, null,
				Color.RED, Color.GREEN));

		mBody.setLinearDamping(.9f);
		this.setProp("Friction", (Float) .4f);
		this.setProp("Close Safety", (Integer) (1));
		this.setProp("Open Safety", (Integer) (1));
		this.setProp("Camera Tracked", (Integer) (0));
	}

	/**
	 * Load resources needed by the actor
	 */
	@Override
	public void loadResources() {
		AssetManager man = Game.get().getAssetManager();
		man.load(mTex, Texture.class);
	}

	@Override
	public void update() {
		Iterable<StableContact> contacts = Game.get().getLevel().getContactHandler()
				.getContacts(this);
		Iterable<Body> bodies = ContactHandler.getBodies(contacts);
		Actor actor;
		float angle = Convert.getFloat(getProp("Angle"));
		boolean active = inputActive();
		mBody.setLinearVelocity(new Vector2(active ? -1f : 1f, 0).rotate(angle));
		for (Body b : bodies) {
			actor = (Actor) b.getUserData();
			if (actor instanceof Redirector) {
				Vector2 dir = mBody.getLinearVelocity();
				Vector2 pos = new Vector2(this.mBody.getPosition().sub(actor.mBody.getPosition()));
				if (dir.dot(pos) < 0f) {
					mBody.setLinearVelocity(Vector2.Zero);
				}
			} else if (active && Convert.getInt(getProp("Open Safety")) != 0 || !active
					&& Convert.getInt(getProp("Close Safety")) != 0) {
				mBody.setLinearVelocity(Vector2.Zero);
			}
		}
		if (mBody.getLinearVelocity().len2() > 0 && active) {
			if (mCamDelay < 0)
				mCamDelay = 0;
			if (mCamDelay < mCamDelayMax)
				mCamDelay++;
		} else {
			mCamDelay--;
		}
		if (mCamDelay > 5 && Convert.getInt(getProp("Camera Tracked")) != 0) {
			// TODO: figure out how to move camera less violently and we can reenable this.
			//Game.get().getLevelView().setCameraPosition(mBody.getPosition());
		}
	}

	public boolean inputActive() {
		if (mInputSrc == null) {
			return false;
		}

		Object input = mInputSrc.observe();
		return (input instanceof Boolean && (Boolean) input);
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

	@Override
	public void loadBodies() {
	}

	@Override
	public void postLoad() {
	}

	// //////////////
	// Properties
	// //////////////

	private IObservable mInputSrc;

	@Override
	public Actor inputSrc() {
		return (Actor) mInputSrc;
	}

	@Override
	public void inputSrc(Actor inputSrc) {
		mInputSrc = (inputSrc instanceof IObservable) ? (IObservable) inputSrc : null;
	}
}
