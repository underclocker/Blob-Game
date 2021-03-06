package org.siggd.actor;

import org.box2dLight.PointLight;
import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.Knocked;
import org.siggd.Level;
import org.siggd.actor.meta.Prop;
import org.siggd.view.BodySprite;
import org.siggd.view.CompositeDrawable;
import org.siggd.view.LevelView;

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
public class Dot extends Actor implements Knocked {
	private static String HOLLOW_GFX = "HollowDot.png";
	private static String STANDARD_GFX = "Dot.png";
	private final Vector2 mOrigin;
	private String mTex;
	public Blob mTargetBlob;
	private int mEatTimer = 10;
	private PointLight mPointLight;
	private int mHollowFlag;
	public static boolean ATE_DOT = false;
	public static boolean SLURP_DOT = false;
	public static int ONTIME_EAT = 0;

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
	public Dot(Level level, long id) {
		super(level, id);
		mName = "Dot";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		mOrigin = new Vector2();
		mBody = makeBody(mName, 32, BodyType.DynamicBody, mOrigin, false);
		((CompositeDrawable) mDrawable).mDrawables.add(new BodySprite(mBody, mOrigin, mTex));

		setProp("Density", (Float) .3f);
		setProp("Friction", (Float) .1f);

		mBody.setGravityScale(0);
		mBody.setLinearDamping(.5f);
		mBody.setAngularDamping(.9f);

		if (LevelView.mUseLights) {
			mPointLight = new PointLight(Game.get().getLevelView().getRayHandler(), 32);
			mPointLight.setColor(.1f, .1f, .1f, .9f);
			mPointLight.setDistance(1f);
			mPointLight.setXray(true);
			mPointLight.attachToBody(mBody, 0, 0);
			mPointLight.setActive(false);
		}
		setProp("Active", 1);
		setProp("Layer", 3);
		// TODO: set light active based on if actor is dummy or not
		setProp("X", -10000);

	}

	/**
	 * Load resources needed by the actor
	 */
	@Override
	public void loadResources() {
		AssetManager man = Game.get().getAssetManager();
		man.load(mTex, Texture.class);
		man.load("data/gfx/" + HOLLOW_GFX, Texture.class);
	}

	/**
	 * Load bodies needed by the actor
	 */
	@Override
	public void loadBodies() {
	}

	public void knocked(Actor a) {
		if (mTargetBlob != null)
			return;
		if (a instanceof Blob) {
			mTargetBlob = (Blob) a;
		} else {
			if (a instanceof Dot && ((Dot) a).mTargetBlob != null) {
				mTargetBlob = ((Dot) a).mTargetBlob;
			}
			mBody.setGravityScale(1);
		}
	}

	@Override
	public void update() {
		if (mBody.getLinearVelocity().len2() > .01f) {
			mBody.setGravityScale(1);
		}
		if (mTargetBlob != null) {
			Vector2 offset = new Vector2(mTargetBlob.getX(), mTargetBlob.getY());
			offset.sub(mBody.getPosition());
			float delay = (10 - mEatTimer) / 20f;
			offset.scl(4.5f + delay);
			offset.scl(1.5f);
			offset.scl(Level.PHYSICS_SCALE);
			mBody.applyForceToCenter(offset, true);
			mTargetBlob.applyForce(offset.scl(-1));
			mEatTimer--;
			Level l = Game.get().getLevel();
			if (mEatTimer < 0 && (l.musicTime() == 13 || (l.musicTime() == 5 && ONTIME_EAT < 36))
					&& !SLURP_DOT) {
				if (l.musicTime() == 13) {
					ONTIME_EAT = 0;
				}
				SLURP_DOT = true;
				// mBody.getFixtureList().get(0).setFilterData(mTargetBlob.getEyeFilter());
				mBody.getFixtureList().get(0).setSensor(true);
			}
			if (mEatTimer <= -5 && (l.musicTick() || (l.musicOffTick() && ONTIME_EAT < 36))
					&& !ATE_DOT) {
				ATE_DOT = true;
				setProp("Active", 0);
				mTargetBlob.eatDot();
			}
		}
	}

	@Override
	public void setProp(String name, Object val) {
		if (name.equals("Active")) {
			if (mPointLight != null)
				mPointLight.setActive(Convert.getInt(val) != 0);
			setActive(Convert.getInt(val) != 0);
		}
		super.setProp(name, val);
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

	@Prop(name = "Hollow")
	public int getHollow() {
		return mHollowFlag;
	}

	@Prop(name = "Hollow")
	/**
	 * @param flag 1 or 0
	 */
	public void setHollow(int flag) {
		mHollowFlag = flag;
		((CompositeDrawable) mDrawable).mDrawables.clear();
		if (flag == 1) {
			((CompositeDrawable) mDrawable).mDrawables.add(new BodySprite(mBody, mOrigin,
					"data/gfx/" + HOLLOW_GFX));
			if (LevelView.mUseLights)
				mPointLight.setDistance(.75f);
		} else {
			((CompositeDrawable) mDrawable).mDrawables.add(new BodySprite(mBody, mOrigin,
					"data/gfx/" + STANDARD_GFX));
		}
	}

	@Override
	public void postLoad() {
	}
}
