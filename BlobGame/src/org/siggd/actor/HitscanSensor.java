package org.siggd.actor;

import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.actor.meta.IObservable;
import org.siggd.view.BodySprite;
import org.siggd.view.CompositeDrawable;
import org.siggd.view.Drawable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;

public class HitscanSensor extends Actor implements RayCastCallback, IObservable{

	private class HitScanDrawable implements Drawable {

		@Override
		public void drawSprite(SpriteBatch batch) {
			// no sprite to draw
		}

		@Override
		public void drawElse(ShapeRenderer shapeRender) {
			// draw laser!
			if (mLaserEnd == null)
				return;
			else {
				shapeRender.begin(ShapeType.Line);
				if(mState == true) {
					shapeRender.setColor(0, 1, 0, 1);
				}
				else {
					shapeRender.setColor(1, 0, 0, 1);
				}
				shapeRender.line(getX(), getY(), mLaserEnd.x, mLaserEnd.y);
				shapeRender.end();
			}
		}

		@Override
		public void drawDebug(Camera camera) {
			// use?
		}

	}

	private String mTex;
	private Vector2 mLaserEnd;
	private final float mLaserLength = 1000;

	public HitscanSensor(Level level, long id) {
		super(level, id);
		mName = "Hitscan Sensor";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 64, BodyType.StaticBody, origin, false);
		setProp("Output", 0);
		setState(false);
		setProp("Actor Hit", -1);
		setProp("Detect Non-Blob", 0);
		mLaserEnd = null;
		((CompositeDrawable) mDrawable).mDrawables.add(new BodySprite(mBody, origin, mTex));
		((CompositeDrawable) mDrawable).mDrawables.add(new HitScanDrawable());
	}

	@Override
	public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
		Actor intersect = (Actor) fixture.getBody().getUserData();
		if (intersect != null) {
			// ignore things like wind/redirectors
			if (intersect.mBody.getFixtureList().get(0).isSensor()
					|| intersect instanceof VacuumBot || intersect instanceof ExplodeBall
					|| intersect instanceof ImplodeBall) {
				return -1;
			}
			if (intersect instanceof Blob) {
				setProp("Output", 1);
				setState(true);
			} else if (Convert.getInt(getProp("Detect Non-Blob")) == 1
					&& intersect.mBody.getType() != BodyType.StaticBody) {
				setProp("Output", 1);
				setState(true);
			} else {
				setProp("Output", 0);
				setState(false);
			}
			setProp("Actor Hit", intersect.getId());
			mLaserEnd = point.cpy();
			// clip at the first actor
			return fraction;
		} else {
			// there was no actor tied to this fixture, ignore it
			return -1;
		}
	}

	@Override
	public void update() {
		Vector2 start = mBody.getPosition().cpy();
		Vector2 end = new Vector2(0, mLaserLength).rotate(Convert.getDegrees(mBody.getAngle()));
		mLaserEnd = end.add(start);
		setProp("Actor Hit", -1);
		setProp("Output", 0);
		// changes could be made to start and end, thats why a copy is passed
		mLevel.getWorld().rayCast(this, start, mLaserEnd.cpy());
	}

	@Override
	public void loadResources() {
		AssetManager man = Game.get().getAssetManager();
		man.load(mTex, Texture.class);
	}

	@Override
	public void loadBodies() {
	}

	@Override
	public void postLoad() {
	}

	@Override
	public Object observe() {
		return getState();
	}
	public boolean getState() {
		return mState;
	}
	private void setState(boolean state) {
		if (state == mState)
			return;
		mState = state;
	}

	
	// This value MUST only be modified through
	// setState, or the lights won't work.
	private boolean mState = false; 
}
