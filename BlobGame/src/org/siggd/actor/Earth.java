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
public class Earth extends Actor implements IObserver {
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
	public Earth(Level level, long id) {
		super(level, id);
		mName = "earth";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 512, BodyType.KinematicBody, origin, false);
		mDrawable.mDrawables.add(new BodySprite(mBody, origin, mTex));
		setProp("Friction", (Float) .4f);
		mBody.setAngularVelocity(.21f);
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
