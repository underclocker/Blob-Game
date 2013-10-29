package org.siggd.view;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Logger;

import org.box2dLight.LightMap;
import org.box2dLight.RayHandler;
import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.actor.Actor;
import org.siggd.actor.Blob;
import org.siggd.actor.Spawner;
import org.siggd.editor.ActorPanel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;

/**
 * This class draws a level on the screen (it is the View of MVC)
 * 
 * @author mysterymath
 * 
 */
public class LevelView {
	public static final float MIN_WIDTH = 16; // /< Minimum Width of the
												// viewport, in meters
	public static final float MIN_HEIGHT = 9; // /< Minimum Height of the
												// viewport, in meters
	// Note: the above also define the nominal aspect ratio

	public static final float vWIDTH = 1920; // /< "Virtual" width of the
												// viewport, in pixels
	public static final float vHEIGHT = 1080; // /< "Virtual" height of the
												// viewport, in pixels
	public static final float BORDER = 3.5f; // /< Border around visible
												// display,
												// in meters. Only used in
												// multiplayer
	public static final float CAM_SMOOTH = .08f; // /< coefficient for how much
													// closer the camera gets to
													// its
													// correct location per
													// iteration.
	public static boolean mUseLights = true;
	private OrthographicCamera mCamera; // /< The camera
	private OrthographicCamera mOldCamera; // /< The camera
	private RayHandler mRayHandler; // /< Lighting overlay
	private SpriteBatch mBatch; // /< Object used to efficiently render bunches
								// of sprites
	private ShapeRenderer mShapeRenderer;
	private final float mVScale; // /< The virtual scale factor, from meters to
									// pixels
	private float mScale; // /< The current scale factor, from meters to pixels
	private float mOldScale;
	private float mWidth; // /< Width of the viewport, in meters
	private float mHeight; // /< Height of the viewport, in meters
	private Box2DDebugRenderer mDebug; // /< A debug renderer for box2d
	private boolean mDoDebugRender = false; // /< True if we should do physics
											// debug rendering
	// private boolean mDoFramerateRender = true; ///< True if we should do
	// Framerate rendering
	private Rectangle mClip; // /< Clipping rectangle, in screen coords
	private ShaderProgram mDefaultShaderProgram;;

	private boolean mFullScreenEnabled;
	private int mOldWidth;
	private int mOldHeight;

	// Store min and max extents, for zoom calculation
	private float mMinX = Float.MAX_VALUE, mMaxX = -Float.MAX_VALUE, mMinY = Float.MAX_VALUE,
			mMaxY = -Float.MAX_VALUE;

	private static final Logger mLog = Logger.getLogger(LevelView.class.getName());

	/**
	 * Constructor
	 */
	public LevelView() {
		int w = Gdx.graphics.getWidth();
		int h = Gdx.graphics.getHeight();
		// Fullscreen toggle parameters
		mFullScreenEnabled = false;
		// Set virtual scale (it never changes)
		mVScale = vHEIGHT / MIN_HEIGHT;
		mOldWidth = w;
		mOldHeight = h;

		// Create Camera and SpriteBatch
		mCamera = new OrthographicCamera();
		mOldCamera = new OrthographicCamera();
		mBatch = new SpriteBatch();
		mShapeRenderer = new ShapeRenderer();
		mDebug = new Box2DDebugRenderer();

		if (mUseLights) {
			mRayHandler = new RayHandler(null, w / 8, h / 8);
		}

		// Set width and height
		mWidth = MIN_WIDTH;
		mHeight = MIN_HEIGHT;

		mClip = new Rectangle();

		GLCommon gl10 = Gdx.graphics.getGLCommon();
		gl10.glEnable(GL10.GL_LINE_SMOOTH);
		gl10.glHint(GL10.GL_LINE_SMOOTH_HINT, GL10.GL_NICEST);

		// Call onResize, so scale information is available in Game.create()
		onResize(w, h);
		mOldScale = mScale;
		String vertexShader = "attribute vec4 a_position;\n" + "attribute vec4 a_color;\n"
				+ "uniform mat4 u_worldView;\n" + "varying vec4 v_color; \n" + "void main(){\n"
				+ "v_color = a_color;\n" + "gl_Position = u_worldView * a_position;\n" + "}\n";
		String fragmentShader = "#ifdef GL_ES                \n" + "precision mediump float;    \n"
				+ "#endif                      \n" + "varying vec4 v_color;       \n"
				+ "void main()                 \n" + "{                           \n"
				+ "  gl_FragColor = v_color;   \n" + "}                           \n";
		if (Gdx.graphics.isGL20Available()) {
			mDefaultShaderProgram = new ShaderProgram(vertexShader, fragmentShader);
			if (!mDefaultShaderProgram.isCompiled())
				throw new IllegalStateException(mDefaultShaderProgram.getLog());
		}
	}

	/**
	 * Render the level, one layer at a time. The draw order for objects in the
	 * same layer is undefined. Objects in lower layers are drawn before (and
	 * thus behind) objects in higher layers.
	 */
	public void render() {
		// Get the Game state
		int state = Game.get().getState();

		// Get the level
		Level level = Game.get().getLevel();

		// Update ambient light level
		if (mRayHandler != null) {
			mRayHandler.setAmbientLight(.1f, .1f, .1f, level.getAmbientLight());
			mRayHandler.setCombinedMatrix(mCamera.combined);
		}

		// Clear the screen
		Gdx.gl.glClearColor(.0f, .0f, .0f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		// Draw sprites
		mBatch.setProjectionMatrix(mCamera.combined);
		mShapeRenderer.setProjectionMatrix(mCamera.combined);

		if (state == Game.PLAY || state == Game.MENU) {
			// Calculate clipping rectangle
			float minX = Convert.getFloat(level.getProp("Min Camera X"));
			float minY = Convert.getFloat(level.getProp("Min Camera Y"));
			float maxX = Convert.getFloat(level.getProp("Max Camera X"));
			float maxY = Convert.getFloat(level.getProp("Max Camera Y"));
			Rectangle worldClip = new Rectangle(minX, minY, maxX - minX, maxY - minY);
			ScissorStack.calculateScissors(mCamera, new Matrix4(), worldClip, mClip);

			// Apply clipping
			ScissorStack.pushScissors(mClip);
		}

		// mShapeRenderer.begin(ShapeType.Line);
		// Collect a sorted list of the layer numbers
		Integer[] layers = auditLayers();
		for (Integer lNum : layers) {
			if (Game.get().getEditor() != null && state == Game.EDIT
					&& Game.get().getEditor().mLayerPanel.noShow.contains(lNum)) {
				// don't draw actors if the layer isn't checked
				continue;
			}
			// Begin layer of sprites
			mBatch.begin();

			for (Actor a : level) {
				int aLayNum = Convert.getInt(a.getProp("Layer"));
				if (aLayNum != lNum) {
					continue;
				}

				// Edit mode: (tint selected actor)
				if (state == Game.EDIT) {
					if (a == Game.get().getEditor().getSelectedActor()) {
						switch (Game.get().getEditor().getActorEditState()) {
						case SELECTMOVE:
							mBatch.setColor(Color.BLUE);
							break;
						case ADD:
							break;
						case REMOVE:
							mBatch.setColor(Color.RED);
							break;
						}
					}
				}
				if (a.isActive() || state == Game.EDIT) {
					Drawable draw = a.getDrawable();
					if (draw != null
							&& (Convert.getInt(a.getProp("Visible")) == 1 || state == Game.EDIT)) {
						// Draw the sprite component of the actor
						try {
							draw.drawSprite(mBatch);
						} catch (Exception e) {
							mLog.severe("Exception drawing sprite for actor " + a.getId() + ": "
									+ e);
						}
					}
				}

				// remove any tint caused by editor
				if (state == Game.EDIT) {
					// Draw the select point
					float x = a.getX();
					float y = a.getY();
					float angle = a.getMainBody().getAngle();
					Texture tex = Game.get().getAssetManager()
							.get(Game.get().getEditor().selectPoint, Texture.class);
					Vector2 mOrigin = new Vector2(tex.getWidth() / 4 / mVScale, tex.getHeight() / 4
							/ mVScale); // 4 is half of half
					mBatch.draw(tex, x - mOrigin.x, y - mOrigin.y, tex.getWidth() / 2 / mVScale,
							tex.getHeight() / 2 / mVScale); // scale image down
															// by 2
					mBatch.setColor(Color.WHITE);
				}
			}

			// mShapeRenderer.end();
			// End layer of sprites
			mBatch.end();

			// Begin layer of non-sprites
			for (Actor a : level) {
				int aLayNum = Convert.getInt(a.getProp("Layer"));
				if (aLayNum != lNum) {
					continue;
				}
				Drawable draw = a.getDrawable();
				if (draw != null && (a.isActive() || state == Game.EDIT)) {
					// Draw the non-sprite component of the actor
					try {
						draw.drawElse(mShapeRenderer);
					} catch (Exception e) {
						mLog.severe("Exception drawing custom graphics for actor " + a.getId()
								+ ": " + e);
					}
				}
			}
			// End layer of non-sprites

		}

		if (state == Game.EDIT && Game.get().getEditor() != null) {

			if (Game.get().getEditor().getActorEditState() == ActorPanel.Action.ADD
					&& Game.get().getEditor().getSelectedActor() != null) {
				// Get Selected Actor
				Actor a = Game.get().getEditor().getSelectedActor();
				Drawable draw = a.getDrawable();
				if (draw != null) {
					mBatch.setColor(1f, 1f, 1f, .38f);

					// Translate so that the screen pos is 0,0
					Vector3 screenPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
					mCamera.unproject(screenPos);
					mCamera.translate(screenPos);

					// Draw the actor
					// Begin drawing hover actor
					mBatch.begin();
					draw.drawSprite(mBatch);
					mBatch.end();
					draw.drawElse(mShapeRenderer);

					mBatch.setColor(Color.WHITE);

					// Reset the camera
					mCamera.translate(-screenPos.x, -screenPos.y, 0);
				}
			}
		}

		if (state == Game.PLAY || state == Game.MENU) {
			// End clipping
			ScissorStack.popScissors();
		}

		if (mRayHandler != null && Convert.getInt(Game.get().getLevel().getProp("Use Light")) != 0) {
			mRayHandler.updateAndRender();
		}

		// Debug renderer
		if (mDoDebugRender) {
			mDebug.render(Game.get().getLevel().getWorld(), mCamera.combined);

			for (Actor a : level) {
				Drawable draw = a.getDrawable();
				if (draw != null) {
					// Draw the debug component of the actor
					try {
						draw.drawDebug(mCamera);
					} catch (Exception e) {
						mLog.severe("Exception drawing debug information for actor " + a.getId()
								+ ": " + e);
					}
				}
			}
		}

		// TODO: ADD FRAMERATE renderer to the screen
		mMinX = Float.MAX_VALUE;
		mMaxX = -Float.MAX_VALUE;
		mMinY = Float.MAX_VALUE;
		mMaxY = -Float.MAX_VALUE;
	}

	public void update() {
		mOldCamera.position.x = mCamera.position.x;
		mOldCamera.position.y = mCamera.position.y;
		mOldCamera.viewportWidth = mCamera.viewportWidth;
		mOldCamera.viewportHeight = mCamera.viewportHeight;
		if (Game.get().getState() == Game.PLAY) {
			positionCamera(true);
		}
	}

	private void smoothCam() {

		float delta, scale;

		delta = (mCamera.position.x - mOldCamera.position.x);
		scale = scaleSmooth(delta);
		delta *= scale;

		mCamera.position.x = mOldCamera.position.x + delta;

		delta = (mCamera.position.y - mOldCamera.position.y);
		scale = scaleSmooth(delta);
		delta *= scale;

		mCamera.position.y = mOldCamera.position.y + delta;

		float deltax = (mCamera.viewportWidth - mOldCamera.viewportWidth);
		scale = scaleSmooth(deltax);
		float deltay = (mCamera.viewportHeight - mOldCamera.viewportHeight);
		float scalecache = scaleSmooth(deltay);

		if (scalecache > scale)
			scale = scalecache;

		deltax *= scale;
		deltay *= scale;

		mCamera.viewportHeight = mOldCamera.viewportHeight + deltay;
		mCamera.viewportWidth = mOldCamera.viewportWidth + deltax;

		mScale = mOldScale + (mScale - mOldScale) * CAM_SMOOTH;
		mOldScale = mScale;

	}

	public float scaleSmooth(float delta) {
		float abs = Math.abs(delta);
		return (40f / (200f + 10f * abs + 250f * (float) Math.sqrt(abs)));
	}

	public void setWorld(World world) {
		if (mRayHandler != null)
			mRayHandler.setWorld(world);
	}

	public ShaderProgram getDefaultShaderProgram() {
		return mDefaultShaderProgram;
	}

	public OrthographicCamera getCamera() {
		return mCamera;
	}

	public Vector2 getLevelCenter() {
		Level lev = Game.get().getLevel();
		float minX = Convert.getFloat(lev.getProp("Min Camera X"));
		float minY = Convert.getFloat(lev.getProp("Min Camera Y"));
		float maxX = Convert.getFloat(lev.getProp("Max Camera X"));
		float maxY = Convert.getFloat(lev.getProp("Max Camera Y"));
		return (new Vector2((minX + maxX) / 2f, (minY + maxY) / 2f));
	}

	public void positionCamera(boolean smooth) {
		Level lev = Game.get().getLevel();
		int state = Game.get().getState();

		ArrayList<Blob> blobs = lev.getBlobs(true);
		for (Blob b : blobs) {
			setCameraPosition(new Vector2(b.getX(), b.getY()));
		}

		ArrayList<Spawner> spawners = lev.getSpawners(true);
		for (Spawner s : spawners) {
			if (s.blobsContained() > 0) {
				setCameraPosition(new Vector2(s.getX(), s.getY()));
			}
		}
		float minX = Convert.getFloat(lev.getProp("Min Camera X"));
		float minY = Convert.getFloat(lev.getProp("Min Camera Y"));
		float maxX = Convert.getFloat(lev.getProp("Max Camera X"));
		float maxY = Convert.getFloat(lev.getProp("Max Camera Y"));
		// Calculate scale and do "resize"
		mCamera.position.x = (mMaxX + mMinX) / 2;
		mCamera.position.y = (mMaxY + mMinY) / 2;
		calcScale(maxX - minX, maxY - minY);

		onResize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		if (smooth) {
			smoothCam();
		}
		// Clip the new camera position if playing
		if (state == Game.PLAY) {
			clipCam(minX, maxX, minY, maxY);
		}
		mCamera.update();
	}

	/**
	 * Destructor
	 */
	public void dispose() {
		// Destroy the sprite batcher
		mBatch.dispose();
	}

	/**
	 * Determines which layer numbers exist, so that non-existent layers can be
	 * skipped
	 * 
	 * @return
	 */
	public Integer[] auditLayers() {
		HashSet<Integer> layerSet = new HashSet<Integer>();
		Level level = Game.get().getLevel();
		for (Actor a : level) {
			Integer lNum = Convert.getInt(a.getProp("Layer"));
			if (lNum != null) {
				layerSet.add(lNum);
			}
		}

		Integer[] ret = layerSet.toArray(new Integer[layerSet.size()]);
		Arrays.sort(ret);
		return ret;
	}

	/**
	 * Called on resize.
	 */
	public void onResize(int width, int height) {
		float cachedScale = mScale;
		// Calculate the viewport size needed for a mWidthxmHeight region to be
		// displayed within the window, as large as possible
		float widthForHeight = mHeight / height * width; // The width in meters
															// that should be
															// displayed if
															// mHeight is used
		float heightForWidth = mWidth / width * height; // The height in meters
														// that should be
														// displayed if mWidth
														// is used

		if (widthForHeight <= mWidth) { // If the obtainable width would be
										// smaller than we want, show more
										// height
			mCamera.viewportWidth = mWidth;
			mCamera.viewportHeight = heightForWidth;
			mScale = width / mWidth;
		} else if (heightForWidth <= mHeight) { // If the obtainable height
												// would be smaller than what we
												// want, show more width
			mCamera.viewportWidth = widthForHeight;
			mCamera.viewportHeight = mHeight;
			mScale = height / mHeight;
		} else {
			// So close there are floating point errors
			mCamera.viewportWidth = mWidth;
			mCamera.viewportHeight = mHeight;
			mScale = (width / mWidth + height / mHeight) / 2;
		}

		// Update the camera's matrices
		mCamera.update();

		if (mRayHandler != null && cachedScale != mScale) {
			LightMap lightMap = mRayHandler.getLightMap();
			width /= 8;
			height /= 8;
			if (lightMap != null && !(lightMap.mWidth == width && lightMap.mHeight == height)) {
				lightMap.constructLightMap(width, height);
			}
		}
	}

	// ACCESSORS & MUTATORS

	/**
	 * Returns the SpriteBatch
	 * 
	 * @return Returns the SpriteBatch
	 */
	public SpriteBatch getSpriteBatch() {
		return mBatch;
	}

	/**
	 * Returns the SpriteBatch
	 * 
	 * @return Returns the SpriteBatch
	 */
	public RayHandler getRayHandler() {
		return mRayHandler;
	}

	/**
	 * Returns the scale (in units of pixels/meter)
	 * 
	 * @return the Scale
	 */
	public float getScale() {
		return mScale;
	}

	/**
	 * Returns the virtual scale (in units of virtual pixels/meter)
	 * 
	 * @return the Scale
	 */
	public float getVScale() {
		return mVScale;
	}

	/**
	 * Sets whether debug renderer should be used
	 */
	public void setDebugRender(boolean val) {
		mDoDebugRender = val;
	}

	/**
	 * Toggles whether debug renderer should be used
	 */
	public void toggleDebugRender() {
		mDoDebugRender = !mDoDebugRender;
	}

	public void toggleFullscreen() {
		mFullScreenEnabled = !mFullScreenEnabled;
		if (mFullScreenEnabled) {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			mOldHeight = Gdx.graphics.getHeight();
			mOldWidth = Gdx.graphics.getWidth();
			Gdx.graphics.setDisplayMode((int) screenSize.getWidth(), (int) screenSize.getHeight(),
					mFullScreenEnabled);
		} else {
			Gdx.graphics.setDisplayMode(mOldWidth, mOldHeight, mFullScreenEnabled);
		}
	}

	/**
	 * Sets whether debug renderer should be used
	 */
	/*
	 * public void setFramerateRender(boolean val) { mDoFramerateRender = val; }
	 */

	/**
	 * Attempts to clip the camera position to some max and min bounds
	 */
	public void clipCam(float minX, float maxX, float minY, float maxY) {
		// True if constraints violated
		boolean minXConstraint, maxXConstraint, minYConstraint, maxYConstraint, bothX, bothY;
		float width = mCamera.viewportWidth;
		float height = mCamera.viewportHeight;

		// Extents of view
		float left = mCamera.position.x - width / 2;
		float right = mCamera.position.x + width / 2;
		float top = mCamera.position.y - height / 2;
		float bot = mCamera.position.y + height / 2;

		// Calculate whether constraints violated
		minXConstraint = (left < minX);
		minYConstraint = (top < minY);
		maxXConstraint = (right > maxX);
		maxYConstraint = (bot > maxY);
		bothX = (maxX - minX) <= width;
		bothY = (maxY - minY) <= height;

		if (bothX) { // Center between min and max constraints
			mCamera.position.x = (minX + maxX) / 2;
		} else if (minXConstraint) { // Fix min constraint
			mCamera.position.x = minX + width / 2;
		} else if (maxXConstraint) { // Fix max constraint
			mCamera.position.x = maxX - width / 2;
		}

		if (bothY) { // Center between min and max constraints
			// System.out.println("Both Constraints");
			mCamera.position.y = (minY + maxY) / 2;
		} else if (minYConstraint) { // Fix min constraint
			// System.out.println("Min Constraint");
			mCamera.position.y = minY + height / 2;
		} else if (maxYConstraint) { // Fix max constraint
			// System.out.println("Max Constraint");
			mCamera.position.y = maxY - height / 2;
		}
	}

	/**
	 * Calculates needed camera scale
	 */
	public void calcScale(float maxwidth, float maxheight) {
		float extentX = -Float.MAX_VALUE;
		float extentY = -Float.MAX_VALUE;
		extentX = Math.max(extentX, Math.abs(mCamera.position.x - mMinX) + BORDER);
		extentX = Math.max(extentX, Math.abs(mCamera.position.x - mMaxX) + BORDER);
		extentX = Math.max(extentX, MIN_WIDTH / 2);
		extentX = Math.min(extentX, maxwidth / 2);
		extentY = Math.max(extentY, Math.abs(mCamera.position.y - mMinY) + BORDER);
		extentY = Math.max(extentY, Math.abs(mCamera.position.y - mMaxY) + BORDER);
		extentY = Math.max(extentY, MIN_HEIGHT / 2);
		extentY = Math.min(extentY, maxheight / 2);

		// Calculate new width and height to show that preserves aspect ratio
		mWidth = extentX * 2;
		mHeight = extentY * 2;
	}

	public void zoom(float amount) {
		mWidth *= 1 + amount;
		mHeight *= 1 + amount;
		onResize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		mOldScale = mScale;
		mCamera.update();
	}

	/**
	 * Sets the camera position. If this is called multiple times per frame, the
	 * average will be calculated upon render.
	 * 
	 * @param pos
	 *            The position
	 */
	public void setCameraPosition(Vector2 pos) {
		mMinX = Math.min(mMinX, pos.x);
		mMaxX = Math.max(mMaxX, pos.x);
		mMinY = Math.min(mMinY, pos.y);
		mMaxY = Math.max(mMaxY, pos.y);
		if (Game.get().getState() == Game.EDIT) {
			mCamera.position.x = pos.x;
			mCamera.position.y = pos.y;
			mCamera.update();
		}
	}

	public void resetCamera() {
		mCamera.position.x = 0;
		mCamera.position.y = 0;
		mCamera.update();
		calcScale(16, 9);
		onResize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	/**
	 * Gets the current camera position.
	 * 
	 * @return pos The position
	 */
	public Vector2 getCameraPosition() {
		return new Vector2(mCamera.position.x, mCamera.position.y);
	}

	/**
	 * Unprojects a vector
	 */
	public void unproject(Vector2 vec) {
		Vector3 tmp = new Vector3(vec.x, vec.y, 0);
		mCamera.unproject(tmp);
		vec.x = tmp.x;
		vec.y = tmp.y;
	}

	/**
	 * Projects a vector
	 */
	public void project(Vector2 vec) {
		Vector3 tmp = new Vector3(vec.x, vec.y, 0);
		mCamera.project(tmp);
		vec.x = tmp.x;
		vec.y = tmp.y;
	}

	/**
	 * Gets screen width, in world coords
	 */
	public float getWidth() {
		return mCamera.viewportWidth;
	}

	/**
	 * Gets screen height, in world coords
	 */
	public float getHeight() {
		return mCamera.viewportHeight;
	}
}
