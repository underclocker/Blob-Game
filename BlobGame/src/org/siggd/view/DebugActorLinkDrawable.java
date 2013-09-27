package org.siggd.view;

import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.actor.Actor;
import org.siggd.actor.meta.IObservable;
import org.siggd.actor.meta.IObserver;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;

/**
 * This class draws a debug line to the actor pointed to by a property
 * 
 * @author mysterymath (that's your name dummy)
 * 
 */
public class DebugActorLinkDrawable implements Drawable {
	private Actor mActor;
	private String mPropName;
	private String mVariableProp;
	private Color mColor;
	private Color mOffColor;
	private Vector2 mOffSet;
	private ShapeRenderer mSRend;

	/**
	 * 
	 * @param actor
	 *            Actor that polls the actor defined by propName
	 * @param propName
	 *            Property that defines actor the actor to poll for variable
	 *            Prop
	 * @param variableProp
	 *            Property that is polled, typically "Output"
	 * @param color
	 *            Color to draw when variableProp is 0
	 * @param offColor
	 *            Color to draw when variableProp is 1
	 * @param offSet
	 *            Vector2 offset from {actor}'s body position, in pixels
	 */
	public DebugActorLinkDrawable(Actor actor, String propName,
			String variableProp, Color color, Color offColor, Vector2 offSet) {
			mActor = actor;
			mPropName = propName;
			mColor = color;
			mOffColor = offColor;
			mOffSet = offSet.scl((float) (1.0 / Game.get().getLevelView()
					.getVScale())); // offset is in pixels
			mVariableProp = variableProp;
	}

	public DebugActorLinkDrawable(Actor actor, String propName,
			String variableProp, Color color, Color offColor) {
		this(actor, propName, variableProp, color, offColor, Vector2.Zero.cpy());
	}

	public DebugActorLinkDrawable(Actor actor, String propName,
			String variableProp, Color color) {
		this(actor, propName, variableProp, color, color, Vector2.Zero.cpy());
	}

	public DebugActorLinkDrawable(Actor actor, String propName, Color color,
			Vector2 offSet) {
		this(actor, propName, null, color, color, offSet);
	}

	public DebugActorLinkDrawable(Actor actor, String propName, Color color) {
		this(actor, propName, null, color, color, Vector2.Zero.cpy());
	}

	public DebugActorLinkDrawable(Actor actor, String propName) {
		this(actor, propName, Color.BLACK);
	}

	/**
	 * Does nothing
	 */
	@Override
	public void drawSprite(SpriteBatch batch) {
	}

	/**
	 * Does nothing
	 */
	@Override
	public void drawElse(ShapeRenderer shapeRenderer) {
		mSRend =shapeRenderer;
	}

	/**
	 * Draws the debug line
	 */
	@Override
	public void drawDebug(Camera camera) {
		if (!Gdx.graphics.isGL20Available())
			return;

		if (mActor == null)
			return;

		Actor b;
		if (mPropName != null) {
			if (!mActor.hasProp(mPropName))
				return;
		
			b = mActor.getLevel().getActorById(
					Convert.getInt(mActor.getProp(mPropName)));
		} else {
			// Find other actor using IObserver
			if (!(mActor instanceof IObserver))
				return;
			
			b = ((IObserver)mActor).inputSrc();
		}

		if (b == null)
			return;
		
		// Draw the line
		Vector2 aPos = mActor.getMainBody().getPosition().cpy().add(mOffSet);
		Vector2 bPos = b.getMainBody().getPosition();

		Color drawColor = mColor;
		if (mVariableProp != null) {
			if (Convert.getInt(b.getProp(mVariableProp)) == 1) {
				drawColor = mOffColor;
			}
		} else if (b instanceof IObservable) {
			Object state = ((IObservable)b).observe();
			if (state instanceof Boolean && (Boolean)state) {
				drawColor = mOffColor;
			}
		}

		mSRend.setColor(drawColor);
		mSRend.setProjectionMatrix(camera.combined);
		mSRend.begin(ShapeType.Line);
		mSRend.line(aPos.x, aPos.y, bPos.x, bPos.y);
		mSRend.end();
	}

	public String getPropName() {
		return mPropName;
	}
}
