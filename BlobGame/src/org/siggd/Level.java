package org.siggd;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.siggd.actor.Actor;
import org.siggd.actor.Blob;
import org.siggd.actor.Door;
import org.siggd.actor.Dot;
import org.siggd.actor.FadeIn;
import org.siggd.actor.Spawner;
import org.siggd.actor.meta.ActorEnum;
import org.siggd.view.LevelView;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

/**
 * This class represents a game world, or map.
 * 
 * Level coordinates are equal to screen coordinates (1-1 mapping) The origin is
 * 0,0, which is the lower left corner of the map (just like in math.)
 * Accordingly, all the math formulas can be used without modification
 * (hooray!).
 * 
 * @author mysterymath
 * 
 */
public class Level implements Iterable<Actor> {
	public static final String SAVE_FILE = ".BlobGame/BlobSave.json";
	// Static array of level names for locking levels
	public static String[] LEVELS = { "level1", "level7", "level5", "level3", "level4", "level2",
			"level8" };
	public static String MEDIUM_SUFFIX = "_med";
	public static String HARD_SUFFIX = "_hard";
	public static boolean MEDEASY_COMPLETE = false;
	public static boolean EASY_PASSED = false;
	public static boolean HARD_PASSED = false;
	public static boolean COMPLETE = false;
	public static boolean FIRSTCHECK = true;

	public static final float PHYSICS_SCALE = 3f;

	// Array of actors
	ArrayList<Actor> mActors;
	// Level Properties
	HashMap<String, Object> mProps;
	// The next id to be assigned to an actor
	private long mNextId;
	// Box2D Physics Environment
	private World mWorld;
	private ArrayList<Body> mBodiesToDestroy;
	private ContactHandler mContactHandler;
	private String mAssetKey;
	private float mVolume = 0.5f;

	float mCurrentVolume = 0f;
	private float mFadeRate = 0.02f;
	private int mMusicTick = 0;

	Music mMusic;
	Music nMusic = null;
	private LinkedList<Actor> mAddQueue;
	public Blob mFirstBlobFinished = null;
	private float mAmbientLight;
	private JSONObject mLevelSave;
	private static final Logger mLog = Logger.getLogger(Level.class.getName());

	/**
	 * Constructor
	 */
	public Level(String assetKey) {
		mAssetKey = assetKey;
		// Create actor array
		mActors = new ArrayList<Actor>();
		mAddQueue = new LinkedList<Actor>();
		mProps = new HashMap<String, Object>();
		mBodiesToDestroy = new ArrayList<Body>();
		// X Gravity
		mProps.put(Prop.GRAVITY_X, (float) 0.0);

		// Y Gravity
		mProps.put(Prop.GRAVITY_Y, (float) -9.8);

		// Minimum Camera X position
		mProps.put(Prop.MIN_CAMERA_X, (float) Integer.MIN_VALUE);

		// Minimum Camera Y position
		mProps.put("Min Camera Y", (float) Integer.MIN_VALUE);

		// Maximum Camera X position
		mProps.put("Max Camera X", (float) Integer.MAX_VALUE);

		// Maximum Camera Y position
		mProps.put("Max Camera Y", (float) Integer.MAX_VALUE);

		// Whether puff enabled
		mProps.put("Puff Enabled", (int) 1);

		// Whether solidity enabled
		mProps.put("Solidity Enabled", (int) 1);

		// Background Music
		mProps.put("SongName", "");

		// Background Image
		mProps.put("Parallax", "");

		mProps.put("Use Light", 1);

		mProps.put("Difficulty", 0);

		setProp("Ambient Light", .3f);
		mNextId = 0;

		// Initialize the world;
		mWorld = new World(new Vector2((float) 0, (float) -9.8), true);
		mContactHandler = new ContactHandler();
		mWorld.setContactListener(mContactHandler);
		mContactHandler.addListener(new BlobDetangler());
	}

	public void killFade() {
		for (Actor a : mActors) {
			if (a instanceof FadeIn) {
				FadeIn fi = (FadeIn) a;
				fi.setVisible(0);
			}
		}
	}

	public void startMusic() {
		/*
		 * for (Actor a : mActors) { if (a instanceof FadeIn &&
		 * !((FadeIn)a).fadedOut()) { return; } }
		 */

		// Music nMusic = null;
		mMusicTick++;
		// Inherited a song
		if (mMusic != null) {
			AssetManager man = Game.get().getAssetManager();
			String musicPath = "data/mus/" + (String) getProp("SongName");
			try {
				if (nMusic == null) {
					String extension = musicPath.substring(musicPath.length() - 4,
							musicPath.length());
					if (".wav".equals(extension) || ".mp3".equals(extension)
							|| ".ogg".equals(extension)) {
						man.load(musicPath, Music.class);
						man.finishLoading();
						nMusic = man.get(musicPath, Music.class);

					}
				}
			} catch (Exception e) {
			}
			// If the music was changed to a nonexistent song in realtime, stop
			// the music
			if (!man.isLoaded(musicPath)) {
				mMusic = null;
			}
		}

		// Didn't inherit a song
		if (mMusic == null) {
			AssetManager man = Game.get().getAssetManager();
			String musicPath = "data/mus/" + (String) getProp("SongName");

			if (!((String) getProp("SongName")).equals("")) {
				try {
					String extension = musicPath.substring(musicPath.length() - 4,
							musicPath.length());
					if (".wav".equals(extension) || ".mp3".equals(extension)
							|| ".ogg".equals(extension)) {
						man.load(musicPath, Music.class);
						man.finishLoading();
						mMusic = man.get(musicPath, Music.class);
						mMusic.stop();
					}
				} catch (Exception e) {
					mMusic = null;
					// e.printStackTrace();
					// DebugOutput.info(new Object(), (String)
					// getProp("SongName"));
				}
			}
		}

		// Inherited a song, need to switch to new one
		if (nMusic != null && mMusic.hashCode() != nMusic.hashCode() && !nMusic.isPlaying()) {
			mCurrentVolume -= mFadeRate;
			if (mCurrentVolume >= 0f) {
				if (!mMusic.isPlaying()) {
					mMusic.play();
					mMusicTick = 0;
				}
				mMusic.setVolume(mCurrentVolume);
			} else {
				mMusic.stop();
				nMusic.setVolume(0);
				mMusic = nMusic;
				nMusic = null;
			}
		} else if (mMusic != null) {
			// mCurrentVolume = mVolume;
			mCurrentVolume += mFadeRate;
			if (mCurrentVolume >= mVolume)
				mCurrentVolume = mVolume;
			if (!mMusic.isPlaying()) {
				mMusic.play();
				mMusicTick = 0;
			}
			mMusic.setVolume(mCurrentVolume);
		}
		if (mMusic != null) {
			int mill = Math.round(mMusic.getPosition() * 5 * 60 / 3);
			if (mill % 60 == 0)
				mMusicTick = 0;
		}
	}

	public void stopMusic() {
		if (mMusic != null && mMusic.isPlaying()) {
			mMusic.pause();
		}
	}

	public void update() {
		if (Game.get().getState() == Game.PLAY || Game.get().getState() == Game.MENU) {

			startMusic();
			if (!musicTick()) {
				Dot.ATE_DOT = false;
				Dot.SLURP_DOT = false;
			}
			Dot.ONTIME_EAT++;
			Door.PLAYED = false;

			while (mBodiesToDestroy.size() > 0) {
				getWorld().destroyBody(mBodiesToDestroy.remove(0));
			}
			// Begin the step.

			for (int i = 0; i < PHYSICS_SCALE; i++) {
				mWorld.step(1 / (60f * PHYSICS_SCALE), 8, 3);
			}
			Actor a;
			for (Iterator<Actor> actor = mActors.iterator(); actor.hasNext();) {
				a = actor.next();
				if (a.isActive()) {
					try {
						a.update();
					} catch (Exception e) {
						mLog.severe("Exception when updating actor no " + a.getId() + ": "
								+ e.toString());
					}
				}
			}

		} else {
			stopMusic();
		}
		flushActorQueue();
	}

	public void addBodiesToDestroy(Body b) {
		b.setActive(false);
		mBodiesToDestroy.add(b);
	}

	/**
	 * Returns a unique ID for an actor
	 * 
	 * @return the ID
	 */
	public long getId() {
		return mNextId++;
	}

	/**
	 * Returns the actor for an id
	 * 
	 * @param id
	 *            The id for which to search
	 * @return The actor, or null if could not be found
	 */
	public Actor getActorById(long id) {
		for (Actor a : mActors) {
			if (a.getId() == id && a.getLevel() == this) {
				return a;
			}
		}
		return null;
	}

	/**
	 * Returns blobs if it can find any
	 * 
	 * @return The blobs
	 */
	public ArrayList<Blob> getBlobs(boolean activeOnly) {
		ArrayList<Blob> blobs = new ArrayList<Blob>();
		for (Actor a : mActors) {
			if (a instanceof Blob) {
				if (activeOnly && !a.isActive()) {
					continue;
				}
				blobs.add((Blob) a);
			}
		}
		return blobs;
	}

	public ArrayList<Spawner> getSpawners(boolean activeOnly) {
		ArrayList<Spawner> spawners = new ArrayList<Spawner>();
		for (Actor a : mActors) {
			if (a instanceof Spawner) {
				if (activeOnly && !a.isActive()) {
					continue;
				}
				spawners.add((Spawner) a);
			}
		}
		return spawners;
	}

	/**
	 * Removes actor from Level.
	 * 
	 * @param id
	 *            ID of Actor to remove.
	 */
	public void removeActor(long id) {
		// do nothing to recycle ID's due to how
		// RemoveCommand works (also we probably
		// won't run out)
		int index = 0;
		for (Actor a : mActors) {
			if (a.getId() == id) {
				break;
			}
			index++;
		}
		if (index < mActors.size()) {
			Actor a = mActors.get(index);
			a.destroy();
			mActors.remove(index);
		}
	}

	/**
	 * Add an actor to the world. This must be the world passed to this actor.
	 * 
	 * @param a
	 */
	public void addActor(Actor a) {
		mAddQueue.addFirst(a);
	}

	/**
	 * Loads a world from a JSON Object
	 * 
	 * @param level
	 *            The world to load.
	 */
	public void load(JSONObject level) throws JSONException {
		JSONObject levelProperties = level.getJSONObject("props");
		// Read in world properties
		JSONArray arr = levelProperties.names();
		for (int j = 0; j < levelProperties.length(); j++) {
			/**** LLLLLEEEANDROID CHANGE ****/
			String key = arr.getString(j);
			setProp(key, levelProperties.get(key));
		}
		JSONArray actors = level.getJSONArray("actors");
		// The JSON array of actors
		for (int i = 0; i < actors.length(); i++) {
			// Retrieve the JSON for this actor
			JSONObject jsonActor = actors.getJSONObject(i);

			String actorType = jsonActor.getString("class");
			long actorId = jsonActor.getLong("id");
			JSONObject jsonProps = jsonActor.getJSONObject("props");
			Class c = null;
			try {
				// Determine which Actor class
				c = Class.forName(actorType);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Constructor cons = null;
			try {
				// Constructor for the actor
				cons = c.getConstructor(Level.class, long.class);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException("Error: Cannot find construcor for Actor: "
						+ c.getName());
			}
			Actor actor = null;
			try {
				// Construct the actor
				actor = (Actor) cons.newInstance(this, actorId);
			} catch (Exception e) {
				throw new RuntimeException("Error: Unable to instantiate Actor: " + c.getName());
			}
			mNextId = Math.max(actor.getId(), mNextId) + 1;

			this.addActor(actor);
		}
		flushActorQueue();

		for (int i = 0; i < actors.length(); i++) {
			// Retrieve the JSON for this actor
			JSONObject jsonActor = actors.getJSONObject(i);

			long actorId = jsonActor.getLong("id");
			Actor actor = getActorById(actorId);
			JSONObject jsonProps = jsonActor.getJSONObject("props");
			JSONArray aArr = jsonProps.names();
			for (int j = 0; j < jsonProps.length(); j++) {
				/**** LLLLLEEEANDROID CHANGE ****/
				String key = aArr.getString(j);
				actor.setProp(key, jsonProps.get(key));
			}
		}

		loadFromLevelSave();
		Game.get().getLevelView().setWorld(mWorld);
	}

	public void loadFromLevelSave() {
		try {
			File f = new File(Gdx.files.getExternalStoragePath() + SAVE_FILE);
			FileHandle handle;
			if (!f.exists()) {
				mLevelSave = new JSONObject();
				handle = new FileHandle(f);
			} else {
				handle = Gdx.files.external(SAVE_FILE);
				String json = handle.readString();
				if ("".equals(json)) {
					mLevelSave = new JSONObject();
				} else {
					mLevelSave = new JSONObject(json);
				}
			}
			if (!mLevelSave.has("HASH")) {
				saveToLevelSave("HASH", "NT4R33LH45H");
				JSONObject startLevel;
				if (mLevelSave.has(Game.get().mStartingLevel)) {
					startLevel = mLevelSave.getJSONObject(Game.get().mStartingLevel);
				} else {
					startLevel = new JSONObject();
				}
				startLevel.put("unlocked", true);
				saveToLevelSave(Game.get().mStartingLevel, startLevel);
			}
			if (mLevelSave.has(getAssetKey())) {
				JSONObject levelJson = mLevelSave.getJSONObject(getAssetKey());
				if (levelJson.has("dots")) {
					JSONArray dots = levelJson.getJSONArray("dots");
					loadDots(dots);
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private void loadDots(JSONArray dots) throws JSONException {
		for (int i = 0; i < dots.length(); i++) {
			Actor actor = getActorById(dots.getInt(i));
			if (actor instanceof Dot) {
				Dot d = (Dot) actor;
				getActorById(d.getId()).setProp("Hollow", 1);
			}
		}
	}

	public void saveToLevelSave(String key, Object value) {
		try {
			mLevelSave.put(key, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		FileHandle handle = Gdx.files.external(SAVE_FILE);
		handle.writeString(mLevelSave.toString(), false);
	}

	/**
	 * Saves the world to JSON
	 * 
	 * @throws JSONException
	 * @return The world in JSON form.
	 */
	public JSONObject save() throws JSONException {
		JSONObject levelProperties = new JSONObject();
		for (String key : mProps.keySet()) {
			levelProperties.put(key, getProp(key));
		}

		// The JSON representation of the level contains list of actors and
		// level properties
		JSONObject level = new JSONObject();

		level.put("props", levelProperties);

		// The JSON array of actors
		JSONArray actors = new JSONArray();
		ActorEnum mEnum = new ActorEnum();
		Blob templateBlob = null;
		if (getBlobs(false).size() > 0) {
			templateBlob = getBlobs(false).get(0);
		}
		// Loop through actors
		for (Actor a : mActors) {
			if (a instanceof Blob && templateBlob != null && !a.equals(templateBlob)) {
				continue;
			}
			if (a instanceof Dot) {
				a.setProp("Active", 1);
				a.setProp("Hollow", 0);
			}
			JSONObject jsonActor = new JSONObject();
			// Save Actor class
			jsonActor.put("class", a.getClass().getName());
			jsonActor.put("id", a.getId());

			jsonActor.put("props", diff(mEnum.getProperties(a.getClass()), a));
			actors.put(jsonActor);
		}
		level.put("actors", actors);
		return level;
	}

	/**
	 * Flushes mAddQueue, adding all its actor to mActors
	 */
	public void flushActorQueue() {
		Actor a;
		while (!mAddQueue.isEmpty()) {
			a = mAddQueue.removeFirst();
			if (a.getLevel() != this || mNextId <= a.getId()) {
				throw new IllegalArgumentException("Cannot add actor to world.");
			}
			mActors.add(a);
		}
	}

	/**
	 * Returns all the Actors in the level
	 * 
	 * @return All the Actors in the level
	 */
	public ArrayList<Actor> getActors() {
		return mActors;
	}

	public String getAssetKey() {
		return mAssetKey;
	}

	public JSONObject getLevelSave() {
		return mLevelSave;
	}

	/**
	 * 
	 * @return Properties of the Level
	 */
	public HashMap<String, Object> getProps() {
		return mProps;
	}

	/**
	 * Determines if the level contains a particular property
	 * 
	 * @param name
	 *            The name of the property
	 * @return True if the property exists
	 */
	public boolean hasProp(String name) {
		return mProps.containsKey(name);
	}

	/**
	 * Returns a given property, or null if it doesn't exist. Potentially throws
	 * ClassCastException.
	 * 
	 * @param name
	 *            The name of the property
	 * @return Requested property
	 */
	public <T> T getProp(String name) {
		if (name.equals("Gravity X")) {
			return (T) (Float) getWorld().getGravity().x;
		}
		if (name.equals("Gravity Y")) {
			return (T) (Float) getWorld().getGravity().y;
		}
		if (name.equals("Ambient Light")) {

			return (T) (Float) mAmbientLight;
		}
		Object prop = mProps.get(name);
		if (prop == null) {
			return null;
		}

		return (T) mProps.get(name);
	}

	/**
	 * Sets a given property
	 * 
	 * @param name
	 *            The name of the property
	 * @param val
	 *            The value
	 */
	public void setProp(String name, Object val) {
		if (name.equals("Gravity X")) {
			getWorld().setGravity(
					new Vector2(Convert.getFloat(val), (Float) getWorld().getGravity().y));
			return;
		}
		if (name.equals("Gravity Y")) {
			getWorld().setGravity(
					new Vector2((Float) getWorld().getGravity().x, Convert.getFloat(val)));
			return;
		}
		if (name.equals("SongName")) {
			mProps.put(name, val);
			stopMusic();
			if (val.equals("")) {
				mMusic = null;
			} else {
				try {
					AssetManager man = Game.get().getAssetManager();

					String musicPath = "data/mus/" + (String) getProp("SongName");

					if ((new File(musicPath)).exists()
							&& musicPath.substring(musicPath.length() - 4, musicPath.length())
									.equals(".wav")) {
						man.load(musicPath, Music.class);
						man.finishLoading();
						mMusic = man.get(musicPath, Music.class);
						mMusic.stop();
					}
				} catch (Exception e) {
					mMusic = null;
				}
			}
			return;
		}
		if (name.equals("Ambient Light")) {
			mAmbientLight = Convert.getFloat(val);
		}
		mProps.put(name, val);
	}

	/**
	 * Gets the World
	 * 
	 * @return the world
	 */
	public World getWorld() {
		return mWorld;
	}

	/**
	 * Creates a diff of two HashMaps, only containing the values in overrides
	 * that differ from those in defaults as well as the values that exist in
	 * overrides but not defaults. If there are no differences null is returned.
	 * 
	 * @param defaults
	 *            The default values HashMap.
	 * @param overrides
	 *            The HashMap that is diff'd against defaults.
	 * @return The diff or null if there are none.
	 */
	// CONSIDER MOVING TO UTILS CLASS
	public HashMap<String, Object> diff(HashMap<String, Object> defaults, Actor actor) {
		HashMap<String, Object> retHash = new HashMap<String, Object>();
		for (String key : actor.getProperties().keySet()) {
			if (!actor.getProp(key).equals(defaults.get(key))) {
				retHash.put(key, actor.getProp(key));
			}
		}
		return retHash.size() != 0 ? retHash : null;
	}

	/**
	 * Loads resources needed by the level
	 */
	public void loadResources() {
		// Load all the actor's resources, except the bodies
		for (Actor a : mActors) {
			a.loadResources();
		}

		Game.get().getAssetManager().finishLoading();

		// Load the actor's bodies, which may depend on their other resources
		for (Actor a : mActors) {
			a.loadBodies();
		}

		for (Actor a : getActors()) {
			a.postLoad();
		}

		// If this level isn't inheriting a song from the previous level, load
		// our new song!
		if (mMusic == null) {
			try {
				AssetManager man = Game.get().getAssetManager();

				String musicPath = "data/mus/" + (String) getProp("SongName");

				if ((new File(musicPath)).exists()
						&& musicPath.substring(musicPath.length() - 4, musicPath.length()).equals(
								".wav")) {
					man.load(musicPath, Music.class);
				}
			} catch (Exception e) {
				mMusic = null;
			}
		}
	}

	/**
	 * Dispose of the actor's resources
	 */
	public void dispose() {
		saveProgress();
		if (LevelView.mUseLights)
			Game.get().getLevelView().getRayHandler().removeAll();
		for (Actor a : mActors) {
			a.dispose();
		}
		Gdx.input.setInputProcessor(Game.get().getInput());
		if (Game.get().getState() == Game.EDIT) {
			stopMusic();
		}
	}

	public ContactHandler getContactHandler() {
		return mContactHandler;
	}

	public void saveProgress() {
		if (mAssetKey == null)
			return;
		try {
			FileHandle handleSt = Gdx.files.external(SAVE_FILE);
			String json = handleSt.readString();
			JSONObject levels;
			if (json.length() < 1) {
				levels = new JSONObject();
			} else {
				levels = new JSONObject(json);
			}
			boolean unlocked = false;
			if (levels != null && levels.has(mAssetKey)) {
				JSONObject level = (JSONObject) levels.remove(mAssetKey);
				if (level.has("unlocked")) {
					unlocked = level.getBoolean("unlocked");
				}
			}
			JSONObject currentLevel = new JSONObject();
			ArrayList<Actor> actors = getActors();
			JSONArray dotIds = new JSONArray();
			int totalDots = 0;
			int collectedDots = 0;
			for (Actor a : actors) {
				if (a instanceof Dot) {
					totalDots++;
					if (!a.isActive() || ((Dot) a).mTargetBlob != null
							|| ((Dot) a).getHollow() == 1) {
						collectedDots++;
						dotIds.put(a.getId());
					}
				}
			}
			float percent = totalDots > 0 ? ((float) collectedDots) / totalDots : 1f;
			currentLevel.put("progress", percent);
			currentLevel.put("dots", dotIds);
			currentLevel.put("unlocked", unlocked);
			levels.put(mAssetKey, currentLevel);
			FileHandle handle = Gdx.files.external(SAVE_FILE);
			handle.writeString(levels.toString(), false);
			unlockModes(levels);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void unlockModes(JSONObject save) {
		if (!EASY_PASSED) {
			try {
				unlockEasyPass(save);
			} catch (JSONException e) {
				// System.out.println("Error reading save for race mode unlock");
				// e.printStackTrace();
			}
		}
		if (!HARD_PASSED) {
			try {
				unlockHardPass(save);
			} catch (JSONException e) {

			}
		}
		if (!MEDEASY_COMPLETE) {
			try {
				unlockHardMode(save);
			} catch (JSONException e) {
				// System.out.println("Error reading save for hard mode unlock");
				// e.printStackTrace();
			}
		}
		if (!COMPLETE) {
			try {
				unlockCompletion(save);
			} catch (JSONException e) {
				// System.out.println("Error reading save for hard mode unlock");
				// e.printStackTrace();
			}
		}
		FIRSTCHECK = false;
	}

	private static void unlockEasyPass(JSONObject save) throws JSONException {
		if (passedEasy(save)) {
			EASY_PASSED = true;
		}
	}

	private static void unlockHardPass(JSONObject save) throws JSONException {
		if (passedHard(save)) {
			HARD_PASSED = true;
		}
	}

	private static boolean passedEasy(JSONObject save) throws JSONException {
		String level = LEVELS[0] + MEDIUM_SUFFIX;
		if (save.has(level) && (save.getJSONObject(level).getBoolean("unlocked"))) {
			return true;
		} else {
			return false;
		}
	}

	private static boolean passedHard(JSONObject save) throws JSONException {
		String level = "gen";
		if (save.has(level) && (save.getJSONObject(level).getBoolean("unlocked"))) {
			return true;
		} else {
			return false;
		}
	}

	private static void unlockHardMode(JSONObject save) throws JSONException {
		if (hasHardModePermission(save)) {
			MEDEASY_COMPLETE = true;
		}
	}

	private static void unlockCompletion(JSONObject save) throws JSONException {
		if (fullGameComplete(save)) {
			COMPLETE = true;
			if (!FIRSTCHECK) {
				for (Player p : Game.get().getPlayers()) {
					p.mustache = true;
				}
			}
		}
	}

	private static boolean hasHardModePermission(JSONObject save) throws JSONException {
		// easy mode check
		for (String s : LEVELS) {
			if (save.has(s)) {
				if (((JSONObject) (save.get(s))).getDouble("progress") != 1) {
					return false;
				} else {
					// nothing just note that this is the passing condition
				}
			} else {
				return false;
			}
		}
		// medium mode check
		for (String level : LEVELS) {
			String s = level + MEDIUM_SUFFIX;
			if (save.has(s)) {
				if (((JSONObject) (save.get(s))).getDouble("progress") != 1) {
					return false;
				} else {
					// nothing just note that this is the passing condition
				}
			} else {
				return false;
			}
		}
		return true;
	}

	private static boolean fullGameComplete(JSONObject save) throws JSONException {
		// easy mode check
		for (String s : LEVELS) {
			if (save.has(s)) {
				if (((JSONObject) (save.get(s))).getDouble("progress") != 1) {
					return false;
				} else {
					// nothing just note that this is the passing condition
				}
			} else {
				return false;
			}
		}
		// medium mode check
		for (String level : LEVELS) {
			String s = level + MEDIUM_SUFFIX;
			if (save.has(s)) {
				if (((JSONObject) (save.get(s))).getDouble("progress") != 1) {
					return false;
				} else {
					// nothing just note that this is the passing condition
				}
			} else {
				return false;
			}
		}
		// hard mode check
		for (String level : LEVELS) {
			String s = level + HARD_SUFFIX;
			if (save.has(s)) {
				if (((JSONObject) (save.get(s))).getDouble("progress") != 1) {
					return false;
				} else {
					// nothing just note that this is the passing condition
				}
			} else {
				return false;
			}
		}
		return true;
	}

	public boolean musicTick() {
		return mMusicTick % 18 == 0;
	}

	public boolean musicOffTick() {
		return (mMusicTick + 9) % 18 == 0;
	}

	public int musicTime() {
		return (mMusicTick % 18);
	}

	// ITERABLE INTERFACE

	/**
	 * Returns an iterator over the level's actors
	 * 
	 * @return Iterator
	 */
	@Override
	public Iterator<Actor> iterator() {
		return mActors.iterator();
	}

	public float getAmbientLight() {
		return mAmbientLight;
	}
}
