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
 * I like to copy and paste
 * 
 * @author underclocker
 * 
 */
public class StickyBot extends Actor {
	private String mTex;
	private Fixture mOuterBall;
	private Vector2 mStartPosition;
	private Fixture mMainFixture;

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
	public StickyBot(Level level, long id) {
		super(level, id);
		mName = "Circle";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 192, BodyType.DynamicBody, origin, false);
		((CompositeDrawable) mDrawable).mDrawables.add(new BodySprite(mBody, origin, mTex, 1.5f));

		mStartPosition = new Vector2();

		CircleShape circle = new CircleShape();
		circle.setPosition(new Vector2(0, 0));
		circle.setRadius(3.8f);
		// Create a fixtureDef
		FixtureDef fd = new FixtureDef();
		fd.shape = circle;
		fd.density = 0f;
		fd.friction = 0f;
		fd.isSensor = true;

		mOuterBall = mBody.createFixture(fd);

		setProp("Density", (Float) 2f);
		setProp("Friction", (Float) .5f);

		mBody.setGravityScale(0);
		mBody.setLinearDamping(.5f);
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
		Iterable<Actor> actors = ContactHandler.getActors(contacts);
		boolean enemySeen = false;
		for (Actor a : actors) {
			if (a instanceof Blob) {
				enemySeen = true;
				Blob blob = (Blob) a;
				Vector2 blobPos = new Vector2(blob.getX(), blob.getY());
				Vector2 offset = mBody.getPosition().cpy();
				offset.sub(blobPos);
				offset.nor();
				offset.mul(-5);
				mBody.applyForceToCenter(offset);
			}
		}
		if (!enemySeen) {
			Vector2 offset = mBody.getPosition().cpy();
			offset.sub(mStartPosition);
			offset.mul(-1);
			offset.nor();
			mBody.applyForceToCenter(offset);
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
		if(man.containsAsset(mTex)) {
			man.unload(mTex);
		}
	}

	@Override
	public void postLoad() {
	}
}
