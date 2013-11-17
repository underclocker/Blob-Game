package org.siggd.actor;

import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.actor.meta.IObservable;
import org.siggd.view.BodySprite;
import org.siggd.view.DebugActorLinkDrawable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class NotGate extends Actor implements IObservable {
	private String mTex;
	private boolean mPropagate;
	private int mPropagateVal;

	public NotGate(Level level, long id) {
		super(level, id);
		mName = "Not Gate";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 andigin = new Vector2();
		mBody = makeBody(mName, 64, BodyType.StaticBody, andigin, false);
		mBody.setFixedRotation(true);
		mBody.setActive(false);
		setProp("Output", (Integer) 0);
		setProp("Input", (Integer) (-1));
		// invisible when running
		setProp("Visible", (Integer) 0);
		// gate should always be ontop
		setProp("Layer", Integer.MAX_VALUE);
		// gfx
		// magic number in pixels, based on textured
		mDrawable.mDrawables.add(new DebugActorLinkDrawable(this, "Input", "Output", Color.RED,
				Color.GREEN, new Vector2(0, -60)));
		mDrawable.mDrawables.add(new BodySprite(mBody, andigin, mTex));
	}

	@Override
	public void update() {

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
		Actor A = mLevel.getActorById(Convert.getInt(getProp("Input")));
		// TODO Auto-generated method stub
		return !(Boolean)((IObservable) A).observe();
	}

}