package org.siggd.actor;

import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.view.BodySprite;
import org.siggd.view.CompositeDrawable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;

public class ChainLink extends Actor {
	private String mTex;
	private Body mAnchor;
	private Joint mJoint;

	public ChainLink(Level level, long id) {
		super(level, id);
		mName = "lightbulb";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 64, BodyType.DynamicBody, origin);
		mBody.setAngularDamping(.35f);
		((CompositeDrawable) mDrawable).mDrawables.add(new BodySprite(mBody, origin, mTex));
		setProp("HangingTarget", (Integer) (-1));
		setProp("Density", (Float) (.25f));
	}

	public void setProp(String name, Object val) {
		super.setProp(name, val);
	}

	@Override
	public void loadResources() {
		AssetManager man = Game.get().getAssetManager();
		man.load(mTex, Texture.class);
	}

	@Override
	public void loadBodies() {
		// do we need this?
	}

	public void update() {

	}

	public void destroy() {
		if (mJoint != null) {
			getLevel().getWorld().destroyJoint(mJoint);
		}
		super.destroy();
	}

	public void dispose() {
		AssetManager man = Game.get().getAssetManager();
		man.unload(mTex);
	}

	@Override
	public void postLoad() {
		Actor targetActor = getLevel().getActorById(Convert.getInt(getProp("HangingTarget")));
		if (targetActor == null) {
			return;
		}
		Body targetBody = targetActor.getMainBody();
		if (targetBody != mAnchor) {
			mAnchor = targetBody;
			if (targetBody != null) {
				if (mJoint != null) {
					getLevel().getWorld().destroyJoint(mJoint);
					mJoint = null;
				}
				RevoluteJointDef rjd = new RevoluteJointDef();
				rjd.enableLimit = true;
				rjd.collideConnected = false;
				Vector2 rot = new Vector2(0, 0);
				rot.rotate((float) (Convert.getFloat(getProp("Angle")) * 180 / Math.PI));
				rjd.initialize(mBody, mAnchor, new Vector2(getX() + rot.x, getY() + rot.y));
				mJoint = getLevel().getWorld().createJoint(rjd);
			}
		}
	}
}
