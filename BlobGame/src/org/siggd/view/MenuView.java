package org.siggd.view;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.siggd.ControllerFilterAPI;
import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.MenuController;
import org.siggd.Player;
import org.siggd.Player.ControlType;
import org.siggd.actor.Blob;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class MenuView {
	public static String MAIN = "Main";
	public static String PAUSE = "Pause";
	public static String FAKE_PAUSE = "FakePause";
	public static String LEVELS = "Levels";
	public static String CUSTOMIZE = "Customize";

	private Stage mStage;
	private Skin mSkin;
	private Table mMainTable;
	private Table mPauseTable;
	private Table mFakePauseTable;
	private Controller mFakePauseController;
	private Table mLevelsTable;
	private Table mHardLevelsTable;
	private Table mTint;
	private Table mCustomizeTable;
	private Image mJoinImage;
	private int mDelay;
	private MenuController mMenuController;
	private ShapeRenderer mShapeRenderer;
	private HashMap<String, SiggdImageButton> mLevel1;
	private HashMap<String, SiggdImageButton> mHardLevel1;
	private String mCurrentMenu;
	private String mSelectedLevel;
	private final float mSpacing = 10;

	public MenuView() {
		mStage = new Stage();
		mSkin = new Skin();
		mMenuController = new MenuController();
		mShapeRenderer = new ShapeRenderer();
		mDelay = 0;

		// Generate a 1x1 white texture and store it in the skin named "white".
		Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
		pixmap.setColor(0, 0, 0, .5f);
		pixmap.fill();
		mSkin.add("black", new Texture(pixmap));

		// Store the default libgdx font under the name "default".
		mSkin.add("default", new BitmapFont());

		mTint = new Table(mSkin);
		mTint.setFillParent(true);
		mTint.setBackground("black");

		TextButtonStyle textButtonStyle = new TextButtonStyle();
		textButtonStyle.up = mSkin.newDrawable("black", Color.DARK_GRAY);
		textButtonStyle.down = mSkin.newDrawable("black", Color.DARK_GRAY);
		textButtonStyle.checked = mSkin.newDrawable("black", Color.BLUE);
		textButtonStyle.over = mSkin.newDrawable("black", Color.LIGHT_GRAY);
		textButtonStyle.font = mSkin.getFont("default");
		mSkin.add("default", textButtonStyle);

		mMainTable = new Table(mSkin);
		mMainTable.setFillParent(true);

		mStage.addActor(mMainTable);

		final TextButton campaignButton = new TextButton(" Campaign ", mSkin);
		mMainTable.add(campaignButton);
		campaignButton.addListener(mStartCampaign);

		final TextButton hardButton = new TextButton(" Hard ", mSkin);
		mMainTable.add(hardButton);
		hardButton.addListener(mStartHard);

		final TextButton battleButton = new TextButton(" Battle ", mSkin);
		mMainTable.add(battleButton);
		battleButton.addListener(mStartBattle);

		final TextButton slamButton = new TextButton(" Slam ", mSkin);
		mMainTable.add(slamButton);
		slamButton.addListener(mStartSlam);

		final TextButton exitButton = new TextButton(" Exit ", mSkin);
		mMainTable.add(exitButton);
		exitButton.addListener(mExit);

		// Pause Menu
		mPauseTable = new Table(mSkin);
		mPauseTable.setFillParent(true);

		final TextButton mainMenuButton = new TextButton(" Main ", mSkin);
		mPauseTable.add(mainMenuButton);
		mainMenuButton.addListener(mMainMenu);

		final TextButton resetButton = new TextButton(" Reset ", mSkin);
		mPauseTable.add(resetButton);
		resetButton.addListener(mReset);

		final TextButton continueButton = new TextButton(" Continue ", mSkin);
		mPauseTable.add(continueButton);
		continueButton.addListener(mContinue);

		// FakePause Menu
		mFakePauseTable = new Table(mSkin);
		mFakePauseTable.setFillParent(true);

		final TextButton resumeButton = new TextButton(" Continue ", mSkin);
		mFakePauseTable.add(resumeButton);
		resumeButton.addListener(mContinue);

		// Levels Menu
		mLevelsTable = new Table(mSkin);
		mLevelsTable.setFillParent(true);
		ImageButton imageButton = new SiggdImageButton(
				"data/gfx/backButton.png", "data/gfx/backButton.png")
				.getButton();
		imageButton.addListener(mMainMenu);
		mLevelsTable.add(imageButton);

		mLevel1 = new HashMap<String, SiggdImageButton>();
		mLevel1.put("level1", new SiggdImageButton("data/gfx/buttonUp.png",
				"data/gfx/buttonDown.png", "data/gfx/buttonDisabled.png",
				"level1"));
		mLevel1.put("level7", new SiggdImageButton("data/gfx/buttonUp.png",
				"data/gfx/buttonDown.png", "data/gfx/buttonDisabled.png",
				"level7"));
		mLevel1.put("level3", new SiggdImageButton("data/gfx/buttonUp.png",
				"data/gfx/buttonDown.png", "data/gfx/buttonDisabled.png",
				"level3"));
		mLevel1.put("level4", new SiggdImageButton("data/gfx/buttonUp.png",
				"data/gfx/buttonDown.png", "data/gfx/buttonDisabled.png",
				"level4"));
		mLevel1.put("level5", new SiggdImageButton("data/gfx/buttonUp.png",
				"data/gfx/buttonDown.png", "data/gfx/buttonDisabled.png",
				"level5"));
		mLevel1.put("level2", new SiggdImageButton("data/gfx/buttonUp.png",
				"data/gfx/buttonDown.png", "data/gfx/buttonDisabled.png",
				"level2"));
		mLevel1.put("level8", new SiggdImageButton("data/gfx/buttonUp.png",
				"data/gfx/buttonDown.png", "data/gfx/buttonDisabled.png",
				"level8"));

		SiggdImageButton button = mLevel1.get("level1");
		mLevelsTable.add(button.getButton()).space(mSpacing);
		button.getButton().addListener(mStartLevel);

		button = mLevel1.get("level7");
		mLevelsTable.add(button.getButton()).space(mSpacing);
		button.getButton().addListener(mStartLevel);

		button = mLevel1.get("level3");
		mLevelsTable.add(button.getButton()).space(mSpacing);
		button.getButton().addListener(mStartLevel);

		button = mLevel1.get("level4");
		mLevelsTable.add(button.getButton()).space(mSpacing);
		button.getButton().addListener(mStartLevel);

		button = mLevel1.get("level5");
		mLevelsTable.add(button.getButton()).space(mSpacing);
		button.getButton().addListener(mStartLevel);

		button = mLevel1.get("level2");
		mLevelsTable.add(button.getButton()).space(mSpacing);
		button.getButton().addListener(mStartLevel);

		button = mLevel1.get("level8");
		mLevelsTable.add(button.getButton()).space(mSpacing);
		button.getButton().addListener(mStartLevel);

		// Hard Menu
		mHardLevelsTable = new Table(mSkin);
		mHardLevelsTable.setFillParent(true);
		imageButton = new SiggdImageButton("data/gfx/backButton.png",
				"data/gfx/backButton.png").getButton();
		imageButton.addListener(mMainMenu);
		mHardLevelsTable.add(imageButton);

		mHardLevel1 = new HashMap<String, SiggdImageButton>();
		mHardLevel1.put("level1_hard", new SiggdImageButton(
				"data/gfx/buttonUp.png", "data/gfx/buttonDown.png",
				"data/gfx/buttonDisabled.png", "level1_hard"));
		mHardLevel1.put("level3_hard", new SiggdImageButton(
				"data/gfx/buttonUp.png", "data/gfx/buttonDown.png",
				"data/gfx/buttonDisabled.png", "level3_hard"));
		mHardLevel1.put("level5_hard", new SiggdImageButton(
				"data/gfx/buttonUp.png", "data/gfx/buttonDown.png",
				"data/gfx/buttonDisabled.png", "level5_hard"));
		mHardLevel1.put("level2_hard", new SiggdImageButton(
				"data/gfx/buttonUp.png", "data/gfx/buttonDown.png",
				"data/gfx/buttonDisabled.png", "level2_hard"));

		button = mHardLevel1.get("level1_hard");
		mHardLevelsTable.add(button.getButton()).space(mSpacing);
		button.getButton().addListener(mStartLevel);

		button = mHardLevel1.get("level3_hard");
		mHardLevelsTable.add(button.getButton()).space(mSpacing);
		button.getButton().addListener(mStartLevel);

		button = mHardLevel1.get("level5_hard");
		mHardLevelsTable.add(button.getButton()).space(mSpacing);
		button.getButton().addListener(mStartLevel);

		button = mHardLevel1.get("level2_hard");
		mHardLevelsTable.add(button.getButton()).space(mSpacing);
		button.getButton().addListener(mStartLevel);

		// Customize menu
		mCustomizeTable = new Table(mSkin);
		mCustomizeTable.setFillParent(true);
		mJoinImage = new Image(new Texture(
				Gdx.files.internal("data/gfx/circle.png")));

		// Set the starting menu
		setMenu(MAIN);
	}

	public void render() {
		mStage.act(Gdx.graphics.getDeltaTime());
		mStage.draw();
		// Table.drawDebug(mStage);
		mShapeRenderer.setProjectionMatrix(mStage.getCamera().combined);
		JSONObject levelSave = Game.get().getLevel().getLevelSave();
		if (LEVELS.equals(mCurrentMenu)) {
			for (String s : mLevel1.keySet()) {
				ImageButton tmp = mLevel1.get(s).getButton();
				float x = tmp.getX();
				float y = tmp.getY() - 20;
				float width = tmp.getWidth();
				float progress = 0;
				try {
					String key = s + "%";
					if (!levelSave.isNull(key)) {
						progress = Convert.getFloat(levelSave.get(key));
					}
				} catch (JSONException e) {
				}
				mShapeRenderer.begin(ShapeType.Filled);
				mShapeRenderer.setColor(Color.BLACK);
				mShapeRenderer.rect(x, y, width, 10);
				if (progress < 1) {
					mShapeRenderer.setColor(Color.RED);
				} else {
					mShapeRenderer.setColor(Color.GREEN);
				}
				mShapeRenderer.rect(x + 2, y + 2, (width - 4) * progress, 6);
				mShapeRenderer.end();
			}
		} else if (CUSTOMIZE.equals(mCurrentMenu)) {
			if (Game.get().activePlayers() > 0) {
				mJoinImage.remove();
			}
		}
		mMenuController.draw(mShapeRenderer);
	}

	public void update() {
		mMenuController.update();
		if (CUSTOMIZE.equals(mCurrentMenu)
				&& Game.get().getState() != Game.PLAY) {
			Player p = null;
			if (mDelay > 10) {
				p = testForNewPlayer();
			} else {
				mDelay++;
			}
			if (p != null) {
				if (!Game.get().playerExists(p)) {
					System.out.println("Adding player of type: "
							+ p.controltype);
					Game.get().addPlayer(p);
				}
				activatePlayer(p);
			}
			boolean start = false;
			for (Player pl : Game.get().getPlayers()) {
				if (!pl.active)
					continue;
				if (pl.controltype == ControlType.Controller
						&& pl.controller != null) {
					Controller c = pl.controller;
					start = start
							|| c.getButton(ControllerFilterAPI
									.getButtonFromFilteredId(c,
											ControllerFilterAPI.BUTTON_START));
				} else if (pl.controltype == ControlType.WASD
						|| pl.controltype == ControlType.Arrows) {
					start = start || Gdx.input.isKeyPressed(Input.Keys.SPACE)
							|| Gdx.input.isKeyPressed(Input.Keys.ENTER);
				}
				if (start)
					break;
			}
			if (start) {
				for (Player pl : Game.get().getPlayers()) {
					if (pl.active && pl.controltype == ControlType.Controller
							&& pl.controller != null) {
						mMenuController.setController(pl.controller);
						mMenuController.setPlayerId(pl.id);
						break;
					}
				}
				setMenu("NULL");
				Game.get().setState(Game.PLAY);
				Game.get().setLevel(mSelectedLevel);
			}
		}
	}

	public void resize(int width, int height) {
		mStage.setViewport(width, height, true);
	}

	public void dispose() {
		mStage.dispose();
	}

	private Player testForNewPlayer() {
		Player p = null;
		for (Controller c : Controllers.getControllers()) {
			if (Game.get().getNumberOfPlayers() < Game.MAX_PLAYERS && p == null) {
				for (int i = 0; i < 10; i++) {
					if (c.getButton(i)) {
						// unassigned controller button pressed
						Player inactivePlayer = Game.get().getPlayer(c);
						if (inactivePlayer == null) {
							p = new Player(Game.get().getNumberOfPlayers());
							p.controller = c;
							p.controltype = org.siggd.Player.ControlType.Controller;
						} else if (!inactivePlayer.active) {
							p = inactivePlayer;
						}
						break;
					}
				}
			} else {
				// shortcircuit if there are max players or a new player is
				// ready to be added
				break;
			}
		}
		if (p == null) {
			if (Gdx.input.isKeyPressed(Input.Keys.LEFT)
					|| Gdx.input.isKeyPressed(Input.Keys.RIGHT)
					|| Gdx.input.isKeyPressed(Input.Keys.DOWN)
					|| Gdx.input.isKeyPressed(Input.Keys.UP)) {
				Player inactivePlayer = Game.get().getPlayer(
						org.siggd.Player.ControlType.Arrows);
				if (inactivePlayer == null) {
					p = new Player(Game.get().getNumberOfPlayers());
					p.controltype = org.siggd.Player.ControlType.Arrows;
				} else {
					if (!inactivePlayer.active) {
						p = inactivePlayer;
					}
				}
			} else if (Gdx.input.isKeyPressed(Input.Keys.A)
					|| Gdx.input.isKeyPressed(Input.Keys.D)
					|| Gdx.input.isKeyPressed(Input.Keys.S)
					|| Gdx.input.isKeyPressed(Input.Keys.W)) {
				Player inactivePlayer = Game.get().getPlayer(
						org.siggd.Player.ControlType.WASD);
				if (inactivePlayer == null) {
					p = new Player(Game.get().getNumberOfPlayers());
					p.controltype = org.siggd.Player.ControlType.WASD;
				} else {
					if (!inactivePlayer.active) {
						p = inactivePlayer;
					}
				}
			}
		}
		return p;
	}

	/**
	 * Makes a player active and spawns a blob in the customize level for the
	 * player
	 * 
	 * @param p
	 *            player to activate
	 */
	private void activatePlayer(Player p) {
		if (p != null) {
			p.active = true;
			Level l = Game.get().getLevel();
			Blob b = new Blob(l, l.getId());
			l.addActor(b);
			Vector2 pos = new Vector2((p.id % 4) * Gdx.graphics.getWidth() / 4
					+ Gdx.graphics.getWidth() / 8,
					p.id < 4 ? 5 * Gdx.graphics.getHeight() / 12
							: 11 * Gdx.graphics.getHeight() / 12);
			Game.get().getLevelView().unproject(pos);
			b.setX(pos.x);
			b.setY(pos.y);
			b.setProp("Player ID", p.id);
		}
	}

	private final ChangeListener mStartCampaign = new ChangeListener() {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			TextButton textButton = (TextButton) actor;
			textButton.setChecked(false);
			setMenu(LEVELS);
		}
	};

	private final ChangeListener mStartHard = new ChangeListener() {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			TextButton textButton = (TextButton) actor;
			textButton.setChecked(false);
			setMenu("Hard Levels");
		}
	};

	private final ChangeListener mStartBattle = new ChangeListener() {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			TextButton textButton = (TextButton) actor;
			textButton.setChecked(false);
			Game.get().setState(Game.PLAY);
			Game.get().setLevel("battleground");
		}
	};

	private final ChangeListener mStartSlam = new ChangeListener() {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			TextButton textButton = (TextButton) actor;
			textButton.setChecked(false);
			Game.get().setState(Game.PLAY);
			Game.get().setLevel("SLAM");
		}
	};
	private final ChangeListener mReset = new ChangeListener() {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			if (actor instanceof TextButton) {
				TextButton textButton = (TextButton) actor;
				textButton.setChecked(false);
			}
			Game.get().setState(Game.PLAY);
			Game.get().setLevel(Game.get().getLevel().getAssetKey());
		}
	};

	private final ChangeListener mStartLevel = new ChangeListener() {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			mSelectedLevel = actor.getName();
			setMenu(CUSTOMIZE);
		}

	};

	private final ChangeListener mContinue = new ChangeListener() {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			TextButton textButton = (TextButton) actor;
			textButton.setChecked(false);
			Game.get().setPaused(false);
			Game.get().setState(Game.PLAY);
		}
	};

	private final ChangeListener mMainMenu = new ChangeListener() {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			if (actor instanceof TextButton) {
				TextButton textButton = (TextButton) actor;
				textButton.setChecked(false);
			}
			if (!"earth".equals(Game.get().getLevel().getAssetKey())) {
				Game.get().setNextLevel("earth");
				Game.get().setPaused(false);
				Game.get().getLevelView().resetCamera();
			}
			Game.get().deactivatePlayers();
			setMenu(MAIN);
		}
	};
	private final ChangeListener mExit = new ChangeListener() {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			TextButton textButton = (TextButton) actor;
			textButton.setChecked(false);
			Gdx.app.exit();
		}
	};

	public void giveFocus() {

		InputMultiplexer multiplexer = Game.get().getInput();
		if (!multiplexer.getProcessors().contains(mStage, true)) {
			multiplexer.addProcessor(mStage);
		}
		if (!multiplexer.getProcessors().contains(mMenuController, true)) {
			multiplexer.addProcessor(mMenuController);
		}
	}

	public void setMenu(String menu) {
		mCurrentMenu = menu;
		mDelay = 0;
		mMainTable.remove();
		mPauseTable.remove();
		mFakePauseTable.remove();
		mLevelsTable.remove();
		mHardLevelsTable.remove();
		mCustomizeTable.remove();
		mTint.remove();
		if (MAIN.equals(menu)) {
			mStage.addActor(mMainTable);
			mMenuController.setTable(mMainTable);
			if (Game.RELEASE) {
				Game.get().deactivatePlayers();
			}
		} else if (PAUSE.equals(menu)) {
			mStage.addActor(mTint);
			mStage.addActor(mPauseTable);
			mMenuController.setTable(mPauseTable);
		} else if (FAKE_PAUSE.equals(menu)) {
			mStage.addActor(mTint);
			mStage.addActor(mFakePauseTable);
			mMenuController.setTable(mFakePauseTable);
		} else if (LEVELS.equals(menu)) {
			mStage.addActor(mLevelsTable);
			JSONObject levelSave = Game.get().getLevel().getLevelSave();
			for (String s : mLevel1.keySet()) {
				ImageButton tmp = mLevel1.get(s).getButton();
				String key = s + "Unlocked";
				if (levelSave.isNull(key)) {
					tmp.setDisabled(true);
				} else {
					tmp.setDisabled(false);
				}
			}
			mMenuController.setTable(mLevelsTable);
			mMenuController.setIndex(1);
		} else if ("Hard Levels".equals(menu)) {
			mStage.addActor(mHardLevelsTable);
			JSONObject levelSave = Game.get().getLevel().getLevelSave();
			for (String s : mHardLevel1.keySet()) {
				ImageButton tmp = mHardLevel1.get(s).getButton();
				String key = s + "Unlocked";
				if (levelSave.isNull(key)) {
					tmp.setDisabled(true);
				} else {
					tmp.setDisabled(false);
				}
			}
			mMenuController.setTable(mHardLevelsTable);
			mMenuController.setIndex(1);
		} else if (CUSTOMIZE.equals(menu)) {
			if (!mCustomizeTable.getChildren().contains(mJoinImage, false)) {
				mCustomizeTable.add(mJoinImage);
			}
			mStage.addActor(mCustomizeTable);
			mMenuController.setTable(null);
			Game.get().setLevel("charselect");
			for (Player p : Game.get().getPlayers()) {
				if (p.active) {
					Level l = Game.get().getLevel();
					Blob b = new Blob(l, l.getId());
					l.addActor(b);
					Vector2 pos = new Vector2((p.id % 4)
							* Gdx.graphics.getWidth() / 4
							+ Gdx.graphics.getWidth() / 8,
							p.id < 4 ? 5 * Gdx.graphics.getHeight() / 12
									: 11 * Gdx.graphics.getHeight() / 12);
					Game.get().getLevelView().unproject(pos);
					b.setX(pos.x);
					b.setY(pos.y);
					b.setProp("Player ID", p.id);
				}
			}
		}
	}

	public void setFakePauseController(Controller c) {
		mFakePauseController = c;
	}

	public Controller getFakePauseController() {
		return mFakePauseController;
	}

	public MenuController getMenuController() {
		return mMenuController;
	}

	public String getCurrentMenu() {
		return mCurrentMenu;
	}

	public Stage getStage() {
		return mStage;
	}

	public void onResize(int width, int height) {
		mStage.setViewport(width, height, true);
	}
}
