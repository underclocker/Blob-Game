package org.siggd.actor;

import org.siggd.Game;
import org.siggd.Level;
import org.siggd.actor.meta.IObservable;
import org.siggd.actor.meta.IObserver;
import org.siggd.view.BodySprite;
import org.siggd.view.CompositeDrawable;
import org.siggd.view.DebugActorLinkDrawable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class Outpipe extends Actor implements IObserver {
	private String mTex;
	private String mOffTex;
	private BodySprite mDefaultDrawable;
	private BodySprite mOffDrawable;
	private boolean mState;

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

	public Outpipe(Level level, long id) {
		super(level, id);
		mName = "Outpipe colored";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		mOffTex = mTex.substring(0, mTex.length() - 9) + "red.png";
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 128, BodyType.StaticBody, origin, false);

		mDefaultDrawable = (new BodySprite(mBody, origin, mTex));
		mOffDrawable = (new BodySprite(mBody, origin, mOffTex));

		mState = true;
		((CompositeDrawable) mDrawable).mDrawables.add(mDefaultDrawable);
		((CompositeDrawable) mDrawable).mDrawables.add(new DebugActorLinkDrawable(this, null, null,
				Color.RED, Color.GREEN));

		this.setProp("Friction", (Float) .3f);
	}

	/**
	 * Load resources needed by the actor
	 */
	@Override
	public void loadResources() {
		AssetManager man = Game.get().getAssetManager();
		man.load(mTex, Texture.class);
		man.load(mOffTex, Texture.class);
	}

	@Override
	public void update() {
		setState(inputActive());
	}

	private void setState(boolean state) {
		if (state == mState)
			return;
		if (state) {
			mDrawable.mDrawables.remove(mOffDrawable);
			mDrawable.mDrawables.add(mDefaultDrawable);
		} else {
			mDrawable.mDrawables.remove(mDefaultDrawable);
			mDrawable.mDrawables.add(mOffDrawable);
		}
		mState = state;
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