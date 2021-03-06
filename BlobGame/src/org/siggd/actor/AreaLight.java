package org.siggd.actor;

import org.box2dLight.PointLight;
import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.actor.meta.IObservable;
import org.siggd.actor.meta.IObserver;
import org.siggd.view.BodySprite;
import org.siggd.view.LevelView;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class AreaLight extends Actor implements IObserver {
	private String mTex;
	private PointLight mLight;

	public AreaLight(Level level, long id) {
		super(level, id);
		mName = "lightbulb";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 32, BodyType.StaticBody, origin, true);
		mDrawable.mDrawables.add(new BodySprite(mBody, origin, mTex));
		setProp("Visible", (Integer) 0);
		if (LevelView.mUseLights) {
			mLight = new PointLight(Game.get().getLevelView().getRayHandler(), 32, new Color(.1f,
					.1f, .1f, 1), 5f, mBody.getPosition().x, mBody.getPosition().y);
			mLight.attachToBody(mBody, 0f, 0f);
			mLight.setSoft(true);
			mLight.setSoftnessLenght(.5f);
			setProp("Red", mLight.getColor().r);
			setProp("Green", mLight.getColor().g);
			setProp("Blue", mLight.getColor().b);
			setProp("Alpha", mLight.getColor().a);
			setProp("Distance", mLight.getDistance());
			setProp("Softness", mLight.getSoftShadowLenght());
			setProp("Static", 0);
		}
		if (mLight != null)
			mLight.setActive(false);
		setProp("Active", 1);
		// TODO: set light active based on if actor is dummy or not
		setProp("X", -10000);
	}

	@Override
	public void setProp(String name, Object val) {
		if (name == "Distance") {
			if (mLight != null)
				mLight.setDistance((Float) Convert.getFloat(val));
		} else if (name == "Softness") {
			if (mLight != null) {
				float value = (Float) Convert.getFloat(val);
				if (value > 0) {
					mLight.setSoft(true);
					mLight.setSoftnessLenght(value);
				} else {
					mLight.setSoft(false);
				}
			}
		} else if (name == "Static") {
			if (mLight != null) {
				float value = (Float) Convert.getFloat(val);
				if (value == 1) {
					mLight.setStaticLight(true);
				} else if (value == 0) {
					mLight.setStaticLight(false);
				} else {
					val = mProps.get("Static");
				}
			}
		} else if (name == "Red") {
			if (mLight != null) {
				Color color = mLight.getColor();
				color.r = (Float) Convert.getFloat(val);
				mLight.setColor(color);
			}
		} else if (name == "Green") {
			if (mLight != null) {
				Color color = mLight.getColor();
				color.g = (Float) Convert.getFloat(val);
				mLight.setColor(color);
			}
		} else if (name == "Blue") {
			if (mLight != null) {
				Color color = mLight.getColor();
				color.b = (Float) Convert.getFloat(val);
				mLight.setColor(color);
			}
		} else if (name == "Alpha") {
			if (mLight != null) {
				Color color = mLight.getColor();
				color.a = (Float) Convert.getFloat(val);
				mLight.setColor(color);
			}
		} else if (name == "Active") {
			if (mLight != null) {
				mLight.setActive(Convert.getFloat(val) == 1);
			}
		}
		super.setProp(name, val);
	}

	public void update() {
		if (mLight != null)
			mLight.setActive(inputActive());
	}

	@Override
	public void loadResources() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadBodies() {
		// TODO Auto-generated method stub

	}

	@Override
	public void postLoad() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		super.dispose();
	}

	public boolean inputActive() {
		if (mInputSrc == null) {
			return false;
		}

		Object input = mInputSrc.observe();
		return (input instanceof Boolean && (Boolean) input);
	}

	// //////////////
	// Properties
	// //////////////

	private IObservable mInputSrc;

	@Override
	public Actor inputSrc() {
		return (Actor) mInputSrc;
	}

	@Override
	public void inputSrc(Actor inputSrc) {
		mInputSrc = (inputSrc instanceof IObservable) ? (IObservable) inputSrc : null;
	}
}
