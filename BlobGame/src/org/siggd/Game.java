package org.siggd;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.siggd.Player.ControlType;
import org.siggd.actor.meta.ActorEnum;
import org.siggd.actor.meta.PropScanner;
import org.siggd.editor.Editor;
import org.siggd.view.LevelView;
import org.siggd.view.MenuView;

import aurelienribon.bodyeditor.BodyEditorLoader;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

/**
 * This class dispatches the main game loop (the Controller in MVC).
 * 
 * A few things to note:
 * 
 * InputProcessors registered with Input should *always* return false.
 * 
 * @author mysterymath
 * 
 */
public class Game implements ApplicationListener {
	/**
	 * Definitions for Game-level state machine
	 * 
	 * @author mysterymath
	 * 
	 */

	public final static int EDIT = 0;
	public final static int PLAY = 1;
	public final static int MENU = 2;
	public final static int MAX_PLAYERS = 8;
	public final static boolean RELEASE = true;

	public final String mStartingLevel = "level1";
	// The game's current state
	private int mState;

	private boolean mPaused;

	// The view of the level
	private LevelView mLevelView;
	// The menu renderer
	private MenuView mMenuView;
	// The level
	private Level mLevel;
	// An enumerator for all the actors
	private ActorEnum mActorEnum;
	// A scanner for actor properties
	private PropScanner mPropScanner;
	// The asset manager
	private AssetManager mAssetManager;
	// Input event dispatcher
	private InputMultiplexer mInput;
	// Loads bodies from editor
	private BodyEditorLoader mBodyLoader;
	private ArrayList<Player> mPlayers;
	private PlayerListener mPlayerListener;
	private String mNextLevel;

	/**
	 * Constructor (private)
	 */
	private void Game() {
	}

	/**
	 * The singleton object for type Game
	 */
	private static Game theGame;

	/**
	 * Function to get the singleton object
	 * 
	 * @return
	 */
	public static Game get() {
		if (theGame == null) {
			theGame = new Game();
		}

		return theGame;
	}

	/**
	 * The level editor
	 */
	private Editor mEditor;

	/**
	 * Initialize the game
	 */
	@Override
	public void create() {
		mPropScanner = new PropScanner("org.siggd.actor");

		// Create the asset manager
		mAssetManager = new AssetManager();
		mAssetManager.setLoader(Level.class, new LevelLoader(
				new InternalFileHandleResolver()));
		mAssetManager.setLoader(Texture.class, new TextureLoaderWrapper(
				new InternalFileHandleResolver()));
		// Setup input
		mInput = new InputMultiplexer();
		mPlayers = new ArrayList<Player>();
		DebugOutput.enable();
		DebugOutput.info(this, "Controllers: "
				+ Controllers.getControllers().size);
		try {
			ControllerFilterAPI.load();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (!RELEASE) {
			// Create players the old way if not in release mode
			int i = 0;
			for (Controller controller : Controllers.getControllers()) {
				Player p = new Player(mPlayers.size());
				p.controller = controller;
				p.controltype = ControlType.Controller;
				mPlayers.add(p);
				DebugOutput.info(this,
						"Controller #" + i++ + ": " + controller.getName());
			}
			if (Controllers.getControllers().size == 0) {
				DebugOutput.info(this, "No controllers attached");
				Player p = new Player(mPlayers.size());
				p.controltype = ControlType.Arrows;
				mPlayers.add(p);
			}
		}

		// setup the listener that prints events to the console
		mPlayerListener = new PlayerListener();
		Controllers.addListener(mPlayerListener);

		// Create the level view
		mLevelView = new LevelView();

		mMenuView = new MenuView();

		// Load physics bodies

		if (RELEASE && false) {
			mBodyLoader = new BodyEditorLoader(Gdx.files.internal("data/bodies.json"));
		} else {
			mBodyLoader = new BodyEditorLoader(combineBodies());
		}

		// BEGIN: EDITOR
		// Enumerate the available actors
		mActorEnum = new ActorEnum();
		// END: EDITOR
		mLevel = new Level(null);

		// BEGIN: EDITOR
		// Create the editor
		DebugOutput.disable();
		if (!RELEASE) {
			mEditor = new Editor();
			setLevel(mStartingLevel);
			setState(Game.EDIT);
			// load the select/point image
			mAssetManager.load(mEditor.selectPoint, Texture.class);
		} else {
			setState(Game.MENU);
			setLevel("earth");
			mLevelView.setCameraPosition(new Vector2());
		}
		// END: EDITOR

		// Load level from JSON

		// BEGIN: EDITOR
		// Load all actor resources
		mActorEnum.loadActorResources();
		// END: EDITOR

		// Finish loading all the resources
		mAssetManager.finishLoading();
	}

	public String combineBodies() {
		// TODO: This shouldn't be needed at release.
		StringBuilder sbOut = new StringBuilder();
		FileHandle dirHandle;
		if (Gdx.app.getType() == ApplicationType.Android) {
			dirHandle = Gdx.files.internal("data/bodies");
		} else {
			// ApplicationType.Desktop ..
			dirHandle = Gdx.files.internal("./bin/data/bodies");
		}
		sbOut.append("{\"rigidBodies\":[");
		for (FileHandle entry : dirHandle.list()) {
			String s = entry.readString();
			s = s.replaceAll("\\.\\./", "");
			StringBuilder sb = new StringBuilder(s);
			sb.delete(0, 16);
			sb.delete(sb.length() - 22, sb.length());
			sb.append(",");
			sbOut.append(sb.toString());
		}
		sbOut.delete(sbOut.length() - 1, sbOut.length());
		sbOut.append("],\"dynamicObjects\":[]}");
		// System.out.println(sbOut.toString());
		return sbOut.toString();
	}

	/**
	 * Deinitialize the game
	 */
	@Override
	public void dispose() {
		// BEGIN: EDITOR
		// Destroy the editor
		if (mEditor != null) {
			mEditor.dispose();
		}
		// END: EDITOR

		// Destroy the level view
		mLevelView.dispose();

		mMenuView.dispose();

		// Destroy the asset manager
		mAssetManager.dispose();
	}

	/**
	 * Render the game
	 */
	@Override
	public void render() {
		// Load any necessary resources
		if (!mAssetManager.update()) {
			// Show loading screen if not done loading
			return;
		}
		if (mNextLevel != null) {
			setLevel(mNextLevel);
			mNextLevel = null;
		}
		if (mState == PLAY || mState == MENU) {
			mMenuView.update();
			if (!mPaused) {
				mLevel.update();
			}
		}

		if (mEditor != null) {
			mEditor.update();
		}

		if (mNextLevel == null) {
			mLevelView.render();
		}
		if (mState == MENU) {
			mMenuView.render();
		}
	}

	/**
	 * Handle a window resize
	 */
	@Override
	public void resize(int width, int height) {
		mLevelView.onResize(width, height);
		mMenuView.onResize(width, height);
	}

	/**
	 * Handle an externally-generated pause
	 */
	@Override
	public void pause() {
	}

	/**
	 * Handle an externally-generated resume
	 */
	@Override
	public void resume() {
	}

	// ACCESSORS & MUTATORS
	/**
	 * Returns the current state
	 * 
	 * @return the Game's State
	 */
	public int getState() {
		return mState;
	}

	/**
	 * Sets the current state
	 * 
	 * @param state
	 *            The new state
	 */
	public void setState(int state) {
		mState = state;
		if (state == MENU) {
			Gdx.input.setCursorCatched(false);
			mMenuView.giveFocus();
		} else if (state == EDIT) {
			Gdx.input.setCursorCatched(false);

			Gdx.input.setInputProcessor(mInput);
			if (!mInput.getProcessors().contains(mEditor, true)) {
				mInput.addProcessor(mEditor);
			}
		} else if (state == PLAY) {
			setPaused(false);
			Gdx.input.setInputProcessor(mInput);
			if (RELEASE) {
				Gdx.input.setCursorCatched(true);
			} else if (!mInput.getProcessors().contains(mEditor, true)) {
				mInput.addProcessor(mEditor);
			}
		}
	}

	public void setPaused(boolean pause) {
		mPaused = pause;
	}

	public boolean getPaused() {
		return mPaused;
	}

	/**
	 * Returns the Game's Asset Manager
	 * 
	 * @return the Game's Asset Manager
	 */
	public AssetManager getAssetManager() {
		return mAssetManager;
	}

	/**
	 * Returns the Game's ActorEnum
	 * 
	 * @return Returns the Game's ActorEnum
	 */
	public ActorEnum getActorEnum() {
		return mActorEnum;
	}

	/**
	 * Returns the Game's PropScanner
	 * 
	 * @return
	 */
	public PropScanner getPropScanner() {
		return mPropScanner;
	}

	/**
	 * Returns the Game's Editor
	 * 
	 * @return Returns the Game's Editor
	 */
	public Editor getEditor() {
		return mEditor;
	}

	/**
	 * Returns the Game's Level
	 * 
	 * @return Returns the Game's Level
	 */
	public Level getLevel() {
		return mLevel;
	}

	/**
	 * Loads and changes the games Level, this may be incompatible with android.
	 * (1/19/2013)
	 * 
	 * @param fileName
	 *            name of the file to be loaded
	 */
	public void setLevel(String fileName) {
		Music music = null;
		String song = null;
		if (mLevel != null) {
			mLevel.stopMusic();
			mLevel.dispose();
			if (!Game.RELEASE) {
				Game.get().getInput().addProcessor(Game.get().getEditor());
			}
			music = mLevel.mMusic;
			song = (String) mLevel.mProps.get("SongName");

		}

		if (mLevel != null && mLevel.getAssetKey() != null) {
			mAssetManager.unload(mLevel.getAssetKey());
		}
		// TODO loading screen stuff here?
		mAssetManager.load(fileName, Level.class);
		mAssetManager.finishLoading();
		mLevel = mAssetManager.get(fileName, Level.class);
		if (Game.get().getEditor() != null) {
			getEditor().updateWorldProperties();
		}

		// Finish loading the map
		mAssetManager.finishLoading();

		// Load all level resources
		String newSong = (String) mLevel.mProps.get("SongName");
		if (newSong.equals(song)) {
			mLevel.mMusic = music;
		}
		mLevel.loadResources();

		// Finish loading the level resources
		mAssetManager.finishLoading();
	}

	/**
	 * Used by the level editor to reset level;
	 * 
	 * @param json
	 */
	public void setLevel(JSONObject json) {
		String assetKey = mLevel.getAssetKey();
		if (mLevel != null)
			mLevel.dispose();
		mLevel = null;
		mLevel = new Level(assetKey);
		try {
			mLevel.load(json);
		} catch (JSONException e) {
			System.out.println("Failed to load JSONObject level");
		}
		mLevel.loadResources();
	}

	/**
	 * Loads and changes the games Level, this may be incompatible with android.
	 * (1/19/2013)
	 * 
	 * @param the
	 *            Level object to use
	 */
	public void setLevel(Level l) {
		if (mLevel != null)
			mLevel.dispose();
		mLevel = l;
	}

	/**
	 * Returns the Game's LevelView
	 * 
	 * @return Returns the Game's LevelView
	 */
	public LevelView getLevelView() {
		return mLevelView;
	}

	/**
	 * Returns the Game's MenuView
	 * 
	 * @return Returns the Game's MenuView
	 */
	public MenuView getMenuView() {
		return mMenuView;
	}

	/**
	 * Returns the Game's Body Editor Loader
	 * 
	 * @return Returns the Game's BodyEditorLoader
	 */
	public BodyEditorLoader getBodyEditorLoader() {
		return mBodyLoader;
	}

	/**
	 * Returns the Input
	 * 
	 * @returns the Input
	 */
	public InputMultiplexer getInput() {
		return mInput;
	}

	public int getNumberOfPlayers() {
		return mPlayers.size();
	}

	public ArrayList<Player> getPlayers() {
		return mPlayers;
	}

	public void addPlayer(Player p) {
		mPlayers.add(p);
	}

	public Player getPlayer(int id) {
		for (Player p : mPlayers) {
			if (p.id == id) {
				return p;
			}
		}
		return null;
	}

	public Player getPlayer(Controller c) {
		for (Player p : mPlayers) {
			if (p.controller == c) {
				return p;
			}
		}
		return null;
	}

	/**
	 * Used to get Arrows or WASD player
	 * 
	 * @param t
	 *            ControlType.Controller is ignored, use Arrows or WASD
	 * @return
	 */
	public Player getPlayer(ControlType t) {
		if (t == ControlType.Controller)
			return null;
		for (Player p : mPlayers) {
			if (p.controltype == t) {
				return p;
			}
		}
		return null;
	}

	public void setNextLevel(String nextLevel) {
		mNextLevel = nextLevel;
	}
}