package org.siggd.actor;

import org.siggd.ContactHandler;
import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.StableContact;
import org.siggd.actor.meta.IObservable;
import org.siggd.actor.meta.IObserver;
import org.siggd.view.Animation;
import org.siggd.view.BodySprite;
import org.siggd.view.CompositeDrawable;
import org.siggd.view.DebugActorLinkDrawable;
import org.siggd.view.Drawable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

/**
 * This class can be used as a convenient shortcut for any of the bodies in
 * bodies.json. Just set the Body property to the appropriate body, and the
 * actor will become that body.
 * 
 * @author mysterymath
 * 
 */
public class Wind extends Actor implements IObserver, IObservable{
	private String mTex;
	private float mWaveyness = 100f;
	private float mInverseWaveyness = 1f / mWaveyness;
	private Animation mAnimation;
	private Drawable frame;
	private int mInputDelay = 2;
	private int mDelay = 0;
	private boolean mState = false; 

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
	public Wind(Level level, long id) {
		super(level, id);
		mName = "Wind";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 128, BodyType.StaticBody, origin, true);
		mDrawable.mDrawables.add(new DebugActorLinkDrawable(this, null, null, Color.RED, Color.GREEN));
		frame = new BodySprite(mBody, origin, mTex);
		((CompositeDrawable) mDrawable).mDrawables.add(frame);
		setProp("Wind Strength", (Float) 0.5f);
		mAnimation = new Animation(mBody, origin);
		mAnimation.mTicksPerFrame = 3;
		mAnimation.addFrame("data/gfx/wind1.png");
		mAnimation.addFrame("data/gfx/wind2.png");
		mAnimation.addFrame("data/gfx/wind3.png");
		mAnimation.addFrame("data/gfx/wind4.png");
		mAnimation.addFrame("data/gfx/wind5.png");
		mAnimation.addFrame("data/gfx/wind6.png");
		mAnimation.addFrame("data/gfx/wind7.png");
		mAnimation.setCurFrame(((Double) (Math.random() * 7)).intValue());
	}

	/**
	 * Load resources needed by the actor
	 */
	@Override
	public void loadResources() {
		AssetManager man = Game.get().getAssetManager();
		man.load(mTex, Texture.class);
		man.load("data/gfx/wind1.png", Texture.class);
		man.load("data/gfx/wind2.png", Texture.class);
		man.load("data/gfx/wind3.png", Texture.class);
		man.load("data/gfx/wind4.png", Texture.class);
		man.load("data/gfx/wind5.png", Texture.class);
		man.load("data/gfx/wind6.png", Texture.class);
		man.load("data/gfx/wind7.png", Texture.class);
	}

	/**
	 * Load bodies needed by the actor
	 */
	@Override
	public void loadBodies() {
	}

	@Override
	public void update() {
		if(inputActive() || Convert.getInt(getProp("Target Input")) == -1) {
			
			if (mDelay < mInputDelay){
				mDelay++;
			} else {
				setState(true);
			}
			setProp("Visible", 1);
			mAnimation.update();
			((CompositeDrawable) mDrawable).mDrawables.remove(frame);
			frame = mAnimation.getCurFrame();
			((CompositeDrawable) mDrawable).mDrawables.add(frame);
			Iterable<StableContact> contacts = Game.get().getLevel().getContactHandler()
					.getContacts(this);
			Iterable<Body> bodies = ContactHandler.getBodies(contacts);
			Vector2 force = new Vector2(0, Convert.getFloat(getProp("Wind Strength")))
					.rotate((float) (Convert.getFloat(getProp("Angle"))));
			force.mul(.2f);
			Vector2 forceHat = new Vector2(force);
			forceHat.nor();
			Vector2 tempforce;
			Actor actor;
			int i;
			int numSubBodies;
			Body subBody;
			float density;
			for (Body b : bodies) {
				if (!(b.getUserData() instanceof Platform || b.getUserData() instanceof Redirector || b
						.getFixtureList().get(0).isSensor())) {
					actor = (Actor) b.getUserData();
					density = actor.getProp("Density");
					numSubBodies = actor.getNumSubBodies();
					float subDiv = 1f / numSubBodies;
					for (i = 0; i < numSubBodies; i++) {
						if (Math.random() < mInverseWaveyness) {
							subBody = actor.getSubBody(i);
							tempforce = new Vector2(force);
							tempforce.mul(subBody.getMass() / density);
							tempforce.mul(subDiv * (float) Math.random() * mWaveyness * 4f);
							subBody.applyForceToCenter(tempforce);
						}
					}
	
					tempforce = new Vector2(force);
					Vector2 posDiff = new Vector2(b.getPosition());
					posDiff.sub(this.mBody.getPosition());
					float a1 = posDiff.dot(forceHat);
					Vector2 a1vec = new Vector2(forceHat);
					a1vec.mul(a1);
					posDiff.sub(a1vec);
					tempforce.add(posDiff);
					if (posDiff.len() > .25f) {
						tempforce.mul((Math.max(1f - posDiff.len(), 0f)));
					}
					tempforce.mul(b.getMass() / density);
	
					b.applyForceToCenter(tempforce);
				}
			}
		}
		else {
			if (mDelay > 0){
				mDelay--;
			} else {
				setState(false);
			}
			setProp("Visible", 0);
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

	public boolean inputActive(){
		if(mInputSrc == null) {
			return false;
		}

		Object input = mInputSrc.observe();
		return (input instanceof Boolean) && (Boolean)input;
	}
	private IObservable mInputSrc;
	@Override
	public Actor inputSrc() {
		return (Actor)mInputSrc;
	}

	@Override
	public void inputSrc(Actor inputSrc) {
		mInputSrc =(inputSrc instanceof IObservable) ? (IObservable)inputSrc : null;
	}
	
	
	@Override
	public Object observe() {
		return getState();
	}

	public boolean getState() {
		return mState;
	}

	private void setState(boolean state) {
		mState = state;
	}

}
