package org.siggd.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.siggd.Convert;
import org.siggd.DebugOutput;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.Timer;
import org.siggd.actor.meta.Prop;
import org.siggd.actor.meta.PropScanner;
import org.siggd.view.CompositeDrawable;
import org.siggd.view.DebugActorLinkDrawable;
import org.siggd.view.Drawable;

import pong.client.core.BodyEditorLoader;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Array;

/**
 * This class represents an object in the world.
 * 
 * @author mysterymath
 * 
 */
public abstract class Actor {
	// The world that contains this actor
	protected final Level mLevel;
	// The actor's id in the world
	private final long mId;
	// The way to draw this actor
	final protected CompositeDrawable mDrawable;

	protected Body mBody;
	protected String mName;
	protected ArrayList<Body> mSubBodies;
	protected boolean mActive;
	protected Vector2 mOldVCenter = Vector2.Zero;
	protected Timer mSoundTimer;

	// members pulled out of property map
	private int mLayer;
	private int mCollisionGroup;
	private int mGrabbableInd;
	private int mVisibleInd;
	private int mSpawner;
	private String mCollisionSound;
	private float mCollisionThreshold;
	private float mCollisionPitch;

	public boolean isActive() {
		return mActive;
	}

	public void setActive(boolean active) {
		this.mActive = active;
		mBody.setActive(active);
	}

	/**
	 * The actor's properties These are editable with the editor, and convenient
	 * for communicating with other actors.
	 */
	protected HashMap<String, Object> mProps;

	/**
	 * Getter and setter methods for properties.
	 */
	private PropScanner.Props mPropMethods;

	/**
	 * Constructor. No non-optional parameters may be added to this constructor
	 * in subclasses. This should contain only properties, and code that MUST
	 * run before later init,
	 * 
	 * @param level
	 *            The world that contains this actor
	 * @param id
	 *            The id
	 */
	public Actor(Level level, long id) {
		mLevel = level;
		mId = id;
		mActive = true;
		mSubBodies = new ArrayList<Body>();
		mDrawable = new CompositeDrawable();

		// Setup default actor properties
		mProps = new HashMap<String, Object>();

		// Actor properties:
		mLayer = 1;
		mCollisionGroup = 0;
		mGrabbableInd = 0;
		mVisibleInd = 1;
		mSpawner = -1;
		mCollisionSound = "";
		mCollisionPitch = 1.0f;
		mCollisionThreshold = 3.0f;

		mSoundTimer = new Timer();
		mSoundTimer.setTimer(5);
		mSoundTimer.unpause();

		if (Game.get().getPropScanner() != null) {
			mPropMethods = Game.get().getPropScanner().getProps(getClass());
		}
	}

	/**
	 * Called to perform actor logic
	 */
	public void update() {
		if (!"".equals(mCollisionSound)) {
			Vector2 vel;
			vel = new Vector2(mBody.getLinearVelocity());
			vel.sub(mOldVCenter);
			float velLength = vel.len();
			if (velLength > mCollisionThreshold && mSoundTimer.isTriggered()) {
				AssetManager man = Game.get().getAssetManager();
				Sound sound;
				long soundID;
				String soundstr = "data/sfx/" + mCollisionSound;
				if (man.isLoaded(soundstr)) {
					sound = man.get(soundstr, Sound.class);
					soundID = sound.play();
					sound.setPitch(soundID, mCollisionPitch + velLength * .005f);
					sound.setVolume(soundID, Math.min(.7f, (velLength) * .05f));
					mSoundTimer.reset();
				}
			}
			mSoundTimer.update();
			mOldVCenter = new Vector2(mBody.getLinearVelocity());
		}
	}

	/**
	 * Called to draw the actor. Most actor's won't need this, as LevelView can
	 * draw simple textures. Use this if you need more complicated drawing (and
	 * make sure your texture is null)
	 */
	public void draw() {

	}

	/**
	 * Make the Box2d body
	 * 
	 * @param name
	 *            The name of the body in the JSON body definition file
	 * @param scale
	 *            The width of the image
	 * @param Vector2
	 *            origin [out] Object that will receive the image origin
	 */
	public Body makeBody(String name, float scale, BodyType bdtype, Vector2 origin) {
		return makeBody(name, scale, bdtype, origin, false);
	}

	/**
	 * Make the Box2d body
	 * 
	 * @param name
	 *            The name of the body in the JSON body definition file
	 * @param scale
	 *            The width of the image
	 * @param Vector2
	 *            origin [out] Object that will receive the image origin
	 * @param isSensor
	 *            True if should be a sensor
	 */
	public Body makeBody(String name, float scale, BodyType bdtype, Vector2 origin, boolean isSensor) {

		// Correct scale by camera scale factor
		float correct = Game.get().getLevelView().getVScale();
		scale /= correct;

		// Create a BodyDef
		BodyDef bd = new BodyDef();
		bd.position.set(0, 0);
		bd.type = bdtype;

		// Create a FixtureDef
		FixtureDef fd = new FixtureDef();
		fd.density = .1f;
		fd.friction = 1f;
		fd.restitution = 0.3f;
		fd.isSensor = isSensor;

		// Create a Body
		Body body = mLevel.getWorld().createBody(bd);

		// Create the body fixture automatically by using the loader.
		BodyEditorLoader loader = Game.get().getBodyEditorLoader();
		loader.attachFixture(body, name, fd, scale);

		// Give Body a reference to its Actor
		body.setUserData(this);

		Vector2 tmpOrigin = loader.getOrigin(name, scale);
		origin.x = tmpOrigin.x;
		origin.y = tmpOrigin.y;

		return body;
	}

	/**
	 * Load resources needed by the actor
	 */
	public abstract void loadResources();

	/**
	 * Load body used by the actor
	 */
	public abstract void loadBodies();

	/**
	 * Called after all actors have been loaded
	 */
	public abstract void postLoad();

	/**
	 * Dispose of the actor's resources
	 */
	public void dispose() {

	}

	/**
	 * Destroy Actor Body(s)
	 */
	public void destroy() {
		Game.get().getLevel().getWorld().destroyBody(mBody);
	}

	/**
	 * Determines if the actor contains a particular property
	 * 
	 * @param name
	 *            The name of the property
	 * @return True if the property exists
	 */
	public boolean hasProp(String name) {
		return mProps.containsKey(name);
	}

	// ACCESSORS & MUTATORS:
	/**
	 * Accessor for the Actor's level (as in stage).
	 * 
	 * @return Level the Actor is currently in.
	 */
	public Level getLevel() {
		return mLevel;
	}

	/**
	 * Accessor for the Actor's name.
	 * 
	 * @return Actor's name.
	 */
	public String getName() {
		return mName;
	}

	@Prop(name = "ID")
	/**
	 * Accessor for the Actor's unique ID
	 * 
	 * @return Unique ID of the Actor
	 */
	public long getId() {
		return mId;
	}

	/**
	 * Accessor for the Actor's Box2d body (if any)
	 * 
	 * @return the body, or null if none
	 */
	public Body getMainBody() {
		return mBody;
	}

	public int getNumSubBodies() {
		return mSubBodies.size();
	}

	public Body getSubBody(int index) {
		return mSubBodies.get(index);
	}

	/**
	 * Set a category group to all fixtures of a single body.
	 * 
	 * @param group
	 *            Id - Negative id does not collide with those with the same
	 *            group id Positive id collides with only those with the same
	 *            group id. Default is 0, which collides with everything;
	 */
	@Prop(name = "Collision Group")
	public void setCollisionGroup(int group) {
		// Create a filter
		Filter filter;

		// Set all fixtures of the body to the same Collision Group.
		Array<Fixture> fixtures = mBody.getFixtureList();
		for (Fixture f : fixtures) {
			// Get the Current Filter data
			filter = f.getFilterData();
			// Change the group index
			filter.groupIndex = (short) group;
			// Apply the new filter to the fixture.
			f.setFilterData(filter);
		}
		mCollisionGroup = group;
	}

	@Prop(name = "Collision Group")
	public int getCollisionGroup() {
		return mCollisionGroup;
	}

	/**
	 * Adds all properties present in h, overwrites duplicates but leaves unique
	 * keys already present
	 * 
	 * @param h
	 *            HashMap of properties to add/overwrite
	 */
	public void setProperties(HashMap<String, Object> h) {
		for (Map.Entry<String, Object> entry : h.entrySet()) {
			setProp(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Accessor for getting all the Actor's properties
	 * 
	 * @return HashMap of Actor properties
	 */
	public HashMap<String, Object> getProperties() {
		HashMap<String, Object> ret = new HashMap<String, Object>(mProps);

		for (String s : mPropMethods.getGetterNames()) {
			ret.put(s, mPropMethods.get(this, s));
		}

		return ret;
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
		// First attempt to get property using new system
		if (mPropMethods != null && mPropMethods.hasGetter(name)) {
			return (T) mPropMethods.get(this, name);
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
		// Try to set property using new method
		if (mPropMethods != null && mPropMethods.hasSetter(name)) {
			mPropMethods.set(this, name, val);
			return;
		}
		if (name.equals("ID")) {
			return;
		}
		mProps.put(name, val);
	}

	public String toString() {
		return "Actor ID:" + getProp("ID");
	}

	/**
	 * Accessor for the way to draw the Actor
	 * 
	 * @return The Actor's Drawable
	 */
	public Drawable getDrawable() {
		return mDrawable;
	}

	// ///////////////
	// Properties
	// //////////////

	@Prop(name = "X")
	public float getX() {
		return mBody.getPosition().x;
	}

	@Prop(name = "X")
	public void setX(float x) {
		mBody.setTransform(x, mBody.getPosition().y, mBody.getAngle());
		mBody.setLinearVelocity(0, 0);
	}

	@Prop(name = "Y")
	public float getY() {
		return mBody.getPosition().y;
	}

	@Prop(name = "Y")
	public void setY(float y) {
		mBody.setTransform(mBody.getPosition().x, y, mBody.getAngle());
		mBody.setLinearVelocity(0, 0);
	}

	@Prop(name = "Friction")
	public float getFriction() {
		return mBody.getFixtureList().get(0).getFriction();
	}

	@Prop(name = "Friction")
	public void setFriction(float friction) {
		for (Fixture f : mBody.getFixtureList()) {
			f.setFriction(friction);
		}
	}

	@Prop(name = "Density")
	public float getDensity() {
		return mBody.getFixtureList().get(0).getDensity();
	}

	@Prop(name = "Density")
	public void setDensity(float d) {
		for (Fixture f : mBody.getFixtureList()) {
			if (!f.isSensor()) {
				f.setDensity(Convert.getFloat(d));
			}
		}
		mBody.resetMassData();
	}

	@Prop(name = "Restitution")
	public float getRestitution() {
		return mBody.getFixtureList().get(0).getRestitution();
	}

	@Prop(name = "Restitution")
	public void setRestitution(float r) {
		for (Fixture f : mBody.getFixtureList()) {
			f.setRestitution(Convert.getFloat(r));
		}
	}

	@Prop(name = "Momentum X")
	public float getMomentumX() {
		return mBody.getLinearVelocity().x * mBody.getMass();
	}

	@Prop(name = "Momentum X")
	public void setMomentumX(float m) {
		mBody.setLinearVelocity(m / mBody.getMass(), mBody.getLinearVelocity().y);
	}

	@Prop(name = "Momentum Y")
	public float getMomentumY() {
		return mBody.getLinearVelocity().y * mBody.getMass();
	}

	@Prop(name = "Momentum Y")
	public void setMomentumY(float m) {
		mBody.setLinearVelocity(mBody.getLinearVelocity().x, m / mBody.getMass());
	}

	@Prop(name = "Velocity X")
	public float getVelocityX() {
		return mBody.getLinearVelocity().x;
	}

	@Prop(name = "Velocity X")
	public void setVelocityX(float m) {
		mBody.setLinearVelocity(m, mBody.getLinearVelocity().y);
	}

	@Prop(name = "Velocity Y")
	public float getVelocityY() {
		return mBody.getLinearVelocity().y;
	}

	@Prop(name = "Velocity Y")
	public void setVelocityY(float m) {
		mBody.setLinearVelocity(mBody.getLinearVelocity().x, m);
	}

	@Prop(name = "Angle")
	public float getAngle() {
		return Convert.getDegrees(mBody.getAngle());
	}

	@Prop(name = "Angle")
	public void setAngle(float m) {
		mBody.setTransform(mBody.getPosition().x, mBody.getPosition().y, Convert.getRadians(m));
	}

	@Prop(name = "Layer")
	public int getLayer() {
		return mLayer;
	}

	@Prop(name = "Layer")
	public void setLayer(int layer) {
		mLayer = layer;
	}

	@Prop(name = "BodyType")
	public int getBodyType() {
		return mBody.getType().ordinal();
	}

	@Prop(name = "BodyType")
	/**
	 * StaticBody(0), KinematicBody(1), DynamicBody(2)
	 * @param type integer representation of type
	 */
	public void setBodyType(int type) {
		mBody.setType(BodyType.values()[type]);
	}

	@Prop(name = "Grabbable")
	public int getGrabable() {
		return mGrabbableInd;
	}

	@Prop(name = "Grabbable")
	/**
	 * @param flag 1 or 0
	 */
	public void setGrabbable(int flag) {
		mGrabbableInd = flag;
	}

	@Prop(name = "Visible")
	public int getVisible() {
		return mVisibleInd;
	}

	@Prop(name = "Visible")
	/**
	 * @param flag 1 or 0
	 */
	public void setVisible(int flag) {
		mVisibleInd = flag;
	}

	@Prop(name = "Spawner")
	public int getSpawner() {
		return mSpawner;
	}

	@Prop(name = "Spawner")
	public void setSpawner(int id) {
		int size = mDrawable.mDrawables.size();
		// remove old bodysprite
		for (int i = 0; i < size; i++) {
			Drawable d = mDrawable.mDrawables.get(i);
			if (d instanceof DebugActorLinkDrawable) {
				if (((DebugActorLinkDrawable) d).getPropName() != null
						&& ((DebugActorLinkDrawable) d).getPropName().equals("Spawner")) {
					mDrawable.mDrawables.remove(d);
				}
				break;
			}
		}
		mDrawable.mDrawables.add(new DebugActorLinkDrawable(this, "Spawner", Color.GREEN));
		mSpawner = id;
		Actor spawner = mLevel.getActorById(Convert.getInt(id));
		if (spawner != null && spawner instanceof Spawner) {
			((Spawner) spawner).addToSpawn(this, 0);
		}
	}

	@Prop(name = "Active")
	public int getActive() {
		return mActive ? 1 : 0;
	}

	@Prop(name = "Active")
	public void setActive(int flag) {
		if (flag == 1) {
			setActive(true);
		} else if (flag == 0) {
			setActive(false);
		}
	}

	@Prop(name = "Collision Sound")
	public String getCollisionSound() {
		return mCollisionSound;
	}

	@Prop(name = "Collision Sound")
	public void setCollisionSound(String sound) {
		mCollisionSound = sound;
		if (!"".equals(sound))
			Game.get().getAssetManager().load("data/sfx/" + sound, Sound.class);
	}

	@Prop(name = "Collision Threshold")
	public float getCollisionThreshold() {
		return mCollisionThreshold;
	}

	@Prop(name = "Collision Threshold")
	public void setCollisionThreshold(float p) {
		mCollisionThreshold = p;
	}

	@Prop(name = "Collision Pitch")
	public float getCollisionPitch() {
		return mCollisionPitch;
	}

	@Prop(name = "Collision Pitch")
	public void setCollisionPitch(float p) {
		mCollisionPitch = p;
	}
}
