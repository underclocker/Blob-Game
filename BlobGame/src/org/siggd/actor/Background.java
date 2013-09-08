package org.siggd.actor;

import org.siggd.Game;
import org.siggd.Level;
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
public class Background extends Actor {
	private String mTex;
	// BEGIN: EDITOR

	// True if this is being edited in the editor
	private boolean mIsInEditor;

	// END: EDITOR

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
	public Background(Level level, long id) {
		super(level, id);
		mName = "Background";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 128, BodyType.StaticBody, origin);
		((CompositeDrawable) mDrawable).mDrawables.add(new BodySprite(mBody, origin, mTex));
		mBody.setBullet(true);
		mProps.put("Body", "Background");
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
		AssetManager man = Game.get().getAssetManager();
		Vector2 origin = new Vector2();
		Texture tex = man.get(mTex, Texture.class);
		// Get old body position and angle
		Vector2 pos = mBody.getPosition();
		BodyType bt = mBody.getType();
		float ang = mBody.getAngle();

		// save friction and density will get reset with the new body,
		// this is the idea location to save and reapply them
		// TODO: save and reaaply other important body properties
		float friction = getFriction();
		float density = getDensity();
		float restitution = getRestitution();
		int collisiongrp = getProp("Collision Group");

		// Remove old body
		getLevel().getWorld().destroyBody(mBody);

		// Make new body
		mBody = makeBody((String) getProp("Body"), tex.getWidth(), bt, origin);

		// Translate body
		mBody.setTransform(pos, ang);

		// reapply saved properties
		setProp("Friction", friction);
		setProp("Density", density);
		setProp("Restitution", restitution);
		setProp("Collision Group", collisiongrp);

		int size = ((CompositeDrawable) mDrawable).mDrawables.size();

		// remove old bodysprite
		for (int i = 0; i < size; i++) {
			Drawable d = ((CompositeDrawable) mDrawable).mDrawables.get(i);
			if (d instanceof BodySprite) {
				((CompositeDrawable) mDrawable).mDrawables.remove(d);
				break;
			}
		}
		((CompositeDrawable) mDrawable).mDrawables.add(new BodySprite(mBody, origin, mTex));
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
	public void setProp(String name, Object val) {
		if ("Body".equals(name)) {
			mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath((String) val);

			super.setProp(name, val);

			// BEGIN: EDITOR
			if (mIsInEditor) {
				loadResources();
				Game.get().getAssetManager().finishLoading();
				loadBodies();
			}
			// END: EDITOR
		} else {
			super.setProp(name, val);
		}
	}

	// BEGIN: EDITOR
	/**
	 * Notifies the Background that it is currently in the editor
	 */
	public void setInEditor() {
		mIsInEditor = true;
	}

	// END: EDITOR

	@Override
	public void postLoad() {
		// TODO Auto-generated method stub

	}
}
