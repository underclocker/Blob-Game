package org.siggd.actor;

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

/**
 * I like to copy and paste
 * 
 * @author underclocker
 * 
 */
public class Redirector extends Actor {
	private String mTex;

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
	public Redirector(Level level, long id) {
		super(level, id);
		mName = "Redirector";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 32, BodyType.DynamicBody, origin, true);
		((CompositeDrawable) mDrawable).mDrawables.add(new BodySprite(mBody, origin, mTex));
		mBody.setGravityScale(0);
		setProp("Visible", (Integer) 0);
		setProp("Hold Time", (Integer) 0);
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
		for (Body b : bodies) {
			actor = (Actor) b.getUserData();
			if (actor instanceof Platform) {
				if (Convert.getInt(actor.getProp("Hold Time")) <= 0) {
					actor.setProp("Hold Time", Convert.getInt(getProp("Hold Time")));
				}
				Vector2 v = new Vector2(1, 0);
				v.rotate(Convert.getFloat(getProp("Angle")));
				v.scl(new Vector2(Convert.getFloat(actor.getProp("DirectionX")), Convert
						.getFloat(actor.getProp("DirectionY"))).len());
				Vector2 oldDir = new Vector2(Convert.getFloat(actor.getProp("DirectionX")),
						Convert.getFloat(actor.getProp("DirectionY")));
				if (!oldDir.equals(v)) {
					actor.setProp("DirectionX", (Float) v.x);
					actor.setProp("DirectionY", (Float) v.y);
				}
			}
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
	}
}
