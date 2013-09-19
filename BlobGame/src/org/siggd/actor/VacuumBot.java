package org.siggd.actor;

import java.util.ArrayList;

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

/**
 * I like too copy and paste
 * 		Yeah I Do too
 * @author wattermann
 */
public class VacuumBot extends Actor {
	private String mTex;
	private Fixture mSensorBall;
	private Vector2 mStartPosition;
	private Fixture mMainFixture;
	int max = Integer.MAX_VALUE;
	VacBotBoundry mNearestBoundry = null;
	float mNearestDistance = Convert.getFloat(max);
	float vacuumStrength = 12f;
		//Distance from bot before it starts aSuckin
	float vactDist = 3.25f;					
//Detect Distance
	float detectRadius = 6f;						
 //0 - who cares  //1 - going to RecyclingCenter //2 - recycling
	int action = 0;
	Blob rememberBlerb;
	/**
	 * Constructor. No non-optional parameters may be added to this constructor.
	 * This should contain only properties, and code that MUST run before later init,
	 * 
	 * @param level The level that contains this actor
	 * @param id The id
	 */
	public VacuumBot(Level level, long id) {
		super(level, id);
		mName = "VacuumBot";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 512, BodyType.KinematicBody, origin, false);
		((CompositeDrawable)mDrawable).mDrawables.add(new BodySprite(mBody, origin, mTex));
		((CompositeDrawable)mDrawable).mDrawables.add(new DebugActorLinkDrawable(this, "Target Recycling Center", Color.RED));
		
		
		mStartPosition = new Vector2();
		
		CircleShape circle = new CircleShape();
		circle.setPosition(new Vector2(0,0));
		circle.setRadius(detectRadius);
		// Create a fixtureDef
		FixtureDef fd = new FixtureDef();
		fd.shape = circle;
		//fd.density = 0f;
		//fd.friction = 0f;
		fd.isSensor = true;
		mSensorBall = mBody.createFixture(fd);		
		
		mBody.setGravityScale(0);
		mBody.setLinearDamping(0.5f);
		mBody.setAngularDamping(0.05f);
		setProp("Speed", 1);
		setProp("Rotation Speed", 2);
		setProp("Friction",0.05f);
		setProp("Target Recycling Center", -1);
	}
	
	/**
	 * Load resources needed by the actor
	 */
	@Override
	public void loadResources() {
		AssetManager man = Game.get().getAssetManager();
		man.load(mTex, Texture.class);
	}
	/**
	 * Load bodies needed by the actor
	 */
	@Override
	public void loadBodies() {
	}
	
	@Override
	public void update(){
		Iterable<StableContact> contacts = Game.get().getLevel().getContactHandler().getContacts(this);
		Iterable<Actor> actors = ContactHandler.getActors(contacts);
		boolean enemySeen = false;
		for (Actor a : actors){
			if(mNearestBoundry == null) {
				mNearestBoundry = nearestBoundry(mNearestDistance);
			}
			else {
				Vector2 boundPos = new Vector2(mNearestBoundry.getX(),mNearestBoundry.getY());
				Vector2 distanceFrom = mBody.getPosition().cpy();
				distanceFrom.sub(boundPos);
				mNearestDistance = distanceFrom.len();
				VacBotBoundry tempBound = nearestBoundry(mNearestDistance);
				if(tempBound == null) {
					if(mStartPosition.x < mBody.getPosition().x) {
						super.update();
						float xDir = Convert.getFloat(getProp("Speed"))*Convert.getFloat(-Math.cos(Convert.getDouble(mNearestBoundry.getProp("Angle"))*Math.PI/180));
						float yDir = Convert.getFloat(getProp("Speed"))*Convert.getFloat(-Math.sin(Convert.getDouble(mNearestBoundry.getProp("Angle"))*Math.PI/180));
						mBody.setLinearVelocity(new Vector2(xDir, yDir));
					}
					else {
						super.update();
						float xDir = Convert.getFloat(getProp("Speed"))*Convert.getFloat(Math.cos(Convert.getDouble(mNearestBoundry.getProp("Angle"))*Math.PI/180));
						float yDir = Convert.getFloat(getProp("Speed"))*Convert.getFloat(Math.sin(Convert.getDouble(mNearestBoundry.getProp("Angle"))*Math.PI/180));
						mBody.setLinearVelocity(new Vector2(xDir, yDir));
					}
					break;
				}
				else {
					mNearestBoundry = tempBound;
					boundPos = new Vector2(mNearestBoundry.getX(),mNearestBoundry.getY());
					distanceFrom = mBody.getPosition().cpy();
					distanceFrom.sub(boundPos);
					mNearestDistance = distanceFrom.len();
				}
			}
			
			if(a instanceof Blob) {
				Blob distCheckBlob = (Blob)a;
				Vector2 distCheck = new Vector2(distCheckBlob.getX(),distCheckBlob.getY());
				distCheck.sub(mBody.getPosition());
				float theDist = Math.abs(distCheck.len());
				if (theDist <= detectRadius) {
					if(rememberBlerb == null) {
						rememberBlerb = (Blob)a;
					}
					Vector2 rememberDist = new Vector2(rememberBlerb.getX(),rememberBlerb.getY());
					rememberDist.sub(mBody.getPosition());
					float remDist = Math.abs(rememberDist.len());
					if(remDist > detectRadius) {
						rememberBlerb = null;
						action = 0;
						continue;
					}
					
					if(rememberBlerb == (Blob)a) {
						if (action == 2) {
							//System.out.println("Action 2!");
							enemySeen = true;
							Blob blob =(Blob)a;
							Vector2 blobPos = new Vector2(blob.getX(),blob.getY());
							int targetId = Convert.getInt(getProp("Target Recycling Center"));
							RecyclingCenter rc = (RecyclingCenter)mLevel.getActorById(targetId);
							Vector2 rcPos = new Vector2(rc.getX(), rc.getY());
							Vector2 rcCpy = rcPos.cpy();
							
							//rotate to the correct angle
							int desiredAngle = Convert.getInt(rc.getProp("Angle"));
							int currentAngle = Convert.getInt(getProp("Angle"));
							if(desiredAngle > 360) {
								desiredAngle-=360;
							}
							
							int totalRotation = desiredAngle-currentAngle;
							int swapper = 1;
							if(totalRotation > 180f || totalRotation < -180f) {
								swapper = -1;
							}
							float rotSpeed = Convert.getFloat(getProp("Rotation Speed"));
							
							if(currentAngle == desiredAngle) {
								mBody.setAngularVelocity(0f);
							}
							else if(desiredAngle > currentAngle) {
								mBody.setAngularVelocity(swapper*(rotSpeed));
							}
							else if(desiredAngle < currentAngle) {
								mBody.setAngularVelocity(swapper*(-rotSpeed));
							}
							
							int angleTo = Convert.getInt(rc.getProp("Angle"));
							if(angleTo > 360) {
								angleTo-=360;
							}
							mBody.setLinearVelocity(new Vector2(0f,0f));
							/*
							if(rcPos.x < mBody.getPosition().x) {
								float xDir = Convert.getFloat(getProp("Speed"))*Convert.getFloat(-Math.cos(Convert.getDouble(mNearestBoundry.getProp("Angle"))*Math.PI/180));
								float yDir = Convert.getFloat(getProp("Speed"))*Convert.getFloat(-Math.sin(Convert.getDouble(mNearestBoundry.getProp("Angle"))*Math.PI/180));
								mBody.setLinearVelocity(new Vector2(xDir, yDir));
							}
							else {
								float xDir = Convert.getFloat(getProp("Speed"))*Convert.getFloat(Math.cos(Convert.getDouble(mNearestBoundry.getProp("Angle"))*Math.PI/180));
								float yDir = Convert.getFloat(getProp("Speed"))*Convert.getFloat(Math.sin(Convert.getDouble(mNearestBoundry.getProp("Angle"))*Math.PI/180));
								mBody.setLinearVelocity(new Vector2(xDir, yDir));
							}
							*/
							
							Iterable<Body> blobSubs = blob.mSubBodies;
							Vector2 force = new Vector2(vacuumStrength/blob.getNumSubBodies(), 0);
							force.rotate((float)angleTo);
							blob.setPulling(true);
							if(blob.isSolid()) {
								blob.transform();
							}			
							for(Body b : blobSubs) {
								b.applyForceToCenter(force);
							}
							/*
							if(mBody.getPosition().x >= rcPos.x-0.5 || mBody.getPosition().x <= rcPos.x+0.5) {
								Iterable<Body> blobSubs = blob.mSubBodies;
								for (Body b : blobSubs){
									if(blobPos.x < mBody.getPosition().x) {
										float xDir = vacuumStrength*Convert.getFloat(Math.cos(Convert.getDouble(angleTo)));
										float yDir = vacuumStrength*Convert.getFloat(Math.sin(Convert.getDouble(angleTo)));
										b.setLinearVelocity(new Vector2(xDir, yDir));
									}
									else {
										float xDir = vacuumStrength*Convert.getFloat(-Math.cos(Convert.getDouble(angleTo)));
										float yDir = vacuumStrength*Convert.getFloat(-Math.sin(Convert.getDouble(angleTo)));;
										b.setLinearVelocity(new Vector2(xDir, yDir));
									}
								}		
							}
							*/
							break;
						}
						
						if(action == 1) {
							//System.out.println("Action 1!");
							enemySeen = true;
							int targetId = Convert.getInt(getProp("Target Recycling Center"));
							RecyclingCenter rc = (RecyclingCenter)mLevel.getActorById(targetId);
							Vector2 rcPos = new Vector2(rc.getX(), rc.getY());
							Vector2 rcCpy = rcPos.cpy();
							rcCpy.sub(mBody.getPosition());
							rcCpy.nor();
							
							//Rotate to fit in Recycling Center
							int desiredAngle = Convert.getInt(rc.getProp("Angle"));
							int currentAngle = Convert.getInt(getProp("Angle"));
							if(desiredAngle > 360) {
								desiredAngle-=360;
							}
							
							int totalRotation = desiredAngle-currentAngle;
							int swapper = 1;
							if(totalRotation > 180f || totalRotation < -180f) {
								swapper = -1;
							}
							float rotSpeed = Convert.getFloat(getProp("Rotation Speed"));
							
							if(totalRotation > 360) {
								totalRotation-=360;
							}
							if(totalRotation < -360) {
								totalRotation+=360;
							}
							if(Math.abs(totalRotation) < 30 || Math.abs(totalRotation) > 330) {
								rotSpeed = 0.5f;
							}
							
							if(currentAngle == desiredAngle) {
								mBody.setAngularVelocity(0f);
							}
							else if(desiredAngle > currentAngle) {
								mBody.setAngularVelocity(swapper*(rotSpeed));
							}
							else if(desiredAngle < currentAngle) {
								mBody.setAngularVelocity(swapper*(-rotSpeed));
							}
							
							
							Blob blob = (Blob)a;
							Vector2 blobPos = new Vector2(blob.getX(),blob.getY());
							Vector2 blobCpy = blobPos.cpy();
							blobCpy.sub(mBody.getPosition());
							float blobDistance = blobCpy.len();
							blobCpy.nor();
							int blobAngle;
							
							if(blobPos.y > mBody.getPosition().y) {
								blobAngle = (int)(Math.acos(Convert.getDouble(blobCpy.x))*(180/Math.PI));
							}
							else {
								if(blobPos.x > mBody.getPosition().x) {
									blobAngle = (int)((Math.asin(Convert.getDouble(blobCpy.y))+2*Math.PI)*(180/Math.PI));
								}
								else {
									blobAngle = (int)((Math.PI+(Math.abs(Math.asin(Convert.getDouble(blobCpy.y)))))*(180/Math.PI));
								}
							}
							
							//Keeping Blob In
							if(blobDistance > 0.4f) {
								
								int angleTo = blobAngle-180;
								if(angleTo < 0) {
									angleTo+=360;
								}
								Iterable<Body> blobSubs = blob.mSubBodies;
								Vector2 force = new Vector2(vacuumStrength/blob.getNumSubBodies(), 0);
								force.rotate((float)angleTo);
							blob.setPulling(true);
								if(blob.isSolid()) {
									blob.transform();
								}
								for(Body b : blobSubs) {
									b.applyForceToCenter(force);
								}					
								
								/*
								for (Body b : blobSubs){
									if(blobPos.x < mBody.getPosition().x) {
										float xDir = vacuumStrength*Convert.getFloat(Math.cos(Convert.getDouble(angleTo)));
										float yDir = vacuumStrength*Convert.getFloat(Math.sin(Convert.getDouble(angleTo)));
										b.setLinearVelocity(new Vector2(xDir, yDir));
									}
									else {
										float xDir = vacuumStrength*Convert.getFloat(-Math.cos(Convert.getDouble(angleTo)));
										float yDir = vacuumStrength*Convert.getFloat(-Math.sin(Convert.getDouble(angleTo)));;
										b.setLinearVelocity(new Vector2(xDir, yDir));
									}
								}
								*/
							}
							
							//Movement to Recyling
							if(rcPos.x < mBody.getPosition().x) {
								float xDir = Convert.getFloat(getProp("Speed"))*Convert.getFloat(-Math.cos(Convert.getDouble(mNearestBoundry.getProp("Angle"))*Math.PI/180));
								float yDir = Convert.getFloat(getProp("Speed"))*Convert.getFloat(-Math.sin(Convert.getDouble(mNearestBoundry.getProp("Angle"))*Math.PI/180));
								mBody.setLinearVelocity(new Vector2(xDir, yDir));
							}
							else {
								float xDir = Convert.getFloat(getProp("Speed"))*Convert.getFloat(Math.cos(Convert.getDouble(mNearestBoundry.getProp("Angle"))*Math.PI/180));
								float yDir = Convert.getFloat(getProp("Speed"))*Convert.getFloat(Math.sin(Convert.getDouble(mNearestBoundry.getProp("Angle"))*Math.PI/180));
								mBody.setLinearVelocity(new Vector2(xDir, yDir));
							}
							
							float distDif = (mBody.getPosition().x - rcPos.x);
							
							//System.out.println("	BodyPos: " + mBody.getPosition().x);
							//System.out.println("		rcPos: " + rcPos.x);
							//System.out.println("			distDif: " + distDif);
							
							Vector2 ifY = rcPos.cpy();
							ifY.sub(mBody.getPosition());
							float distFrom = Math.abs(ifY.len());
							int rcAngle = Convert.getInt(rc.getProp("Angle"));						
							
							if(rcAngle == 270 || rcAngle == 90){
								if((distDif <= 0.05 && distDif >= -0.05) && (currentAngle >= desiredAngle-2 && currentAngle <= desiredAngle+2)) {
									action = 2;
								}
							}
							else {
								if(distFrom < 3 && (currentAngle >= desiredAngle-2 && currentAngle <= desiredAngle+2)) {
									action = 2;
								}
							}	
							break;
						}
						
						if (action != 1 || action != 2){
							//System.out.println("Action 0!");
							enemySeen = true;
							Blob blob = (Blob)a;
							Vector2 blobPos = new Vector2(blob.getX(),blob.getY());
							
							/*Vector2 boundPos = new Vector2(mNearestBoundry.getX(),mNearestBoundry.getY());
							int otherDir = 1;
							if(blobPos.x < mBody.getPosition().x) {
								otherDir = -1;
							}
							float theX = blobPos.x + Convert.getFloat(otherDir*(Math.cos(Convert.getDouble(mNearestBoundry.getProp("Angle"))*Math.PI/180))*blobPos.len());
							float theY = (boundPos.y + 1.25f) + (Convert.getFloat((otherDir*Math.sin(Convert.getDouble(mNearestBoundry.getProp("Angle"))* Math.PI/180)))*blobPos.len());
							Vector2 desiredPos = new Vector2(theX, theY);
							Vector2 offset = mBody.getPosition().cpy();
							offset.sub(desiredPos);
							offset.nor();
							offset.scl(-80000f);
							mBody.applyForceToCenter(offset, true);
							*/
							
							Vector2 blobCpy = blobPos.cpy();
							blobCpy.sub(mBody.getPosition());
							float blobDistance = blobCpy.len();
							blobCpy.nor();
							int desiredAngle;
							int currentAngle = Convert.getInt(getProp("Angle"));
							
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
			
							int totalRotation = desiredAngle-currentAngle;
							int swapper = 1;
							if(totalRotation > 180 || totalRotation < -180) {
								swapper = -1;
							}
							float rotSpeed = Convert.getFloat(getProp("Rotation Speed"));
							
							if(currentAngle == desiredAngle) {
								mBody.setAngularVelocity(0f);
							}
							else if(desiredAngle > currentAngle) {
								mBody.setAngularVelocity(swapper*(rotSpeed));
							}
							else if(desiredAngle < currentAngle) {
								mBody.setAngularVelocity(swapper*(-rotSpeed));
							}
							
							
							if(blobDistance > 0.5f && blobDistance <= vactDist && (currentAngle >= desiredAngle-2 && currentAngle <= desiredAngle+2)) {
								//Vector2 bodyToBlob = mBody.getPosition();
								//bodyToBlob.sub(blobPos);
								//bodyToBlob.nor();
								int angleTo = desiredAngle-180;
								if(angleTo < 0) {
									angleTo+=360;
								}
								Iterable<Body> blobSubs = blob.mSubBodies;
								Vector2 force = new Vector2(vacuumStrength/blob.getNumSubBodies(), 0);
								force.rotate((float)angleTo);
								blob.setPulling(true);
								if(blob.isSolid()) {
									blob.transform();
								}
								for(Body b : blobSubs) {
									b.applyForceToCenter(force);
								}	
								
								/*
								for (Body b : blobSubs) {
									if(blobPos.x < mBody.getPosition().x) {
										float xDir = vacuumStrength*Convert.getFloat(Math.cos(Convert.getDouble(angleTo)));
										float yDir = vacuumStrength*Convert.getFloat(Math.sin(Convert.getDouble(angleTo)));
										b.setLinearVelocity(new Vector2(xDir, yDir));
									}
									else {
										float xDir = vacuumStrength*Convert.getFloat(-Math.cos(Convert.getDouble(angleTo)));
										float yDir = vacuumStrength*Convert.getFloat(-Math.sin(Convert.getDouble(angleTo)));;
										b.setLinearVelocity(new Vector2(xDir, yDir));
									}
								}
								*/
							}
							
							if(blobDistance < 0.65f) {
								action = 1;					
							}
							
							if(blobPos.x < mBody.getPosition().x) {
								super.update();
								float xDir = Convert.getFloat(getProp("Speed"))*Convert.getFloat(-Math.cos(Convert.getDouble(mNearestBoundry.getProp("Angle"))*Math.PI/180));
								float yDir = Convert.getFloat(getProp("Speed"))*Convert.getFloat(-Math.sin(Convert.getDouble(mNearestBoundry.getProp("Angle"))*Math.PI/180));
								mBody.setLinearVelocity(new Vector2(xDir, yDir));
							}
							else {
								super.update();
								float xDir = Convert.getFloat(getProp("Speed"))*Convert.getFloat(Math.cos(Convert.getDouble(mNearestBoundry.getProp("Angle"))*Math.PI/180));
								float yDir = Convert.getFloat(getProp("Speed"))*Convert.getFloat(Math.sin(Convert.getDouble(mNearestBoundry.getProp("Angle"))*Math.PI/180));
								mBody.setLinearVelocity(new Vector2(xDir, yDir));
							}
							break;
						}
					}
				}
			}
		}
		if (!enemySeen){
			action = 0;
			rememberBlerb = null;
			/*Vector2 offset = mBody.getPosition().cpy();
			offset.sub(mStartPosition);
			offset.scl(-1f);
			offset.nor();
			mBody.applyForceToCenter(offset, false); */
/*
			if(mStartPosition.x == mBody.getPosition().x) {
				mBody.setLinearVelocity(new Vector2(0f,0f));
			}
			else
	*/		
			int currentAngle = Convert.getInt(getProp("Angle"));
			int desiredAngle = 0;
					
			float totalRotation = desiredAngle-currentAngle;
			int swapper = 1;
			if(totalRotation > 180f || totalRotation < -180f) {
				swapper = -1;
			}
			float rotSpeed = Convert.getFloat(getProp("Rotation Speed"));
			
			if(totalRotation > 360) {
				totalRotation-=360;
			}
			if(totalRotation < -360) {
				totalRotation+=360;
			}
			if(Math.abs(totalRotation) < 30 || Math.abs(totalRotation) > 330) {
				rotSpeed = 0.5f;
			}		
			
			if(currentAngle == desiredAngle) {
				mBody.setAngularVelocity(0f);
			}
			else if(desiredAngle > currentAngle) {
				mBody.setAngularVelocity(swapper*(rotSpeed));
			}
			else if(desiredAngle < currentAngle) {
				mBody.setAngularVelocity(swapper*(-rotSpeed));
			}			
			
			float slow = 1f;
			int slowDist = Math.abs((int)(mStartPosition.x - mBody.getPosition().x));
			
			if(slowDist < 0.5) {
				slow = 0.5f;
			}
			
			if(Math.abs(mBody.getPosition().x-mStartPosition.x) < 0.05) {
				mBody.setLinearVelocity(new Vector2(0f,0f));
			}
			else if(mStartPosition.x < mBody.getPosition().x) {
				super.update();
				float xDir = Convert.getFloat(getProp("Speed"))*Convert.getFloat(-Math.cos(Convert.getDouble(mNearestBoundry.getProp("Angle"))*Math.PI/180));
				float yDir = Convert.getFloat(getProp("Speed"))*Convert.getFloat(-Math.sin(Convert.getDouble(mNearestBoundry.getProp("Angle"))*Math.PI/180));
				mBody.setLinearVelocity(new Vector2(slow*xDir, slow*yDir));
			}
			else {
				super.update();
				float xDir = Convert.getFloat(getProp("Speed"))*Convert.getFloat(Math.cos(Convert.getDouble(mNearestBoundry.getProp("Angle"))*Math.PI/180));
				float yDir = Convert.getFloat(getProp("Speed"))*Convert.getFloat(Math.sin(Convert.getDouble(mNearestBoundry.getProp("Angle"))*Math.PI/180));
				mBody.setLinearVelocity(new Vector2(slow*xDir, slow*yDir));
			}
			//if(mBody.getPosition().x == mStartPosition.x) {
		//		mBody.setLinearVelocity(new Vector2(0f,0f));
			//}			
			//returnToStart(mNearestBoundry);
		}
		//Vector2 theBoundPos = new Vector2(mNearestBoundry.getX(),mNearestBoundry.getY());
		/*if(mBody.getPosition().y != ((mNearestBoundry.getY() + 1.25f)) + ((mNearestDistance+1.25f)*Convert.getFloat(Math.sin(Convert.getDouble(mNearestBoundry.getProp("Angle")))))) {
			float desiredY = (mNearestBoundry.getY() + 1.25f) + + ((mNearestDistance+1.25f)*Convert.getFloat(Math.sin(Convert.getDouble(mNearestBoundry.getProp("Angle")))));
			System.out.println("			" + desiredY);
		}
		*/
	}
	
	@Override
	public void setProp(String name, Object val) {
		
		if(name.equals("X")) {
			mStartPosition.x = Convert.getFloat(val);
		}
		if(name.equals("Y")) {
			mStartPosition.y = Convert.getFloat(val);
		}
		super.setProp(name, val);
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
	
	/**
	 * 
	 * @return Nearest VacBotBoundry - so that the Bot can move accurately
	 */
	
	public VacBotBoundry nearestBoundry(float currentDistance) {
		float newDistance = currentDistance;
		VacBotBoundry nearest = null;
		boolean sameBound = true;
		Iterable<StableContact> boundryContact = Game.get().getLevel().getContactHandler().getContacts(this);
		Iterable<Actor> possibleBounds = ContactHandler.getActors(boundryContact);
		for (Actor a : possibleBounds){
			if (a instanceof VacBotBoundry) {
				float possibleNewDist;
				VacBotBoundry boundry = (VacBotBoundry)a;
				Vector2 boundryPos = new Vector2(boundry.getX(),boundry.getY());
				Vector2 distanceFrom = mBody.getPosition().cpy();
				distanceFrom.sub(boundryPos);
				possibleNewDist = distanceFrom.len();
				if(possibleNewDist <= newDistance) {
					newDistance = possibleNewDist;
					nearest = (VacBotBoundry)a;
					if (!nearest.equals(mNearestBoundry)) {
						sameBound = false;
					}
				}
			}
		}
		if(nearest == null) {
			return null;
		}
		return nearest;
	}
/*	
	public void returnToStart(VacBotBoundry lastUsedBound) {
		boolean backAtStart = false;
		boolean nullZone = false;
		VacBotBoundry mNearestReturn = null;
		float mNearestReturnDist = max;
		
		while(!nullZone) {
			Vector2 boundPos = new Vector2(Convert.getFloat(lastUsedBound.getProp("X")),Convert.getFloat(lastUsedBound.getProp("Y")));
			Vector2 desiredPos = new Vector2(mStartPosition.x, (boundPos.len()/2*(Convert.getFloat(Math.sin(boundPos.angle())))));
			Vector2 offset = mBody.getPosition().cpy();
			offset.sub(desiredPos);
			offset.scl(-0.5f);
			offset.nor();
			mBody.applyForceToCenter(offset, true);
			if(nearestBoundry(mNearestReturnDist) != null) {
				nullZone = true;
			}
		}
		
		while(!backAtStart) {
			mNearestReturn = nearestBoundry(mNearestReturnDist);
			Vector2 boundPos = new Vector2(Convert.getFloat(mNearestReturn.getProp("X")),Convert.getFloat(mNearestReturn.getProp("Y")));
			Vector2 desiredPos = new Vector2(mStartPosition.x, (boundPos.len()/2*(Convert.getFloat(Math.sin(boundPos.angle())))));
			Vector2 offset = mBody.getPosition().cpy();
			offset.sub(desiredPos);
			offset.scl(-0.5f);
			offset.nor();
			mBody.applyForceToCenter(offset, true);
			if(mBody.getPosition().x == mStartPosition.x) {
				backAtStart = true;
			}
		}
	}
*/	
	public void setAction(int act) {
		action = act;
	}
	
	@Override
	public void postLoad() {
		// TODO Auto-generated method stub
	}
}
