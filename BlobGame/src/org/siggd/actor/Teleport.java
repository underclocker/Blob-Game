package org.siggd.actor;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.siggd.ContactHandler;
import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.LevelGen;
import org.siggd.StableContact;
import org.siggd.view.BodySprite;
import org.siggd.view.CompositeDrawable;

import com.badlogic.gdx.assets.AssetManager;
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
	private int mTimer = 0;

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
		setProp("Extra Level", "");
		setProp("Timer", -1);
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
		mTimer++;
		int timer = Convert.getInt(getProp("Timer"));
		if (timer != -1 && mTimer > timer) {
			changeLevel();
		}
		Iterable<StableContact> contacts = Game.get().getLevel().getContactHandler()
				.getContacts(this);
		Iterable<Actor> actors = ContactHandler.getActors(contacts);
		for (Actor a : actors) {
			if (!a.isActive())
				continue;
			if (a instanceof Blob) {
				Blob b = (Blob) a;
				a.setActive(false);
				b.mFinishedLevel = true;
				if (!aBlobIsPlaying()) {
					changeLevel();
				}
			} else {
				a.setProp("X", Convert.getFloat(getProp("Teleport_XPos")));
				a.setProp("Y", Convert.getFloat(getProp("Teleport_YPos")));
			}
		}
	}

	private boolean aBlobIsPlaying() {
		ArrayList<Actor> actors = Game.get().getLevel().getActors();
		for (Actor a : actors) {
			if ((a instanceof Blob)
					&& (!((Blob) a).mFinishedLevel && ((Blob) a).getmPlayerID() >= 0)) {
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
		String extraLevel = (String) this.getProp("Extra Level");
		JSONObject levelSave = Game.get().getLevel().getLevelSave();
		if (nextLevel != null && !"".equals(nextLevel)) {
			JSONObject level;
			try {
				level = levelSave.getJSONObject(nextLevel);
			} catch (JSONException e) {
				// level does not exist yet
				level = new JSONObject();
			}
			try {
				level.put("unlocked", true);
			} catch (JSONException e) {
				// Should Never Happen
				System.out.println("Congrats! You have achieved the Impossible!");
			}
			Game.get().getLevel().saveToLevelSave(nextLevel, level);
		}
		if (extraLevel != null && !"".equals(extraLevel)) {
			JSONObject level;
			try {
				level = levelSave.getJSONObject(extraLevel);
			} catch (JSONException e) {
				// level does not exist yet
				level = new JSONObject();
			}
			try {
				level.put("unlocked", true);
			} catch (JSONException e) {
				// Should Never Happen
				System.out.println("Congrats! You have achieved the Impossible!");
			}
			Game.get().getLevel().saveToLevelSave(extraLevel, level);
		}
		if ("gen".equals(nextLevel))
			LevelGen.Difficulty += 1.0f;
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
