package org.siggd.actor;

import org.siggd.ContactHandler;
import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.StableContact;
import org.siggd.Timer;
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
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.RayCastCallback;

public class Cannon extends Actor implements RayCastCallback {

	private class HitScanDrawable implements Drawable {
		@Override
		public void drawSprite(SpriteBatch batch) {
			// no sprite to draw
		}

		@Override
		public void drawElse(ShapeRenderer shapeRender) {
			// draw laser!
			if(targetAcquired) {
				shapeRender.begin(ShapeType.Line);
				shapeRender.setColor(1, 0, 0, 1);
				shapeRender.line(startOfLaser.x, startOfLaser.y, mLaserEnd.x, mLaserEnd.y);
				shapeRender.end();
			}
			
		}

		@Override
		public void drawDebug(Camera camera) {
			// use?
		}
	}

	private String mTex;
	private Timer mSpawnTimer;
	private Fixture mSensorBall;
	float detectRadius = 6f;
	Blob rememberBlob;
	int initialAngle;
	int shotsFired = 0;
	boolean immediateRefire = false;
	long previousID;
	boolean targetAcquired = false;
	//private ShapeRenderer mShapeRenderer;
	private Vector2 mLaserEnd = new Vector2();
	private final float mLaserLength = 1000;
	Vector2 startOfLaser = new Vector2();
	boolean checkingLOS = false;

	public Cannon(Level level, long id) {
		super(level, id);
		mName = "lightbulb";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 128, BodyType.KinematicBody, origin, true);
		((CompositeDrawable) mDrawable).mDrawables.add(new BodySprite(mBody, origin, mTex));

		CircleShape circle = new CircleShape();
		circle.setPosition(new Vector2(0, 0));
		circle.setRadius(detectRadius);
		FixtureDef fd = new FixtureDef();
		fd.shape = circle;
		fd.isSensor = true;
		mSensorBall = mBody.createFixture(fd);

		mSpawnTimer = new Timer();
		mSpawnTimer.setTimer(Convert.getInt(this.getProp("Rate")));
		mSpawnTimer.unpause();

		this.setProp("Rate", 120);
		this.setProp("Exit Velocity", 25);
		this.setProp("Layer", 4);
		// 0 - Explode Ball 1 - Implode Ball
		this.setProp("Ammo", 0);
		this.setProp("explodeTime", 180);
		this.setProp("Alternate", 0);

		((CompositeDrawable) mDrawable).mDrawables.add(new BodySprite(mBody, origin, mTex));
		((CompositeDrawable) mDrawable).mDrawables.add(new HitScanDrawable());
	}

	@Override
	public void loadResources() {
		AssetManager man = Game.get().getAssetManager();
		man.load(mTex, Texture.class);
	}

	public void update() {
		// Hitscan Stuff
		float daAngle = (Convert.getDegrees(mBody.getAngle()) - 90);
		if (daAngle < 0)
			daAngle += 360;
		if (daAngle > 360)
			daAngle -= 360;

		Vector2 finalStart = new Vector2(mBody.getPosition().cpy().x + 0.585f, mBody.getPosition()
				.cpy().y);
		finalStart.sub(mBody.getPosition().cpy());
		finalStart.rotate(Convert.getDegrees(mBody.getAngle()));
		Vector2 start = new Vector2(mBody.getPosition().cpy().x, mBody.getPosition().cpy().y);
		start.add(finalStart);
		startOfLaser = start;

		Vector2 end = new Vector2(0, mLaserLength).rotate(daAngle);
		mLaserEnd = end.add(start);
		setProp("Actor Hit", -1);

		mLevel.getWorld().rayCast(this, start, mLaserEnd.cpy());

		Iterable<StableContact> contacts = Game.get().getLevel().getContactHandler()
				.getContacts(this);
		Iterable<Actor> actors = ContactHandler.getActors(contacts);
		boolean enemySeen = false;
		super.update();

		for (Actor a : actors) {
			if (a instanceof Blob) {
				checkingLOS = true;
				Blob seeCheck = (Blob) a;
				Vector2 seeCheckPos = new Vector2(seeCheck.getX(), seeCheck.getY());
				mLevel.getWorld().rayCast(this, mBody.getPosition(), seeCheckPos);
				checkingLOS = false;
				if(!targetAcquired) {
					continue;
				}				
				if (rememberBlob != null) {
					Vector2 rememberDist = new Vector2( rememberBlob.getX(), rememberBlob.getY() );
					rememberDist.sub(mBody.getPosition());
					float remDist = Math.abs(rememberDist.len());
					if (remDist > detectRadius) {
						rememberBlob = null;
					}
				}
				if (rememberBlob == null) {
					rememberBlob = (Blob) a;
				}
				
				enemySeen = true;
				Blob blob = rememberBlob;
				Vector2 blobPos = new Vector2(blob.getX(), blob.getY());
				Vector2 blobCpy = blobPos.cpy();
				blobCpy.sub(mBody.getPosition());
				float blobDistance = blobCpy.len();
				blobCpy.nor();
				int desiredAngle;
				int currentAngle = Convert.getInt(getProp("Angle"));

				if (Convert.getFloat(blob.getProp("Velocity X")) > 0) {
					desiredAngle = 2;
				} else {
					desiredAngle = -2;
				}

				if (blobPos.y > mBody.getPosition().y) {
					desiredAngle += (int) (Math.acos(Convert.getDouble(blobCpy.x)) * (180 / Math.PI));
				} else {
					if (blobPos.x > mBody.getPosition().x) {
						desiredAngle += (int) ((Math.asin(Convert.getDouble(blobCpy.y)) + 2 * Math.PI) * (180 / Math.PI));
					} else {
						desiredAngle += (int) ((Math.PI + (Math.abs(Math.asin(Convert
								.getDouble(blobCpy.y))))) * (180 / Math.PI));
					}
				}

				int totalRotation = desiredAngle - currentAngle;
				int swapper = 1;
				if (totalRotation > 180 || totalRotation < -180) {
					swapper = -1;
				}
				float rotSpeed = 1.5f;

				// System.out.println("Current: " + currentAngle);
				// System.out.println("	Desired: " + desiredAngle);

				float spawnCheckTotal = desiredAngle - currentAngle;
				if (spawnCheckTotal > 360)
					spawnCheckTotal -= 360;
				if (spawnCheckTotal < -360)
					spawnCheckTotal += 360;
				// 3 is because of the aiming offset
				if ((Math.abs(spawnCheckTotal) < 3 || Math.abs(spawnCheckTotal) > 357)
						&& targetAcquired) {
					if (immediateRefire && shotsFired >= 1) {
						if (Game.get().getLevel().getActorById(previousID) != null) {
							if (!Game.get().getLevel().getActorById(previousID).isActive()) {
								spawnActor();
								shotsFired++;
								mSpawnTimer.reset();
							}
						}
					} else {
						mSpawnTimer.update();
						if (mSpawnTimer.isTriggered() || shotsFired == 0) {
							spawnActor();
							shotsFired++;
							mSpawnTimer.reset();
						}
					}
				}

				if (currentAngle == desiredAngle) {
					mBody.setAngularVelocity(0f);
				} else if (desiredAngle > currentAngle) {
					mBody.setAngularVelocity(swapper * (rotSpeed));
				} else if (desiredAngle < currentAngle) {
					mBody.setAngularVelocity(swapper * (-rotSpeed));
				}
			}
		}
		if (!enemySeen) {
			shotsFired = 0;
			int currentAngle = Convert.getInt(getProp("Angle"));
			int desiredAngle = initialAngle;

			float totalRotation = desiredAngle - currentAngle;
			int swapper = 1;
			if (totalRotation > 180f || totalRotation < -180f) {
				swapper = -1;
			}
			float rotSpeed = 1.5f;

			if (totalRotation > 360) {
				totalRotation -= 360;
			}
			if (totalRotation < -360) {
				totalRotation += 360;
			}
			if (Math.abs(totalRotation) < 30 || Math.abs(totalRotation) > 330) {
				rotSpeed = 0.5f;
			}

			if (currentAngle == desiredAngle) {
				mBody.setAngularVelocity(0f);
			} else if (desiredAngle > currentAngle) {
				mBody.setAngularVelocity(swapper * (rotSpeed));
			} else if (desiredAngle < currentAngle) {
				mBody.setAngularVelocity(swapper * (-rotSpeed));
			}
		}
	}

	private void spawnActor() {
		Level thisHereLevel = Game.get().getLevel();
		long nextID = thisHereLevel.getId();
		previousID = nextID;
		Actor toSpawn;
		if (Convert.getInt(getProp("Alternate")) == 1) {
			if ((((shotsFired + 1) % 2) == 0 && Convert.getInt(getProp("Ammo")) == 0)
					|| (((shotsFired + 1) % 2) == 1 && Convert.getInt(getProp("Ammo")) == 1)) {
				toSpawn = new ImplodeBall(thisHereLevel, nextID,
						Convert.getInt(getProp("explodeTime")));
			} else {
				toSpawn = new ExplodeBall(thisHereLevel, nextID,
						Convert.getInt(getProp("explodeTime")));
			}
		} else {
			if (Convert.getInt(getProp("Ammo")) == 1) {
				toSpawn = new ImplodeBall(thisHereLevel, nextID,
						Convert.getInt(getProp("explodeTime")));
			} else {
				toSpawn = new ExplodeBall(thisHereLevel, nextID,
						Convert.getInt(getProp("explodeTime")));
			}
		}

		Vector2 pos = mBody.getPosition();
		toSpawn.setX(pos.x);
		toSpawn.setY(pos.y);
		toSpawn.setProp("Angle", mBody.getAngle());
		Game.get().getLevel().addActor(toSpawn);

		Vector2 vel = new Vector2(Convert.getInt(getProp("Exit Velocity")), 0);
		vel.rotate(Convert.getDegrees(mBody.getAngle()));
		toSpawn.setProp("Velocity X", vel.x);
		toSpawn.setProp("Velocity Y", vel.y);
	}

	public void setProp(String name, Object val) {
		if (name.equals("Rate")) {
			if (Convert.getInt(val) == -1) {
				// it wont even use this timer, kindof, I THINK ESNARF!
				mSpawnTimer.setTimer(5000);
				immediateRefire = true;
			} else {
				immediateRefire = false;
				mSpawnTimer.setTimer(Convert.getInt(val));
			}
		}
		if (name.equals("Angle")) {
			initialAngle = Convert.getInt(val);
		}
		super.setProp(name, val);
	}

	@Override
	public void loadBodies() {
	}

	@Override
	public void postLoad() {
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
				targetAcquired = true;
			} else {
				targetAcquired = false;
			}
			setProp("Actor Hit", intersect.getId());
			if(!checkingLOS) {
				mLaserEnd = point.cpy();
			}
			// clip at the first actor
			return fraction;
		} else {
			// there was no actor tied to this fixture, ignore it
			return -1;
		}
	}
}