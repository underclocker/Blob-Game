package org.siggd.actor;

import org.siggd.ContactHandler;
import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.StableContact;
import org.siggd.view.BodySprite;
import org.siggd.view.CompositeDrawable;
import org.siggd.view.DebugActorLinkDrawable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import org.siggd.Timer;

public class ExplodeBall extends Actor{
	private String mTex;
	private Timer mExplodeTimer;
	private Fixture mSensorBall;
	float detectRadius = 2.5f;
	int explodeTime;			//milliseconds
	int explosionForce = 75;
	long thisID;
	boolean triggered = false;			//Action has finished
	boolean impact = false;				//Boolean to Explode on Impact
	boolean impactActive = false;		//Boolean that the impact has been triggered
	Vector2 prevVelocity = new Vector2();
	float prevDot = 0;					//previous DotProduct
	boolean firstDone = false;			//First update done?
	int onHitBlobTime = 10;
	
	public ExplodeBall(Level level, long id) {
		super(level, id);
		thisID = id;
		mName = "ExplodeBall";
		mTex = "data/"+Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 64, BodyType.DynamicBody, origin, false);
		((CompositeDrawable)mDrawable).mDrawables.add(new BodySprite(mBody, origin, mTex));

		CircleShape circle = new CircleShape();
		circle.setPosition(new Vector2(0,0));
		circle.setRadius(detectRadius);
		FixtureDef fd = new FixtureDef();
		fd.shape = circle;
		fd.isSensor = true;
		mSensorBall = mBody.createFixture(fd);
		explodeTime = 180;
		
		mExplodeTimer = new Timer();
		mExplodeTimer.setTimer(explodeTime);
		mExplodeTimer.unpause();
		
		setProp("Density", 0.3);
	}
	
	public ExplodeBall(Level level, long id, int suppliedTime) {
		super(level, id);
		thisID = id;
		mName = "ExplodeBall";
		mTex = "data/"+Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 64, BodyType.DynamicBody, origin, false);
		((CompositeDrawable)mDrawable).mDrawables.add(new BodySprite(mBody, origin, mTex));

		CircleShape circle = new CircleShape();
		circle.setPosition(new Vector2(0,0));
		circle.setRadius(detectRadius);
		FixtureDef fd = new FixtureDef();
		fd.shape = circle;
		fd.isSensor = true;
		mSensorBall = mBody.createFixture(fd);
		explodeTime = suppliedTime;
		
		if(explodeTime == -1) {
			impact = true;
			explodeTime = 0;				//How long t'ill it explodes after impact
		}
		mExplodeTimer = new Timer();
		mExplodeTimer.setTimer(explodeTime);
		mExplodeTimer.unpause();
		
		setProp("Density", 0.3);
	}

	@Override
	public void loadResources() {
		AssetManager man = Game.get().getAssetManager();
		man.load(mTex, Texture.class);		
	}
	
	public void update() {
		if(impact && !triggered) {
			if(firstDone) {
				if(!impactActive) {
					Vector2 currentVel = new Vector2(Convert.getFloat(getProp("Velocity X")), Convert.getFloat(getProp("Velocity Y")));
					float dot = currentVel.dot(prevVelocity);
					if(prevDot == 0) {
						prevDot = dot;
					}
					else {
						int prevDotScaled = (int)(prevDot/(-(Math.abs(prevDot))));
						int dotScaled = (int)(dot/(-(Math.abs(dot))));
						if(dotScaled != prevDotScaled){
							impactActive = true;
							Iterable<StableContact> contacts = Game.get().getLevel().getContactHandler().getContacts(this);
							Iterable<Actor> actors = ContactHandler.getActors(contacts);
							for(Actor a : actors) {
								if(!(a instanceof Blob)) {
									continue;
								}
								Blob blob = (Blob)a;
								if(blob.isSolid()) {
									break;
								}
								Iterable<Body> bodies = blob.mSubBodies;
								for(Body b : bodies) {
									Vector2 distCheck = new Vector2(b.getPosition());
									distCheck.sub(mBody.getPosition());
									float distCheckLen = distCheck.len();
									if(Math.abs(distCheckLen)<0.8) {
										mExplodeTimer.setTimer(onHitBlobTime);
										break;
									}
								}
								if(mExplodeTimer.mCurTime == onHitBlobTime) {
									break;
								}
							}
						}
					}
				}
				else {
					mExplodeTimer.update();
					if(mExplodeTimer.isTriggered() && !triggered) {
						theAction();
						triggered = true;
					}
				}
			}
			else {
				prevVelocity = new Vector2(Convert.getFloat(getProp("Velocity X")), Convert.getFloat(getProp("Velocity Y")));
				firstDone = true;
			}
		}
		else {
			mExplodeTimer.update();
			if(triggered){
				setProp("X", -400);
				setProp("Y", -400);
				this.setActive(false);
				//Game.get().getLevel().removeActor(thisID);
			}
			if(mExplodeTimer.isTriggered() && !triggered){
				theAction();
				triggered = true;
			}
		}
	}
	
	public void theAction() {	//Explosion, Implosion, Whateves. Decided I should make functions so the code doesn't looks so much like a messy fuckfest.
		Iterable<StableContact> contacts = Game.get().getLevel().getContactHandler().getContacts(this);
		Iterable<Actor> actors = ContactHandler.getActors(contacts);
		for(Actor a : actors) {
			Vector2 distCheck = new Vector2(a.getX(), a.getY());
			distCheck.sub(mBody.getPosition());
			float distCheckLen = distCheck.len();
			
			if(distCheckLen <= detectRadius) {
				if(a instanceof Blob){
					Blob blob = (Blob)a;
					Iterable<Body> blobSubs = blob.mSubBodies;
					Vector2 force = new Vector2(explosionForce/blob.getNumSubBodies()*3, 0);
					
					Vector2 blobPos = new Vector2(blob.getX(),blob.getY());
					Vector2 blobCpy = blobPos.cpy();
					blobCpy.sub(mBody.getPosition());
					blobCpy.nor();
					int desiredAngle;
					if(blobPos.y > mBody.getPosition().y) {
						desiredAngle = (int)(Math.acos(Convert.getDouble(blobCpy.x))*(180/Math.PI));
					}
					else {
						if(blobPos.x > mBody.getPosition().x) {
							desiredAngle = (int)((Math.asin(Convert.getDouble(blobCpy.y))+2*Math.PI)*(180/Math.PI));
						}
						else {
							desiredAngle = (int)((Math.PI+(Math.abs(Math.asin(Convert.getDouble(blobCpy.y)))))*(180/Math.PI));
						}
					}
					
					force.rotate(desiredAngle);
					for(Body b : blobSubs) {
						b.applyForceToCenter(force);
					}
				}
				else if(a.getMainBody().getType() == BodyType.DynamicBody && !(a instanceof VacBotBoundry) && !(a instanceof Redirector) && !(a instanceof RecyclingCenter)) {
					Vector2 force = new Vector2(explosionForce, 0);
					Vector2 actorPos = new Vector2(a.getX(),a.getY());
					Vector2 posCpy = actorPos.cpy();
					posCpy.sub(mBody.getPosition());
					posCpy.nor();
					int desiredAngle;
					if(actorPos.y > mBody.getPosition().y) {
						desiredAngle = (int)(Math.acos(Convert.getDouble(posCpy.x))*(180/Math.PI));
					}
					else {
						desiredAngle = -(int)(Math.acos(Convert.getDouble(posCpy.x))*(180/Math.PI));
						desiredAngle+=360;
					}
					force.rotate((float)desiredAngle);
					a.getMainBody().applyForceToCenter(force);
				}
				else {
					continue;
				}
			}
		}
	}
	@Override
	public void loadBodies() {
		// TODO Auto-generated method stub
	}
	@Override
	public void postLoad() {
		// TODO Auto-generated method stub
	}
}