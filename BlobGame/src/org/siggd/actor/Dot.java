package org.siggd.actor;

import java.util.ArrayList;

import org.box2dLight.PointLight;
import org.siggd.ContactHandler;
import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.StableContact;
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
public class Dot extends Actor {
	private String mTex;
	public Blob mTargetBlob;
	private int mEatTimer = 10;
	private PointLight mPointLight;

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
	public Dot(Level level, long id) {
		super(level, id);
		mName = "Dot";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 32, BodyType.DynamicBody, origin, false);
		((CompositeDrawable) mDrawable).mDrawables.add(new BodySprite(mBody, origin, mTex));

		setProp("Density", (Float) .3f);
		setProp("Friction", (Float) .1f);

		mBody.setGravityScale(0);
		mBody.setLinearDamping(.5f);
		mBody.setAngularDamping(.9f);

		mPointLight = new PointLight(Game.get().getLevelView().getRayHandler(), 32);
		mPointLight.setColor(.1f, .1f, .1f, .9f);
		mPointLight.setDistance(1f);
		mPointLight.setXray(true);
		mPointLight.attachToBody(mBody, 0, 0);
		mPointLight.setActive(false);
		setProp("Active", 1);
		setProp("Layer", 3);
		// TODO: set light active based on if actor is dummy or not
		setProp("X", -10000);

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
		if (mTargetBlob == null) {
			Iterable<StableContact> contacts = Game.get().getLevel().getContactHandler()
					.getContacts(this);
			Iterable<Actor> actors = ContactHandler.getActors(contacts);
			for (Actor a : actors) {
				if (a instanceof Blob) {
					mTargetBlob = (Blob) a;
					break;
				} else {
					mBody.setGravityScale(1);
				}
			}
		} else {
			Vector2 offset = new Vector2(mTargetBlob.getX(), mTargetBlob.getY());
			offset.sub(mBody.getPosition());
			offset.mul(5);
			mBody.applyForceToCenter(offset);
			mTargetBlob.applyForce(offset.mul(-1));
			mEatTimer--;
			if (mEatTimer < 0) {
				mBody.getFixtureList().get(0).setSensor(true);
			}
			if (mEatTimer < -5) {
				setProp("Active", 0);
				mTargetBlob.eatDot();
			}
		}
	}

	@Override
	public void setProp(String name, Object val) {
		if (name.equals("Active")) {
			mPointLight.setActive(Convert.getInt(val) != 0);
			setActive(Convert.getInt(val) != 0);
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
}
