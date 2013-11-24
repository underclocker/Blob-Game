package org.siggd.actor;

import java.util.ArrayList;

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
public class BattleBall extends Actor {
	private String mTex;
	private Fixture mOuterBall;

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
	public BattleBall(Level level, long id) {
		super(level, id);
		mName = "lightbulb";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 256, BodyType.DynamicBody, origin, false);
		((CompositeDrawable) mDrawable).mDrawables.add(new BodySprite(mBody, origin, mTex));

		CircleShape circle = new CircleShape();
		circle.setPosition(new Vector2(0, 0));
		circle.setRadius(.8f);

		// Create a fixtureDef
		FixtureDef fd = new FixtureDef();
		fd.shape = circle;
		fd.density = 0f;
		fd.friction = 0f;
		fd.filter.groupIndex = -1;

		mOuterBall = mBody.createFixture(fd);

		setProp("Density", (Float) .3f);
		setProp("Grabbable", (Integer) 1);
		setProp("Friction", (Float) .5f);
		setProp("Grabbers", (Integer) 0);

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
		Iterable<StableContact> contacts = Game.get().getLevel().getContactHandler()
				.getContacts(this);
		Iterable<Body> bodies = ContactHandler.getBodies(contacts);
		Actor actor;
		ArrayList<Blob> blobs = getLevel().getBlobs(true);
		boolean piloted = false;
		if (Convert.getInt(getProp("Grabbers")) > 0) {
			for (Blob b : blobs) {
				if (b.isSolid()) {
					Vector2 offset = b.mBody.getPosition().cpy();
					offset.sub(mBody.getPosition());
					if (offset.len() < .3) {
						piloted = true;
						if (mBody.getAngularVelocity() < 3 && b.getDirection() == -1) {
							mBody.applyAngularImpulse(1f, true);
						} else if (mBody.getAngularVelocity() > -3 && b.getDirection() == 1) {
							mBody.applyAngularImpulse(-1f, true);
						}
						mBody.applyForceToCenter(new Vector2(b.getDirection() * 20f, 0), true);
					}
				}
			}
		}
		if (!piloted) {
			setProp("Friction", .5f);
			mOuterBall.setFriction(.5f);
		} else {
			setProp("Friction", 5f);
			mOuterBall.setFriction(5f);
		}
	}

	/**
	 * Dispose of the actor's resources
	 */
	@Override
	public void dispose() {
		AssetManager man = Game.get().getAssetManager();
		if(man.containsAsset(mTex)) {
			man.unload(mTex);
		}
	}

	@Override
	public void postLoad() {
		// TODO Auto-generated method stub
	}
}
