package org.siggd.actor;

import java.util.ArrayList;

import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.actor.meta.IObservable;
import org.siggd.view.BodySprite;
import org.siggd.view.CompositeDrawable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;

/**
 * This class can be used as a convenient shortcut for any of the bodies in
 * bodies.json. Just set the Body property to the appropriate body, and the
 * actor will become that body.
 * 
 * @author mysterymath
 * 
 */
public class SwingingCan extends Actor implements IObservable{
	private String mTex;
	private Body mAnchor;
	private Joint mJoint;

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
	public SwingingCan(Level level, long id) {
		super(level, id);
		mName = "Can";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 256, BodyType.DynamicBody, origin);
		mBody.setAngularDamping(.35f);
		((CompositeDrawable) mDrawable).mDrawables.add(new BodySprite(mBody, origin, mTex));
		setProp("HangingTarget", (Integer) (-1));
		setProp("Restitution", (Float) (0f));
		setProp("Grabbable", (Integer) (1));
		setProp("Density", (Float) (0.5f));
	}

	@Override
	public void setProp(String name, Object val) {
		if (name.equals("X") || name.equals("Y")) {
		}
		super.setProp(name, val);
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
	public void update() {
		super.update();
	}

	@Override
	public void destroy() {
		if (mJoint != null) {
			getLevel().getWorld().destroyJoint(mJoint);
		}
		super.destroy();
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

	@Override
	public void postLoad() {
		Actor targetActor = getLevel().getActorById(Convert.getInt(getProp("HangingTarget")));
		if (targetActor == null) {
			return;
		}
		Body targetBody = targetActor.getMainBody();
		mAnchor = targetBody;
		if (targetBody != null) {
			RevoluteJointDef rjd = new RevoluteJointDef();
			rjd.collideConnected = false;
			Vector2 rot = new Vector2(0, 1.0f);
			rot.rotate((float) (Convert.getFloat(getProp("Angle"))));
			rjd.initialize(mBody, mAnchor, new Vector2(getX() + rot.x, getY() + rot.y));
			mJoint = getLevel().getWorld().createJoint(rjd);
		}

	}

	@Override
	public Object observe() {
		// Sweet Lord...  This is gona be expensive...
		ArrayList<Blob> blobs = Game.get().getLevel().getBlobs(true);
		for (Blob blob : blobs){
			ArrayList<Joint> joints = blob.getJoints();
			for (Joint joint : joints){
				if (joint.getBodyA().getUserData().equals(this) || joint.getBodyB().getUserData().equals(this)){
					return true;
				};
			}
		}
		return false;
	}
}
