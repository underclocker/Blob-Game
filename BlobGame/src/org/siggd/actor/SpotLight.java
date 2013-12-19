package org.siggd.actor;

import org.box2dLight.ConeLight;
import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.actor.meta.IObserver;
import org.siggd.actor.meta.IObservable;
import org.siggd.view.BodySprite;
import org.siggd.view.LevelView;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class SpotLight extends Actor implements IObserver {
	private String mTex;
	private ConeLight mLight;

	public SpotLight(Level level, long id) {
		super(level, id);
		mName = "lightbulb";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 64, BodyType.StaticBody, origin, true);
		mDrawable.mDrawables.add(new BodySprite(mBody, origin, mTex));
		//setProp("Visible", (Integer) 0);
		if (LevelView.mUseLights) {
			mLight = new ConeLight(Game.get().getLevelView().getRayHandler(), 128, new Color(.1f,
					.1f, .1f, 1), 5f, mBody.getPosition().x, mBody.getPosition().y,
					mBody.getAngle(), 30f);
			mLight.attachToBody(mBody, 0f, 0f);
			mLight.setSoft(true);
			mLight.setSoftnessLenght(.5f);
			setProp("Red", mLight.getColor().r);
			setProp("Green", mLight.getColor().g);
			setProp("Blue", mLight.getColor().b);
			setProp("Alpha", mLight.getColor().a);
			setProp("Distance", mLight.getDistance());
			setProp("Softness", mLight.getSoftShadowLenght());
			setProp("Cone Angle", mLight.getConeDegree());
			setProp("Static", 0);
			setProp("Layer", 3);
			mLight.setActive(false);
			setProp("Active", 1);
			// TODO: set light active based on if actor is dummy or not
			setProp("X", -10000);
		}
	}

	@Override
	public void setProp(String name, Object val) {
		if (name.equals("Distance")) {
			if (mLight != null)
				mLight.setDistance((Float) Convert.getFloat(val));
		} else if (name.equals("Cone Angle")) {
			if (mLight != null)
				mLight.setConeDegree((Float) Convert.getFloat(val));
		} else if (name.equals("Softness")) {
			if (mLight != null) {
				float value = (Float) Convert.getFloat(val);
				if (value > 0) {
					mLight.setSoft(true);
					mLight.setSoftnessLenght(value);
				} else {
					mLight.setSoft(false);
				}
			}
		} else if (name.equals("Static")) {
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
		} else if (name.equals("Red")) {
			if (mLight != null) {
				Color color = mLight.getColor();
				color.r = (Float) Convert.getFloat(val);
				mLight.setColor(color);
			}
		} else if (name.equals("Green")) {
			if (mLight != null) {
				Color color = mLight.getColor();
				color.g = (Float) Convert.getFloat(val);
				mLight.setColor(color);
			}
		} else if (name.equals("Blue")) {
			if (mLight != null) {
				Color color = mLight.getColor();
				color.b = (Float) Convert.getFloat(val);
				mLight.setColor(color);
			}
		} else if (name.equals("Alpha")) {
			if (mLight != null) {
				Color color = mLight.getColor();
				color.a = (Float) Convert.getFloat(val);
				mLight.setColor(color);
			}
		} else if (name.equals("Active")) {
			if (mLight != null) {
				mLight.setActive((Float) Convert.getFloat(val) == 1);
			}
		}
		super.setProp(name, val);
	}

	public void update() {
		if (mLight != null) {
			mLight.setActive(inputActive());
		}
	}

	@Override
	public void loadResources() {
		AssetManager man = Game.get().getAssetManager();
		man.load(mTex, Texture.class);
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
			return true;
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
