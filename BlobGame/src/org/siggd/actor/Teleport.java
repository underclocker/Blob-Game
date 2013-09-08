package org.siggd.actor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.siggd.ContactHandler;
import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.StableContact;
import org.siggd.view.BodySprite;
import org.siggd.view.CompositeDrawable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

/**
 * This class can be used as a convenient shortcut for any of the bodies in
 * bodies.json. Just set the Body property to the appropriate body, and the
 * actor will become that body.
 * 
 * @author mysterymath
 * 
 */
public class Teleport extends Actor {
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
	public Teleport(Level level, long id) {
		super(level, id);
		mName = "Teleport";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 128, BodyType.StaticBody, origin, true);
		((CompositeDrawable) mDrawable).mDrawables.add(new BodySprite(mBody, origin, mTex));
		setProp("Visible", (Integer) 0);
		setProp("Teleport_XPos", (Float) 0f);
		setProp("Teleport_YPos", (Float) 0f);
		setProp("Level", "");
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
		for (Actor a : actors) {
			if (!a.isActive())
				continue;
			if (a instanceof Blob) {
				a.setActive(false);
				if (mLevel.mFirstBlobFinished == null) {
					mLevel.mFirstBlobFinished = (Blob) a;

				}
				if (!aBlobIsActive()) {
					if (mLevel.mFirstBlobFinished != null) {
						mLevel.mFirstBlobFinished = (Blob) a;
					}
					changeLevel();
				}
			} else {
				a.setProp("X", Convert.getFloat(getProp("Teleport_XPos")));
				a.setProp("Y", Convert.getFloat(getProp("Teleport_YPos")));
			}
		}
	}

	private boolean aBlobIsActive() {
		ArrayList<Actor> actors = Game.get().getLevel().getActors();
		for (Actor a : actors) {
			if ((a instanceof Blob) && a.isActive()) {
				return true;
			}
		}
		return false;
	}

	private void changeLevel() {
		if (this.getProp("Level").equals("")) {
			System.out.println("Invalid Level!");
			return;
		}
		String nextLevel = (String) this.getProp("Level");
		Game.get().getLevel().saveToLevelSave(nextLevel + "Unlocked", 1);
		Game.get().setNextLevel(nextLevel);
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
