package org.siggd.actor;

import org.siggd.Game;
import org.siggd.Level;
import org.siggd.view.BodySprite;
import org.siggd.view.CompositeDrawable;
import org.siggd.view.DebugActorLinkDrawable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class RecyclingCenter extends Actor {
	private String mTex;

	public RecyclingCenter(Level level, long id) {
		super(level, id);
		mName = "RecyclingCenter";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 32, BodyType.StaticBody, origin, true);
		((CompositeDrawable) mDrawable).mDrawables.add(new BodySprite(mBody, origin, mTex));
		((CompositeDrawable) mDrawable).mDrawables.add(new DebugActorLinkDrawable(this,
				"Target Spawner", Color.RED));
		setProp("Target Spawner", -1);
	}

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
}
