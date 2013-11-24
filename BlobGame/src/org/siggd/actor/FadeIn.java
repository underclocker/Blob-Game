package org.siggd.actor;

import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.view.CompositeDrawable;
import org.siggd.view.Drawable;
import org.siggd.view.LevelView;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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
public class FadeIn extends Actor {
	private String mTex;
	private float mRotation = 0;
	private FadeInDrawable mFIDrawable;

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
	private class FadeInDrawable implements Drawable {

		private float alpha = 1;

		@Override
		public void drawSprite(SpriteBatch batch) {
			if (Convert.getInt(getProp("Visible")) == 0)
				return;
			if (alpha >= Convert.getFloat(getProp("Stop")))
				alpha -= .05f;
			if (alpha <= 0) {
				return;
			}

			if (batch == null)
				return;
			AssetManager assMan = Game.get().getAssetManager();
			if (!assMan.isLoaded(mTex))
				return;
			Texture tex = assMan.get(mTex, Texture.class);

			LevelView lv = Game.get().getLevelView();
			Vector2 center = lv.getLevelCenter();
			Vector2 pos = lv.getCameraPosition();
			Vector2 offset = center.cpy().sub(pos);

			float distFromMainLayer = lv.getScale() * 100.0f;
			float scale = distFromMainLayer
					/ (distFromMainLayer + Convert.getFloat(getProp("Distance")));
			offset.scl(scale);
			scale *= Convert.getFloat(getProp("Scale"));
			float halfwidth = tex.getWidth() / 2f / lv.getVScale() * scale;
			float halfheight = tex.getHeight() / 2f / lv.getVScale() * scale;
			batch.setColor(1, 1, 1, alpha);
			batch.draw(tex, pos.x - halfwidth + offset.x + Convert.getFloat(getProp("X Offset"))
					* scale, pos.y - halfheight + offset.y + Convert.getFloat(getProp("Y Offset"))
					* scale, halfwidth, halfheight, halfwidth * 2, halfheight * 2, 1f, 1f,
					mRotation, 0, 0, tex.getWidth(), tex.getHeight(), false, false);
			batch.setColor(Color.WHITE);
		}

		@Override
		public void drawElse(ShapeRenderer shapeRender) {
		}

		@Override
		public void drawDebug(Camera camera) {
		}

	}

	public FadeIn(Level level, long id) {
		super(level, id);
		mName = "Background";
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 128, BodyType.StaticBody, origin, true);
		mBody.setActive(false);
		setProp("Texture", "black.png");
		setProp("Distance", (Float) 0f);
		setProp("Scale", (Float) 100f);
		setProp("Layer", 101);
		setProp("Stop", .0f);
		setProp("X Offset", 0);
		setProp("Y Offset", 0);
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

	public boolean fadedOut() {
		return mFIDrawable.alpha <= 0;
	}

	@Override
	public void setProp(String name, Object val) {
		if (name.equals("Texture")) {
			Drawable oldDrawable = null;
			int size = ((CompositeDrawable) mDrawable).mDrawables.size();
			// remove old bodysprite
			for (int i = 0; i < size; i++) {
				Drawable d = ((CompositeDrawable) mDrawable).mDrawables.get(i);
				if (d instanceof FadeInDrawable) {
					oldDrawable = d;
					((CompositeDrawable) mDrawable).mDrawables.remove(d);
					break;
				}
			}
			if (oldDrawable != null) {
				AssetManager man = Game.get().getAssetManager();
				man.unload(mTex);
			}
			mTex = "data/gfx/" + val;
			loadResources();
			mFIDrawable = new FadeInDrawable();
			((CompositeDrawable) mDrawable).mDrawables.add(mFIDrawable);
		}
		super.setProp(name, val);
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
		if (man.containsAsset(mTex)) {
			man.unload(mTex);
		}
	}

	@Override
	public void postLoad() {
	}
}