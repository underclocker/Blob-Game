package org.siggd.actor;

import java.util.ArrayList;

import org.siggd.ContactHandler;
import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.StableContact;
import org.siggd.actor.meta.IObservable;
import org.siggd.actor.meta.IObserver;
import org.siggd.view.BodySprite;
import org.siggd.view.CompositeDrawable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;

/**
 * I like too copy and paste
 * 
 * @author underclocker
 * 
 */
public class JiggleBall extends Actor implements IObserver {
	private String mTex;
	private Vector2 mStartPosition;

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
	public JiggleBall(Level level, long id) {
		super(level, id);
		mName = "Circle";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 128, BodyType.DynamicBody, origin, false);
		((CompositeDrawable) mDrawable).mDrawables.add(new BodySprite(mBody, origin, mTex));

		mStartPosition = new Vector2();
		setProp("Density", (Float) .3f);
		setProp("Friction", (Float) .3f);

		mBody.setGravityScale(0);
		mBody.setLinearDamping(.5f);
		mBody.setAngularDamping(.9f);
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

	@Override
	public void update() {
		Vector2 offset = mBody.getPosition().cpy();
		offset.sub(mStartPosition);
		offset.scl(-30);
		if (offset.len() > 15) {
			offset.nor().scl(15);
		}
		mBody.applyForceToCenter(offset, true);
		if (mInputSrc != null) {
			Object input = mInputSrc.observe();
			if ((input instanceof Boolean) && !(Boolean)input) {
				mBody.applyForceToCenter(new Vector2(0, -20), true);
			}
		}
	}

	@Override
	public void setProp(String name, Object val) {
		if (name.equals("X")) {
			mStartPosition.x = Convert.getFloat(val);
		}
		if (name.equals("Y")) {
			mStartPosition.y = Convert.getFloat(val);
		}
		super.setProp(name, val);
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
