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
 * This class attaches a sprite to a body
 */
public class BodySprite implements Drawable {
	// The body
	private Body mBody;
	// The origin point of the sprite
	private Vector2 mOrigin;
	// The textures string
	private String mTexString;
	private float mScale = 1;

	public BodySprite(Body body, Vector2 origin, String texString) {
		mBody = body;
		mOrigin = origin;
		mTexString = texString;
	}
	
	public BodySprite(Body body, Vector2 origin, String texString, float scale){
		this(body,origin,texString);
		mScale = scale;
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
		float scale = Game.get().getLevelView().getVScale() / mScale;

		float x = mBody.getPosition().x;
		float y = mBody.getPosition().y;
		float angle = mBody.getAngle();

		if (man.isLoaded(mTexString)) {
			Texture tex = man.get(mTexString, Texture.class);

			batch.draw(tex, x - mOrigin.x, y - mOrigin.y, mOrigin.x, mOrigin.y, tex.getWidth()
					/ scale, tex.getHeight() / scale, 1, 1, angle / (float) Math.PI * 180f, 0, 0,
					tex.getWidth(), tex.getHeight(), false, false);
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
