package org.siggd.view;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.siggd.ControllerFilterAPI;
import org.siggd.DebugOutput;
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
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class MenuView {
	public static String MAIN = "Main";
	public static String PAUSE = "Pause";
	public static String FAKE_PAUSE = "FakePause";
	public static String LEVELS = "Levels";
	public static String CUSTOMIZE = "Customize";
	public static String LOADING = "Loading";
	public static String CONTROLLER = "Controller";
	private Stage mStage;
	private Skin mSkin;
	private Table mMainTable;
	private Table mPauseTable;
	private Table mFakePauseTable;
	private Controller mFakePauseController;
	private Table mLevelsTable;
	private Table mLevelsBG;
	private Table mTint;
	private Table mBaseCustomizeTable;
	private Table mCustomizeTable;
	private Image mJoinImage;
	private Image mStartImage;
	private Table mControllerTable;
	private Table mControllerOverTable;
	private Image mControllerLeft;
	private Image mControllerRight;
	private Image mControllerUp;
	private Image mControllerDown;
	private Image mControllerPoof;
	private Image mControllerSolid;
	private Image mControllerStart;
	private SiggdImageButton mRaceButton;
	private SiggdImageButton mCampaignButton;
	private SiggdImageButton mExitButton;
	private SiggdImageButton mControllerButton;
	private TextButton mClearButton;
	private int mDelay;
	public MenuController mMenuController;
	private ShapeRenderer mShapeRenderer;
	private HashMap<String, SiggdImageButton> mLevel1;
	private String mCurrentMenu;
	public String mSelectedLevel;
	private final float mHorizontalSpacing = 10f;
	private final float mVerticalSpacing = 30f;
	private float mRollingAlpha = 0f;
	private int mHintTimer = 0;
	private int mTwoSecondTimer = 0;
	private ArrayList<Vector2> mSpawnPos;
	private float mGlow = 0;
	private Color mGlowColor = new Color();

	public MenuView() {
		mStage = new Stage();
		mSkin = new Skin();
		mMenuController = new MenuController();
		mShapeRenderer = new ShapeRenderer();
		mDelay = 0;
		mBindingController = null;
		mSpawnPos = new ArrayList<Vector2>();

		// Generate a 1x1 white texture and store it in the skin named "white".
		Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
		pixmap.setColor(0, 0, 0, .65f);
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

		createMainMenu();
		createPauseMenu();
		createFakePauseMenu();
		createLevelsMenu();
		createCustomizeMenu();
		createControllerMenu();

		// Starting menu is now set in Game.create()
	}

	private void createMainMenu() {
		mMainTable = new Table(mSkin);
		mMainTable.setFillParent(true);

		mStage.addActor(mMainTable);

		mRaceButton = new SiggdImageButton("data/gfx/racebtn.png");
		mRaceButton.getButton().setColor(1f, 1f, 1f, .65f);
		mRaceButton.getButton().addListener(mRaceMode);
		mRaceButton.getButton().addListener(mClickListener);

		mCampaignButton = new SiggdImageButton("data/gfx/campaignbtn.png");
		mCampaignButton.getButton().setColor(1f, 1f, 1f, .65f);
		mMainTable.add(mCampaignButton.getButton());
		mCampaignButton.getButton().addListener(mStartCampaign);
		mCampaignButton.getButton().addListener(mClickListener);

		/*
		 * mClearButton = new TextButton(" Clear Save ", mSkin);
		 * mMainTable.add(mClearButton); mClearButton.addListener(mClear);
		 * mClearButton.addListener(mClickListener);
		 */
		if (Controllers.getControllers().size > 0) {
			mControllerButton = new SiggdImageButton("data/gfx/configbtn.png");
			mControllerButton.getButton().setColor(1f, 1f, 1f, .65f);
			mMainTable.add(mControllerButton.getButton());
			mControllerButton.getButton().addListener(mController);
			mControllerButton.getButton().addListener(mClickListener);
		}

		mExitButton = new SiggdImageButton("data/gfx/exitbtn.png");
		mExitButton.getButton().setColor(1f, 1f, 1f, .65f);
		mMainTable.add(mExitButton.getButton());
		mExitButton.getButton().addListener(mExit);
		mExitButton.getButton().addListener(mClickListener);
	}

	private void createPauseMenu() {
		mPauseTable = new Table(mSkin);
		mPauseTable.setFillParent(true);

		final SiggdImageButton mainMenuButton = new SiggdImageButton("data/gfx/stopbtn.png");
		mainMenuButton.getButton().setColor(1f, 1f, 1f, .85f);
		mPauseTable.add(mainMenuButton.getButton());
		mainMenuButton.getButton().addListener(mMainMenu);
		mainMenuButton.getButton().addListener(mClickListener);

		final SiggdImageButton resetButton = new SiggdImageButton("data/gfx/resetbtn.png");
		resetButton.getButton().setColor(1f, 1f, 1f, .85f);
		mPauseTable.add(resetButton.getButton());
		resetButton.getButton().addListener(mReset);
		resetButton.getButton().addListener(mClickListener);

		final SiggdImageButton resumeButton = new SiggdImageButton("data/gfx/resumebtn.png");
		resumeButton.getButton().setColor(1f, 1f, 1f, .85f);
		mPauseTable.add(resumeButton.getButton());
		resumeButton.getButton().addListener(mContinue);
		resumeButton.getButton().addListener(mClickListener);
	}

	private void createFakePauseMenu() {
		mFakePauseTable = new Table(mSkin);
		mFakePauseTable.setFillParent(true);

		final SiggdImageButton resumeButton = new SiggdImageButton("data/gfx/resumebtn.png");
		resumeButton.getButton().setColor(1f, 1f, 1f, .85f);
		mFakePauseTable.add(resumeButton.getButton());
		resumeButton.getButton().addListener(mContinue);
		resumeButton.getButton().addListener(mClickListener);
	}

	private void createLevelsMenu() {
		mLevelsBG = new Table(mSkin);
		mLevelsBG.setFillParent(true);
		mLevelsTable = new Table(mSkin);
		mLevelsTable.setFillParent(true);
		ImageButton imageButton;

		imageButton = new SiggdImageButton("data/gfx/backButton.png").getButton();
		imageButton.setColor(1f, 1f, 1f, .65f);
		imageButton.addListener(mMainMenu);
		imageButton.addListener(mClickListener);
		mLevelsTable.add(imageButton).space(mVerticalSpacing, mHorizontalSpacing, mVerticalSpacing,
				mHorizontalSpacing);

		mLevel1 = new HashMap<String, SiggdImageButton>();
		mLevel1.put("level1", new SiggdImageButton("data/gfx/lvl1Down.png",
				"data/gfx/buttonDisabled.png", "opening"));
		mLevel1.put("level7", new SiggdImageButton("data/gfx/lvl7Down.png",
				"data/gfx/buttonDisabled.png", "level7"));
		mLevel1.put("level5", new SiggdImageButton("data/gfx/lvl5Down.png",
				"data/gfx/buttonDisabled.png", "level5"));
		mLevel1.put("level3", new SiggdImageButton("data/gfx/lvl3Down.png",
				"data/gfx/buttonDisabled.png", "level3"));
		mLevel1.put("level4", new SiggdImageButton("data/gfx/lvl4Down.png",
				"data/gfx/buttonDisabled.png", "level4"));
		mLevel1.put("level2", new SiggdImageButton("data/gfx/lvl2Down.png",
				"data/gfx/buttonDisabled.png", "level2"));
		mLevel1.put("level8", new SiggdImageButton("data/gfx/lvl8Down.png",
				"data/gfx/buttonDisabled.png", "level8"));

		SiggdImageButton button = mLevel1.get("level1");
		button.getButton().setColor(0f, 0f, 1f, .65f);
		mLevelsTable.add(button.getButton()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		mLevelsBG.add(button.getGlow()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		button.getButton().addListener(mStartLevel);
		button.getButton().addListener(mClickListener);
		mLevelsTable.invalidate();
		mLevelsBG.invalidate();

		button = mLevel1.get("level7");
		button.getButton().setColor(0f, 0f, 1f, .65f);
		mLevelsTable.add(button.getButton()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		mLevelsBG.add(button.getGlow()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		button.getButton().addListener(mStartLevel);
		button.getButton().addListener(mClickListener);

		button = mLevel1.get("level5");
		button.getButton().setColor(0f, 0f, 1f, .65f);
		mLevelsTable.add(button.getButton()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		mLevelsBG.add(button.getGlow()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		button.getButton().addListener(mStartLevel);
		button.getButton().addListener(mClickListener);

		button = mLevel1.get("level3");
		button.getButton().setColor(0f, 0f, 1f, .65f);
		mLevelsTable.add(button.getButton()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		mLevelsBG.add(button.getGlow()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		button.getButton().addListener(mStartLevel);
		button.getButton().addListener(mClickListener);

		button = mLevel1.get("level4");
		button.getButton().setColor(0f, 0f, 1f, .65f);
		mLevelsTable.add(button.getButton()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		mLevelsBG.add(button.getGlow()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		button.getButton().addListener(mStartLevel);
		button.getButton().addListener(mClickListener);

		button = mLevel1.get("level2");
		button.getButton().setColor(0f, 0f, 1f, .65f);
		mLevelsTable.add(button.getButton()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		mLevelsBG.add(button.getGlow()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		button.getButton().addListener(mStartLevel);
		button.getButton().addListener(mClickListener);

		button = mLevel1.get("level8");
		button.getButton().setColor(0f, 0f, 1f, .65f);
		mLevelsTable.add(button.getButton()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		mLevelsBG.add(button.getGlow()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		button.getButton().addListener(mStartLevel);
		button.getButton().addListener(mClickListener);

		imageButton = new SiggdImageButton("data/gfx/backButton.png").getButton();
		imageButton.setVisible(false);
		mLevelsTable.add(imageButton).space(mVerticalSpacing, mHorizontalSpacing, mVerticalSpacing,
				mHorizontalSpacing);

		mLevelsTable.row();
		mLevelsBG.row();

		imageButton = new SiggdImageButton("data/gfx/backButton.png").getButton();
		imageButton.setVisible(false);
		mLevelsTable.add(imageButton).space(mVerticalSpacing, mHorizontalSpacing, mVerticalSpacing,
				mHorizontalSpacing);

		mLevel1.put("level1_med", new SiggdImageButton("data/gfx/lvl1Down.png",
				"data/gfx/buttonDisabled.png", "level1_med"));
		mLevel1.put("level7_med", new SiggdImageButton("data/gfx/lvl7Down.png",
				"data/gfx/buttonDisabled.png", "level7_med"));
		mLevel1.put("level5_med", new SiggdImageButton("data/gfx/lvl5Down.png",
				"data/gfx/buttonDisabled.png", "level5_med"));
		mLevel1.put("level3_med", new SiggdImageButton("data/gfx/lvl3Down.png",
				"data/gfx/buttonDisabled.png", "level3_med"));
		mLevel1.put("level4_med", new SiggdImageButton("data/gfx/lvl4Down.png",
				"data/gfx/buttonDisabled.png", "level4_med"));
		mLevel1.put("level2_med", new SiggdImageButton("data/gfx/lvl2Down.png",
				"data/gfx/buttonDisabled.png", "level2_med"));
		mLevel1.put("level8_med", new SiggdImageButton("data/gfx/lvl8Down.png",
				"data/gfx/buttonDisabled.png", "level8_med"));

		button = mLevel1.get("level1_med");
		button.getButton().setColor(0f, 1f, 0f, .65f);
		mLevelsTable.add(button.getButton()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		mLevelsBG.add(button.getGlow()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		button.getButton().addListener(mStartLevel);
		button.getButton().addListener(mClickListener);

		button = mLevel1.get("level7_med");
		button.getButton().setColor(0f, 1f, 0f, .65f);
		mLevelsTable.add(button.getButton()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		mLevelsBG.add(button.getGlow()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		button.getButton().addListener(mStartLevel);
		button.getButton().addListener(mClickListener);

		button = mLevel1.get("level5_med");
		button.getButton().setColor(0f, 1f, 0f, .65f);
		mLevelsTable.add(button.getButton()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		mLevelsBG.add(button.getGlow()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		button.getButton().addListener(mStartLevel);
		button.getButton().addListener(mClickListener);

		button = mLevel1.get("level3_med");
		button.getButton().setColor(0f, 1f, 0f, .65f);
		mLevelsTable.add(button.getButton()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		mLevelsBG.add(button.getGlow()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		button.getButton().addListener(mStartLevel);
		button.getButton().addListener(mClickListener);

		button = mLevel1.get("level4_med");
		button.getButton().setColor(0f, 1f, 0f, .65f);
		mLevelsTable.add(button.getButton()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		mLevelsBG.add(button.getGlow()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		button.getButton().addListener(mStartLevel);
		button.getButton().addListener(mClickListener);

		button = mLevel1.get("level2_med");
		button.getButton().setColor(0f, 1f, 0f, .65f);
		mLevelsTable.add(button.getButton()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		mLevelsBG.add(button.getGlow()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		button.getButton().addListener(mStartLevel);
		button.getButton().addListener(mClickListener);

		button = mLevel1.get("level8_med");
		button.getButton().setColor(0f, 1f, 0f, .65f);
		mLevelsTable.add(button.getButton()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		mLevelsBG.add(button.getGlow()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		button.getButton().addListener(mStartLevel);
		button.getButton().addListener(mClickListener);

		mLevelsTable.row();
		mLevelsBG.row();

		imageButton = new SiggdImageButton("data/gfx/backButton.png").getButton();
		imageButton.setVisible(false);
		mLevelsTable.add(imageButton).space(mVerticalSpacing, mHorizontalSpacing, mVerticalSpacing,
				mHorizontalSpacing);

		mLevel1.put("level1_hard", new SiggdImageButton("data/gfx/lvl1Down.png",
				"data/gfx/buttonDisabled.png", "level1_hard"));
		mLevel1.put("level7_hard", new SiggdImageButton("data/gfx/lvl7Down.png",
				"data/gfx/buttonDisabled.png", "level7_hard"));
		mLevel1.put("level3_hard", new SiggdImageButton("data/gfx/lvl3Down.png",
				"data/gfx/buttonDisabled.png", "level3_hard"));
		mLevel1.put("level4_hard", new SiggdImageButton("data/gfx/lvl4Down.png",
				"data/gfx/buttonDisabled.png", "level4_hard"));
		mLevel1.put("level5_hard", new SiggdImageButton("data/gfx/lvl5Down.png",
				"data/gfx/buttonDisabled.png", "level5_hard"));
		mLevel1.put("level2_hard", new SiggdImageButton("data/gfx/lvl2Down.png",
				"data/gfx/buttonDisabled.png", "level2_hard"));
		mLevel1.put("level8_hard", new SiggdImageButton("data/gfx/lvl8Down.png",
				"data/gfx/buttonDisabled.png", "level8_hard"));

		button = mLevel1.get("level1_hard");
		button.getButton().setColor(1f, 0f, 0f, .65f);
		mLevelsTable.add(button.getButton()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		mLevelsBG.add(button.getGlow()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		button.getButton().addListener(mStartLevel);
		button.getButton().addListener(mClickListener);

		button = mLevel1.get("level7_hard");
		button.getButton().setColor(1f, 0f, 0f, .65f);
		mLevelsTable.add(button.getButton()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		mLevelsBG.add(button.getGlow()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		button.getButton().addListener(mStartLevel);
		button.getButton().addListener(mClickListener);

		button = mLevel1.get("level5_hard");
		button.getButton().setColor(1f, 0f, 0f, .65f);
		mLevelsTable.add(button.getButton()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		mLevelsBG.add(button.getGlow()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		button.getButton().addListener(mStartLevel);
		button.getButton().addListener(mClickListener);

		button = mLevel1.get("level3_hard");
		button.getButton().setColor(1f, 0f, 0f, .65f);
		mLevelsTable.add(button.getButton()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		mLevelsBG.add(button.getGlow()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		button.getButton().addListener(mStartLevel);
		button.getButton().addListener(mClickListener);

		button = mLevel1.get("level4_hard");
		button.getButton().setColor(1f, 0f, 0f, .65f);
		mLevelsTable.add(button.getButton()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		mLevelsBG.add(button.getGlow()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		button.getButton().addListener(mStartLevel);
		button.getButton().addListener(mClickListener);

		button = mLevel1.get("level2_hard");
		button.getButton().setColor(1f, 0f, 0f, .65f);
		mLevelsTable.add(button.getButton()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		mLevelsBG.add(button.getGlow()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		button.getButton().addListener(mStartLevel);
		button.getButton().addListener(mClickListener);

		button = mLevel1.get("level8_hard");
		button.getButton().setColor(1f, 0f, 0f, .65f);
		mLevelsTable.add(button.getButton()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		mLevelsBG.add(button.getGlow()).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		button.getButton().addListener(mStartLevel);
		button.getButton().addListener(mClickListener);

	}

	private void createCustomizeMenu() {
		mCustomizeTable = new Table(mSkin);
		mCustomizeTable.setFillParent(true);
		mBaseCustomizeTable = new Table(mSkin);
		mBaseCustomizeTable.setFillParent(true);
		mJoinImage = new Image(new Texture(Gdx.files.internal("data/gfx/Inst1.png")));
		mStartImage = new Image(new Texture(Gdx.files.internal("data/gfx/Inst2.png")));
		Texture t = new Texture(Gdx.files.internal("data/gfx/InstBlank.png"));
		Image baseImage = new Image(t);
		baseImage.setColor(1, 1, 1, 0.75f);
		mBaseCustomizeTable.add(baseImage);
		// mBaseCustomizeTable.align(Align.top);
		// mCustomizeTable.align(Align.top);
	}

	private void createControllerMenu() {
		mControllerTable = new Table(mSkin);
		mControllerTable.setFillParent(true);
		mControllerOverTable = new Table(mSkin);
		mControllerOverTable.setFillParent(true);
		ImageButton imageButton;
		imageButton = new SiggdImageButton("data/gfx/backButton.png").getButton();
		imageButton.setColor(1, 1, 1, .75f);
		imageButton.addListener(mMainMenu);
		imageButton.addListener(mClickListener);
		mControllerTable.add(imageButton).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);

		imageButton = new SiggdImageButton("data/gfx/controllerconfig1.png").getButton();
		imageButton.setDisabled(true);
		mControllerTable.add(imageButton);
		imageButton = new SiggdImageButton("data/gfx/backButton.png").getButton();
		imageButton.addListener(mMainMenu);
		imageButton.addListener(mClickListener);
		mControllerTable.add(imageButton).space(mVerticalSpacing, mHorizontalSpacing,
				mVerticalSpacing, mHorizontalSpacing);
		imageButton.setDisabled(true);
		imageButton.setVisible(false);
		mControllerLeft = new Image(new Texture(Gdx.files.internal("data/gfx/controllerleft.png")));
		mControllerRight = new Image(
				new Texture(Gdx.files.internal("data/gfx/controllerright.png")));
		mControllerPoof = new Image(new Texture(Gdx.files.internal("data/gfx/controllerpoof.png")));
		mControllerSolid = new Image(
				new Texture(Gdx.files.internal("data/gfx/controllersolid.png")));
		mControllerStart = new Image(
				new Texture(Gdx.files.internal("data/gfx/controllerstart.png")));
		mControllerDown = new Image(new Texture(Gdx.files.internal("data/gfx/controllerdown.png")));
		mControllerUp = new Image(new Texture(Gdx.files.internal("data/gfx/controllerup.png")));
	}

	public void render() {

		// Table.drawDebug(mStage);
		mShapeRenderer.setProjectionMatrix(mStage.getCamera().combined);

		JSONObject levelSave = Game.get().getLevel().getLevelSave();
		if (LEVELS.equals(mCurrentMenu)) {
			for (String s : mLevel1.keySet()) {
				ImageButton tmp = mLevel1.get(s).getButton();
				Image glow = mLevel1.get(s).getGlow();
				float x = tmp.getX();
				float y = tmp.getY() - 20;
				float width = tmp.getWidth();
				float progress = 0;
				try {
					JSONObject level = levelSave.getJSONObject(s);
					progress = (float) level.getDouble("progress");
				} catch (JSONException e) {
					// Level or Progress does not exist, default to 0
					progress = 0;
				}
				mShapeRenderer.begin(ShapeType.Filled);
				mShapeRenderer.setColor(Color.BLACK);
				mShapeRenderer.rect(x, y, width, 10);
				if (progress < 1) {
					mShapeRenderer.setColor(new Color(.7f, progress * .4f, 0, 1));
				} else {
					mShapeRenderer.setColor(new Color(0, .7f, 0, 1));
				}
				mShapeRenderer.rect(x + 2, y + 2, (width - 4) * progress, 6);
				mShapeRenderer.end();
				if (tmp.isTransform() && !tmp.isDisabled()) {
					glow.setVisible(true);
					glow.setColor(mGlowColor);
				} else {
					glow.setVisible(false);
				}
			}
		} else if (CUSTOMIZE.equals(mCurrentMenu)) {
			if (Game.get().activePlayersNum() > 0) {
				mJoinImage.remove();
				if (!mStartImage.isDescendantOf(mCustomizeTable)) {
					mRollingAlpha = 0;
					mHintTimer = 0;
					mCustomizeTable.add(mStartImage);
					mBaseCustomizeTable.setColor(1, 1, 1, 0);
					mCustomizeTable.setColor(1, 1, 1, 0);
				}
			}
		} else if (LOADING.equals(mCurrentMenu)) {
			mShapeRenderer.begin(ShapeType.Filled);
			mShapeRenderer.setColor(Color.DARK_GRAY);
			float x = mStage.getWidth() / 2;
			float y = mStage.getHeight() / 2;
			mShapeRenderer.rect(x - 102, y - 7, 204, 14);
			mShapeRenderer.setColor(new Color(0, .75f, 0, 1));
			mShapeRenderer.rect(x - 100, y - 5,
					((200f * (Game.get().mLoaderMax - Game.get().mHackishLoader.size())) / Game
							.get().mLoaderMax), 10);
			mShapeRenderer.end();
		}
		mRollingAlpha += .03f;
		Color tColor = new Color(1.0f, 1.0f, 1.0f, (float) (1 - Math.cos(mRollingAlpha)) / 2.0f);
		mStartImage.setColor(tColor);
		mJoinImage.setColor(tColor);
		mStage.act(Gdx.graphics.getDeltaTime());
		mStage.draw();
		if (!LOADING.equals(mCurrentMenu))
			mMenuController.draw(mShapeRenderer);
	}

	public void update() {
		mGlow += .05f;
		mGlowColor = mMenuController.getCurColor().cpy();
		mGlowColor.a = ((float) ((1 - Math.cos(mGlow)) / 2f));
		mTwoSecondTimer++;
		if (mTwoSecondTimer > 120)
			mTwoSecondTimer -= 120;
		mMenuController.update();
		if (CUSTOMIZE.equals(mCurrentMenu) && Game.get().getState() != Game.PLAY) {
			customizeMenuUpdate();
		} else if (CONTROLLER.equals(mCurrentMenu)) {
			controllerMenuUpdate();
		}
	}

	public void dispose() {
		mStage.dispose();
	}

	private Controller mBindingController;
	private Binding mCustomBinding;
	private int mBindingDelay;
	private static int BINDING_MENU_DELAY = 0;

	private class Binding {
		public int POOF = -1;
		public int TRANSFORM = -1;
		public int START = -1;
		public int LRAXIS = -1;
		public int UDAXIS = -1;
	}

	private void controllerMenuUpdate() {
		if (mBindingController == null) {
			if (mBindingDelay <= 0) {
				mMenuController.ignore = true;
				mBindingController = testForBindingController();
				mCustomBinding = new Binding();
				// Set left/right immediately
				// mBindingDelay = BINDING_MENU_DELAY / 2;
				mControllerOverTable.clear();
				if (mTwoSecondTimer < 40) {
					mControllerOverTable.add(mControllerLeft);
				} else if (mTwoSecondTimer >= 60 && mTwoSecondTimer < 100) {
					mControllerOverTable.add(mControllerRight);
				}
				if (mBindingController != null)
					mTwoSecondTimer = 0;
			} else {
				mBindingDelay--;
			}
		} else {
			// listen for keys to bind\

			if (mBindingDelay <= 0) {
				mControllerOverTable.clear();
				if (mCustomBinding.LRAXIS == -1) {
					if (testForPov(mBindingController) > 0) {
						mCustomBinding.LRAXIS = ControllerFilterAPI.AXIS_LEFT_LR;
					} else {
						if (mTwoSecondTimer < 40) {
							mControllerOverTable.add(mControllerLeft);
						} else if (mTwoSecondTimer >= 60 && mTwoSecondTimer < 100) {
							mControllerOverTable.add(mControllerRight);
						}
						mCustomBinding.LRAXIS = testForAxis(mBindingController);
					}
					if (mCustomBinding.LRAXIS != -1) {
						Game.get().playTickSound();
						mTwoSecondTimer = 0;
					}
				} else if (mCustomBinding.POOF == -1) {
					mCustomBinding.POOF = testForButton(mBindingController);
					if ((mTwoSecondTimer / 30) % 2 == 0)
						mControllerOverTable.add(mControllerPoof);
					if (mCustomBinding.POOF != -1) {
						Game.get().playTickSound();
						mTwoSecondTimer = 0;
					}
				} else if (mCustomBinding.TRANSFORM == -1) {
					mCustomBinding.TRANSFORM = testForButton(mBindingController);
					if ((mTwoSecondTimer / 30) % 2 == 0)
						mControllerOverTable.add(mControllerSolid);
					if (mCustomBinding.TRANSFORM == mCustomBinding.POOF) {
						mCustomBinding.TRANSFORM = -1;
					}
					if (mCustomBinding.TRANSFORM != -1) {
						Game.get().playTickSound();
						mTwoSecondTimer = 0;
					}
				} else if (mCustomBinding.START == -1) {
					mCustomBinding.START = testForButton(mBindingController);
					if ((mTwoSecondTimer / 30) % 2 == 0)
						mControllerOverTable.add(mControllerStart);
					if (mCustomBinding.START == mCustomBinding.TRANSFORM
							|| mCustomBinding.START == mCustomBinding.POOF) {
						mCustomBinding.START = -1;
					}
					if (mCustomBinding.START != -1) {
						Game.get().playTickSound();
						mTwoSecondTimer = 0;
					}
				} else if (mCustomBinding.UDAXIS == -1) {
					if (testForPov(mBindingController) > 0) {
						mCustomBinding.UDAXIS = ControllerFilterAPI.AXIS_LEFT_UD;
					} else {
						if (mTwoSecondTimer < 40) {
							mControllerOverTable.add(mControllerUp);
						} else if (mTwoSecondTimer >= 60 && mTwoSecondTimer < 100) {
							mControllerOverTable.add(mControllerDown);
						}
						mCustomBinding.UDAXIS = testForAxis(mBindingController);
					}
					if (mCustomBinding.UDAXIS == mCustomBinding.LRAXIS) {
						mCustomBinding.UDAXIS = -1;
					}
					if (mCustomBinding.UDAXIS != -1) {
						Game.get().playNomSound();
						mTwoSecondTimer = 0;
					}
				} else {
					JSONObject bindings = new JSONObject();
					JSONObject buttons = new JSONObject();
					JSONObject axis = new JSONObject();
					try {
						buttons.put("BUTTON_A", mCustomBinding.POOF);
						buttons.put("BUTTON_B", mCustomBinding.TRANSFORM);
						buttons.put("BUTTON_START", mCustomBinding.START);
						axis.put("AXIS_LEFT_UD", mCustomBinding.UDAXIS);
						axis.put("AXIS_LEFT_LR", mCustomBinding.LRAXIS);
						bindings.put("Buttons", buttons);
						bindings.put("Axes", axis);
						ControllerFilterAPI.saveCustomBinding(mBindingController.getName(),
								bindings);
					} catch (JSONException e) {
						System.out.println("Failed creating binding obj");
					}
					mBindingController = null;
					setMenu(MAIN);
				}
				mBindingDelay = BINDING_MENU_DELAY / 2;
			} else {
				mBindingDelay--;
			}
		}
	}

	private int testForAxis(Controller c) {
		if (c == null) {
			System.out.println("NULL CONTROLLER");
			return -1;
		}
		for (int i = 0; i < 4; i++) {
			if (Math.abs(c.getAxis(i)) > 0.75f) {
				return i;
			}
		}
		return -1;
	}

	private int testForButton(Controller c) {
		if (c == null) {
			System.out.println("NULL CONTROLLER");
			return -1;
		}
		for (int i = 0; i < 10; i++) {
			if (c.getButton(i)) {
				return i;
			}
		}
		return -1;
	}

	private int testForPov(Controller c) {
		if (c == null) {
			System.out.println("NULL CONTROLLER");
			return -1;
		}
		if (c.getPov(0) != PovDirection.center)
			return 1;
		// DebugOutput.info(this, c.getPov(0)+ " ");
		return -1;
	}

	private Controller testForBindingController() {
		for (Controller c : Controllers.getControllers()) {
			for (int i = 0; i < 10; i++) {
				if (c.getButton(i)) {
					return c;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (Math.abs(c.getAxis(i)) > 0.75f) {
					return c;
				}
			}
			if (c.getPov(0) != PovDirection.center)
				return c;
		}
		return null;
	}

	private void customizeMenuUpdate() {
		mHintTimer++;
		if (mHintTimer > 120 && mHintTimer <= 180) {
			mCustomizeTable.setColor(1, 1, 1, (mHintTimer - 120) / 60f);
			mBaseCustomizeTable.setColor(1, 1, 1, (mHintTimer - 120) / 60f);
		}
		Player p = null;
		if (mDelay > 10) {
			p = testForNewPlayer();
		} else {
			mDelay++;
		}
		if (p != null) {
			if (!Game.get().playerExists(p)) {
				System.out.println("Adding player of type: " + p.controltype);
				Game.get().addPlayer(p);
			}
			activatePlayer(p);
		}
		boolean start = false;
		for (Player pl : Game.get().getPlayers()) {
			if (!pl.active)
				continue;
			if (pl.controltype == ControlType.Controller && pl.controller != null) {
				Controller c = pl.controller;
				start = start
						|| c.getButton(ControllerFilterAPI.getButtonFromFilteredId(c,
								ControllerFilterAPI.BUTTON_START));
			} else if (pl.controltype == ControlType.WASD || pl.controltype == ControlType.Arrows) {
				start = start || Gdx.input.isKeyPressed(Input.Keys.SPACE)
						|| Gdx.input.isKeyPressed(Input.Keys.ENTER);
			}
			if (start)
				break;
		}
		if (start) {
			for (Player pl : Game.get().getPlayers()) {
				if (pl.active && pl.controltype == ControlType.Controller && pl.controller != null) {
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

	/**
	 * Checks controllers for new player, returns the first candidate
	 * 
	 * @return Player to add to game or null if there is no new player
	 */
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
				for (int i = 0; i < 4; i++) {
					if (Math.abs(c.getAxis(i)) > 0.25f) {
						// unassigned controller stick wiggled
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
			if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)
					|| Gdx.input.isKeyPressed(Input.Keys.DOWN)
					|| Gdx.input.isKeyPressed(Input.Keys.UP)) {
				Player inactivePlayer = Game.get().getPlayer(org.siggd.Player.ControlType.Arrows);
				if (inactivePlayer == null) {
					p = new Player(Game.get().getNumberOfPlayers());
					p.controltype = org.siggd.Player.ControlType.Arrows;
				} else {
					if (!inactivePlayer.active) {
						p = inactivePlayer;
					}
				}
			} else if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.D)
					|| Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.W)) {
				Player inactivePlayer = Game.get().getPlayer(org.siggd.Player.ControlType.WASD);
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
			b.postLoad();
			b.setActive(true);
			l.addActor(b);
			Vector2 pos = mSpawnPos.remove((int) ((mSpawnPos.size()) * Math.random()));
			b.setX(pos.x);
			b.setY(pos.y);
			b.setLayer(4);
			b.setProp("Player ID", p.id);
		}
	}

	private final ClickListener mClickListener = new ClickListener() {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			Game.get().playNomSound();
		}

	};

	private final ChangeListener mStartCampaign = new ChangeListener() {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			setMenu(LEVELS);
		}
	};

	private final ChangeListener mReset = new ChangeListener() {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			Game.get().setState(Game.PLAY);
			String level = Game.get().getLevel().getAssetKey();
			if ("base".equals(level))
				level = "gen";
			Game.get().setLevel(level);
			Game.get().getLevel().killFade();
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
			Game.get().setPaused(false);
			Game.get().setState(Game.PLAY);
		}
	};

	private final ChangeListener mMainMenu = new ChangeListener() {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			if (!"earth".equals(Game.get().getLevel().getAssetKey())) {
				Game.get().setNextLevel("earth");
				Game.get().setPaused(false);
				Game.get().getLevelView().resetCamera();
			}
			Game.get().deactivatePlayers();
			mMenuController.ignore = false;
			setMenu(MAIN);
		}
	};

	private final ChangeListener mClear = new ChangeListener() {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			try {
				FileHandle handleSt = Gdx.files.external(".BlobGame/BlobSave.json");

				handleSt.writeString("", false);
			} catch (Exception e) {
				//DebugOutput.info(this, e.getStackTrace().toString());
			}
			Game.get().getLevel().loadFromLevelSave();
		}
	};

	private final ChangeListener mController = new ChangeListener() {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			setMenu(CONTROLLER);
		}
	};

	private final ChangeListener mRaceMode = new ChangeListener() {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			mSelectedLevel = "gen";
			setMenu(CUSTOMIZE);
		}
	};

	private final ChangeListener mExit = new ChangeListener() {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			Game.get().exit();
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
		mMenuController.ignore = false;
		mCurrentMenu = menu;
		mDelay = 0;
		mBindingController = null;
		mMainTable.remove();
		mPauseTable.remove();
		mFakePauseTable.remove();
		mLevelsTable.remove();
		mLevelsBG.remove();
		mBaseCustomizeTable.remove();
		mCustomizeTable.remove();
		mStartImage.remove();
		mTint.remove();
		mControllerTable.remove();
		mControllerOverTable.remove();
		if (MAIN.equals(menu)) {
			mStage.addActor(mMainTable);
			if ((Game.UNLOCKED || Level.HARD_PASSED)
					&& !mMainTable.getChildren().contains(mRaceButton.getButton(), true)) {
				mMainTable.clear();
				mMainTable.add(mCampaignButton.getButton());
				mMainTable.add(mRaceButton.getButton());
				// mMainTable.add(mClearButton);
				if (Controllers.getControllers().size > 0) {
					mMainTable.add(mControllerButton.getButton());
				}
				mMainTable.add(mExitButton.getButton());
			}
			mMenuController.setTable(mMainTable);
			if (Game.RELEASE) {
				Game.get().deactivatePlayers();
			}
		} else if (PAUSE.equals(menu)) {
			mStage.addActor(mTint);
			mStage.addActor(mPauseTable);
			mMenuController.setTable(mPauseTable);
			mMenuController.setIndex(2);
		} else if (FAKE_PAUSE.equals(menu)) {
			mStage.addActor(mTint);
			mStage.addActor(mFakePauseTable);
			mMenuController.setTable(mFakePauseTable);
		} else if (LEVELS.equals(menu)) {
			mStage.addActor(mLevelsBG);
			mStage.addActor(mLevelsTable);
			if (!Game.UNLOCKED) {
				JSONObject levelSave = Game.get().getLevel().getLevelSave();
				for (String s : mLevel1.keySet()) {
					ImageButton tmp = mLevel1.get(s).getButton();
					float progress = 0;
					try {
						JSONObject level = levelSave.getJSONObject(s);
						boolean unlocked = level.getBoolean("unlocked");
						tmp.setDisabled(!unlocked);
					} catch (JSONException e) {
						// Level or unlocked property not present
						tmp.setDisabled(true);
					}
					try {
						JSONObject level = levelSave.getJSONObject(s);
						progress = (float) level.getDouble("progress");
					} catch (JSONException e) {
						progress = 0;
					}
					tmp.setTransform(progress < 1);
				}
				String firstHardLevel = Level.LEVELS[0] + Level.HARD_SUFFIX;
				if (Level.MEDEASY_COMPLETE) {
					mLevel1.get(firstHardLevel).getButton().setDisabled(false);
				}
			}
			mGlow = 0;
			mMenuController.setTable(mLevelsTable);
			if (!Level.COMPLETE) {
				mMenuController.selectFirstCheckedAvailable();
			} else {
				mMenuController.setIndex(1);
			}
		} else if (CUSTOMIZE.equals(menu)) {
			mSpawnPos.clear();

			mSpawnPos.add(new Vector2(-6.0547f, -2.0357f));
			mSpawnPos.add(new Vector2(-2.0885f, -0.9769f));
			mSpawnPos.add(new Vector2(-0.7361f, 0.6420f));
			mSpawnPos.add(new Vector2(0.5428f, -1.2608f));
			mSpawnPos.add(new Vector2(2.4356f, 0.1152f));
			mSpawnPos.add(new Vector2(2.4956f, -2.3311f));
			mSpawnPos.add(new Vector2(4.2844f, -1.2078f));
			mSpawnPos.add(new Vector2(3.7163f, -3.7335f));

			mHintTimer = 0;
			if (!mJoinImage.isDescendantOf(mCustomizeTable)) {
				mRollingAlpha = 0;
				mCustomizeTable.add(mJoinImage);
			}
			mStage.addActor(mBaseCustomizeTable);
			mStage.addActor(mCustomizeTable);
			mBaseCustomizeTable.setColor(1, 1, 1, 0);
			mCustomizeTable.setColor(1, 1, 1, 0);
			mMenuController.setTable(null);
			Game.get().setLevel("charselect");
		} else if (CONTROLLER.equals(menu)) {
			mBindingDelay = BINDING_MENU_DELAY;
			mStage.addActor(mControllerTable);
			mStage.addActor(mControllerOverTable);
			mMenuController.setTable(mControllerTable);
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
		if (width < 1280)
			width *= 2;
		if (height < 640)
			height *= 2;
		mStage.setViewport(width, height, true);
	}
}
