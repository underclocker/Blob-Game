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
public class Cutscene extends Actor {
	private String mTex;
	private float mRotation = 0;

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
	private class CutsceneDrawable implements Drawable {

		private float alpha = 0;
		private float frame = 0;
		private float xpan = 0;
		private float ypan = 0;
		private float zpan = 1;

		@Override
		public void drawSprite(SpriteBatch batch) {
			if (Game.get().getState() == Game.PLAY) {
				frame++;
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

			if (frame > Convert.getFloat(getProp("Start"))
					&& frame < Convert.getFloat(getProp("End"))) {
				alpha += .0075f;
				if (alpha > 1) {
					alpha = 1;
				}
			} else {
				alpha -= .0075f;
				if (alpha < 0) {
					alpha = 0;
				}
			}
			if (frame > Convert.getFloat(getProp("Start"))) {
				xpan += Convert.getFloat(getProp("X Pan"));
				ypan += Convert.getFloat(getProp("Y Pan"));
				zpan += Convert.getFloat(getProp("Z Pan"));
			}
			pos.x += xpan;
			pos.y += ypan;
			float distFromMainLayer = lv.getScale() * 100.0f;

			float scale = distFromMainLayer
					/ (distFromMainLayer + Convert.getFloat(getProp("Distance")));
			scale /= zpan;
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

	public Cutscene(Level level, long id) {
		super(level, id);
		mName = "Background";
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 128, BodyType.StaticBody, origin, true);
		mBody.setActive(false);
		setProp("Texture", "intro.png");
		setProp("Distance", (Float) 0f);
		setProp("Scale", (Float) 1f);
		setProp("Rotation Speed", (Float) 0f);
		setProp("Rotation", (Float) 0f);
		setProp("Layer", -1);
		setProp("X Offset", 0);
		setProp("Y Offset", 0);
		setProp("Start", 60);
		setProp("End", 120);
		setProp("X Pan", .000f);
		setProp("Y Pan", .000f);
		setProp("Z Pan", .000f);
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
		/*
		 * Texture tex = Game.get().getAssetManager().get(mTex); Vector2 origin
		 * = new Vector2(); mBody = makeBody(mName, tex.getHeight(),
		 * BodyType.StaticBody, origin, false); mDrawable = new
		 * BodySprite(mBody, origin, mTex);
		 */
	}

	@Override
	public void setProp(String name, Object val) {
		if (name.equals("Texture")) {
			Drawable oldDrawable = null;
			int size = ((CompositeDrawable) mDrawable).mDrawables.size();
			// remove old bodysprite
			for (int i = 0; i < size; i++) {
				Drawable d = ((CompositeDrawable) mDrawable).mDrawables.get(i);
				if (d instanceof CutsceneDrawable) {
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
			((CompositeDrawable) mDrawable).mDrawables.add(new CutsceneDrawable());
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