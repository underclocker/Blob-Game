package org.siggd.view;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * This interface represents an object that can be drawn on-screen
 * @author mysterymath
 *
 */
public interface Drawable {
	/**
	 * Draws a sprite, using a shared SpriteBatch
	 * @param batch The shared SpriteBatch
	 */
	public void drawSprite(SpriteBatch batch);
	
	/**
	 * Draws something else (not a sprite)
	 */
	public void drawElse(ShapeRenderer shapeRenderer);
	
	/**
	 * Draws debug information (tied to Editor Debug Mode)
	 */
	public void drawDebug(Camera camera);
}
