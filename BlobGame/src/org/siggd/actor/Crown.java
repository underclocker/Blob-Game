package org.siggd.actor;

import java.util.ArrayList;

import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.Player;
import org.siggd.view.BodySprite;
import org.siggd.view.CompositeDrawable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

/**
 * I like too copy and paste
 * 
 * @author underclocker
 * 
 */
public class Crown extends Actor {
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
	public Crown(Level level, long id) {
		super(level, id);
		mName = "Crown";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 32, BodyType.DynamicBody, origin, true);
		((CompositeDrawable) mDrawable).mDrawables.add(new BodySprite(mBody, origin, mTex));

		mBody.setGravityScale(0);
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
		ArrayList<Blob> blobs = mLevel.getBlobs(false);
		Blob kingBlob = null;
		for (Blob b : blobs) {
			Player player = Game.get().getPlayer(b.getmPlayerID());
			if (player != null && player.mLeader) {
				kingBlob = b;
				break;
			}
		}
		if (kingBlob != null && mLevel.getBlobs(true).size() > 1) {
			Vector2 delta = mBody.getPosition().cpy();
			delta.sub(new Vector2(kingBlob.getX(), kingBlob.getY()));
			delta.add(new Vector2(0f, -.55f));
			delta.mul(-20f);
			Vector2 vel = new Vector2(Convert.getFloat(kingBlob.getProp("Velocity X")),
					Convert.getFloat(kingBlob.getProp("Velocity Y")));
			delta.add(vel);
			mBody.setLinearVelocity(delta);
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
