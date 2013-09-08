package org.siggd.view;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * This drawable is a simple collection of other Drawables
 * 
 * @author mysterymath
 * 
 */
public class CompositeDrawable implements Drawable {
	// Contained drawables
	public ArrayList<Drawable> mDrawables;

	/**
	 * Constructor
	 */
	public CompositeDrawable() {
		mDrawables = new ArrayList<Drawable>();
	}

	/**
	 * Dispatch down to contained Drawables
	 * 
	 * @param batch
	 *            The shared spritebatch
	 */
	@Override
	public void drawSprite(SpriteBatch batch) {
		for (Drawable d : mDrawables) {
			d.drawSprite(batch);
		}
	}

	/**
	 * Dispatch down to contained Drawables
	 */
	@Override
	public void drawElse(ShapeRenderer shapeRender) {
		for (Drawable d : mDrawables) {
			d.drawElse(shapeRender);
		}
	}

	/**
	 * Dispatch down to contained Drawables
	 */
	@Override
	public void drawDebug(Camera camera) {
		for (Drawable d : mDrawables) {
			d.drawDebug(camera);
		}
	}
}
