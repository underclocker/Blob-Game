package org.siggd.view;

import org.siggd.Game;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * This holds a sprite
 */
public class Sprite implements Drawable {
	public Vector2 mPosition;
	public float mAngle;
	// The origin point of the sprite
	private Vector2 mOrigin;
	// The textures string
	private String mTexString;

	public Sprite(Vector2 position, Vector2 origin, String texString) {
		mOrigin = origin;
		mTexString = texString;
		mPosition = position;
	}

	public Sprite(String texString) {
		this(new Vector2(), new Vector2(), texString);
	}

	/**
	 * Draws a sprite, using a shared SpriteBatch
	 * 
	 * @param batch
	 *            The shared SpriteBatch
	 */
	@Override
	public void drawSprite(SpriteBatch batch) {

		// Get the Asset Manager
		AssetManager man = Game.get().getAssetManager();

		// Get Scale
		float scale = Game.get().getLevelView().getVScale();

		if (man.isLoaded(mTexString)) {
			Texture tex = man.get(mTexString, Texture.class);
			batch.draw(tex, mPosition.x - mOrigin.x, mPosition.y - mOrigin.y, mOrigin.x, mOrigin.y,
					tex.getWidth() / scale, tex.getHeight() / scale, 1, 1, mAngle, 0, 0, tex.getWidth(), tex.getHeight(), false, false);
		}
	}

	/**
	 * Does nothing
	 */
	@Override
	public void drawElse(ShapeRenderer shapeRenderer) {
	}

	/**
	 * Does nothing
	 */
	@Override
	public void drawDebug(Camera camera) {
	}
}
