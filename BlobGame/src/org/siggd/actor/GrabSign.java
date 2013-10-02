package org.siggd.actor;

import org.siggd.Game;
import org.siggd.Level;
import org.siggd.view.Animation;
import org.siggd.view.BodySprite;
import org.siggd.view.CompositeDrawable;
import org.siggd.view.Drawable;

import com.badlogic.gdx.assets.AssetManager;
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
public class GrabSign extends Actor {
	private String mTex;
	private Animation mAnimation;
	private Drawable frame;

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
	public GrabSign(Level level, long id) {
		super(level, id);
		mName = "BlobSensor";
		mTex = "data/gfx/Tutorial_Solidity001.png";
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 512, BodyType.StaticBody, origin, true);
		frame = new BodySprite(mBody, origin, mTex);
		((CompositeDrawable) mDrawable).mDrawables.add(frame);
		mAnimation = new Animation(mBody, origin);
		mAnimation.mTicksPerFrame = 60;
		mAnimation.addFrame("data/gfx/Tutorial_Solidity001.png");
		mAnimation.addFrame("data/gfx/Tutorial_Solidity002.png");
		mAnimation.addFrame("data/gfx/Tutorial_Solidity003.png");
	}

	/**
	 * Load resources needed by the actor
	 */
	@Override
	public void loadResources() {
		AssetManager man = Game.get().getAssetManager();
		man.load(mTex, Texture.class);
		man.load("data/gfx/Tutorial_Solidity001.png", Texture.class);
		man.load("data/gfx/Tutorial_Solidity002.png", Texture.class);
		man.load("data/gfx/Tutorial_Solidity003.png", Texture.class);
	}

	/**
	 * Load bodies needed by the actor
	 */
	@Override
	public void loadBodies() {
	}

	@Override
	public void update() {
		mAnimation.update();
		((CompositeDrawable) mDrawable).mDrawables.remove(frame);
		frame = mAnimation.getCurFrame();
		((CompositeDrawable) mDrawable).mDrawables.add(frame);
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
	public void postLoad() {
	}
}
