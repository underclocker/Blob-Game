package org.siggd.actor;

import org.siggd.Convert;
import org.siggd.CrushSensor;
import org.siggd.Game;
import org.siggd.Level;
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
 * This class can be used as a convenient shortcut for any of the bodies in bodies.json. Just set the Body property to the appropriate
 * body, and the actor will become that body.
 * @author mysterymath
 *
 */
public class Gear extends Actor implements IObserver {
	private String mTex;
	private int mDelay;
	/**
	 * Constructor. No non-optional parameters may be added to this constructor.
	 * This should contain only properties, and code that MUST run before later init,
	 * 
	 * @param level The level that contains this actor
	 * @param id The id
	 */
	public Gear(Level level, long id) {
		super(level, id);
		mDelay = 0;
		mName = "Gear";
		mTex = "data/"+Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 512, BodyType.KinematicBody, origin, false);
		mDrawable.mDrawables.add(new BodySprite(mBody, origin, mTex));
		mDrawable.mDrawables.add(new DebugActorLinkDrawable(this, null, null, Color.RED, Color.GREEN));	
		setProp("Friction",(Float).4f);
		setProp("Grabbable",(Integer)1);
		setProp("Rotation Speed", (Float).5f);
		setProp("Safety", (Integer)(1));
		setProp("Output", (Integer)(0));
	}

	public boolean inputActive(){
		if(mInputSrc == null) {
			return false;
		}

		Object input = mInputSrc.observe();
		return (input instanceof Boolean) && (Boolean)input;
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
//if there is input, poll it for permission to update
		if(Convert.getInt(getProp("Target Input"))!= -1 && inputActive() || Convert.getInt(getProp("Target Input"))== -1){	
			super.update();
			float rotation = Convert.getFloat(getProp("Rotation Speed"));
			mBody.setAngularVelocity(rotation);
			setProp("Output",(Integer)(1));
		} else {
			mBody.setAngularVelocity(0f);
			setProp("Output",(Integer)(0));
		}
		// If we could potentially crush something, stop
		if (Convert.getInt(getProp("Safety")) == 1 && CrushSensor.canCrush(mLevel.getContactHandler(), this)) {
			mBody.setAngularVelocity(0);
			setProp("Output",(Integer)(0));
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
	@Override
	public void postLoad() {
	}

	////////////////
	// Properties
	////////////////
	
	private IObservable mInputSrc;
	@Override
	public Actor inputSrc() {
		return (Actor)mInputSrc;
	}
	@Override
	public void inputSrc(Actor inputSrc) {
		mInputSrc = (inputSrc instanceof IObservable) ? (IObservable)inputSrc : null;
	}
}