package org.siggd.actor;

import java.util.ArrayList;

import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.Player;
import org.siggd.actor.meta.IObservable;
import org.siggd.view.BodySprite;
import org.siggd.view.CompositeDrawable;
import org.siggd.view.Drawable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;

public class BlobLaser extends Actor implements RayCastCallback, IObservable {

	private class HitScanDrawable implements Drawable {

		@Override
		public void drawSprite(SpriteBatch batch) {
			// no sprite to draw
		}

		@Override
		public void drawElse(ShapeRenderer shapeRender) {
			// draw laser!
			if (mLaserEnd == null || mLaserStart == null)
				return;
			else {
				float angle = Convert.getDegrees(mBody.getAngle());
				ArrayList<Player> players = Game.get().activePlayers();
				shapeRender.begin(ShapeType.Line);
				Vector2 start, end;
				Player p;
				for (int i = 0; i < mLaserStarts.size(); i++) {
					p = players.get(i);
					if (mDetectedBlobs.contains((Blob)p.mActor)) {
						shapeRender.setColor(Color.GRAY);
					} else {
						shapeRender.setColor(Blob.COLORS[p.id]);
					}
					start = mLaserStarts.get(i);
					end = mLaserEnds.get(i);
					shapeRender.line(start.x, start.y, end.x, end.y);
				}
				shapeRender.end();
				shapeRender.begin(ShapeType.Filled);
				Vector2 offset = new Vector2(0, -.11f).rotate(angle);
				for (int i = 0; i < mLaserStarts.size(); i++) {
					p = players.get(i);
					if (mDetectedBlobs.contains((Blob)p.mActor)) {
						shapeRender.setColor(Blob.COLORS[p.id]);
					} else {
						shapeRender.setColor(Color.BLACK);
					}
					start = mLaserStarts.get(i).cpy().add(offset);
					shapeRender.circle(start.x, start.y, .06f, 16);
				}
				shapeRender.end();
			}
		}

		@Override
		public void drawDebug(Camera camera) {
			// use?
		}

	}

	private String mTex;
	private Vector2 mLaserStart;
	private Vector2 mLaserEnd;
	private ArrayList<Vector2> mLaserStarts;
	private ArrayList<Vector2> mLaserEnds;
	private ArrayList<Blob> mDetectedBlobs;
	private Blob mCurrentBlob;
	private final float mLaserLength = 1000;

	public BlobLaser(Level level, long id) {
		super(level, id);
		mName = "bloblaser";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 128, BodyType.StaticBody, origin, false);
		setProp("Output", 0);
		mLaserEnd = null;
		mLaserStarts = new ArrayList<Vector2>(Game.get().getNumberOfPlayers());
		mLaserEnds = new ArrayList<Vector2>(Game.get().getNumberOfPlayers());
		mDetectedBlobs = new ArrayList<Blob>();
		((CompositeDrawable) mDrawable).mDrawables.add(new BodySprite(mBody, origin, mTex));
		((CompositeDrawable) mDrawable).mDrawables.add(new HitScanDrawable());
	}

	@Override
	public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
		Actor intersect = (Actor) fixture.getBody().getUserData();
		if (intersect != null) {
			// ignore things like wind/redirectors
			if (intersect.mBody.getFixtureList().get(0).isSensor()
					|| (fixture.getBody().getType() != BodyType.StaticBody && !(intersect instanceof Blob))) {
				return -1;
			}
			if (intersect instanceof Blob) {
				if (intersect.equals(mCurrentBlob)) {
					mLaserEnd = point.cpy();
					if (!mDetectedBlobs.contains(mCurrentBlob)) {
						mDetectedBlobs.add(mCurrentBlob);
					}
					return fraction;
				} else {
					return -1;
				}
			}
			mLaserEnd = point.cpy();
			// clip at the first actor
			return fraction;
		} else {
			return -1;
		}
	}

	@Override
	public void update() {
		float angle = Convert.getDegrees(mBody.getAngle());
		mLaserStart = mBody.getPosition().cpy();
		mLaserStart.add(new Vector2(-.4f, 0).rotate(angle));
		mLaserStart.add(new Vector2(0, .02f).rotate(angle));
		Vector2 end = new Vector2(0, mLaserLength).rotate(angle);
		mLaserEnd = end.cpy().add(mLaserStart);
		// changes could be made to start and end, thats why a copy is passed
		int playerNum = Game.get().activePlayersNum();
		Vector2 slider = new Vector2(.8f, 0).rotate(angle);
		slider.div(playerNum + 1);
		mLaserEnds.clear();
		mLaserStarts.clear();
		ArrayList<Player> players = Game.get().getPlayers();
		for (int i = 0; i < playerNum; i++) {
			mLaserStart.add(slider);
			mLaserEnd = end.cpy().add(mLaserStart);
			mCurrentBlob = (Blob) players.get(i).mActor;
			mLevel.getWorld().rayCast(this, mLaserStart.cpy(), mLaserEnd.cpy());
			mLaserStarts.add(mLaserStart.cpy());
			mLaserEnds.add(mLaserEnd.cpy());
		}
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
		return mDetectedBlobs.size() == Game.get().activePlayersNum();
	}

	public boolean getState() {
		return mState;
	}

	private void setState(boolean state) {
		if (state == mState)
			return;
		mState = state;
	}

	private boolean mState = false;
}
