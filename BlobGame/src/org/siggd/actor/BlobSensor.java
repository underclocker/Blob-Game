package org.siggd.actor;

import java.util.ArrayList;

import org.siggd.ContactHandler;
import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.StableContact;
import org.siggd.view.BodySprite;
import org.siggd.view.DebugActorLinkDrawable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class BlobSensor extends Actor {
	private String mTex;
	private boolean mPropagate;
	private int mPropagateVal;
	private ArrayList<Blob> mBlobs;

	public BlobSensor(Level level, long id) {
		super(level, id);
		mBlobs = new ArrayList<Blob>();
		mName = "BlobSensor";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 andigin = new Vector2();
		mBody = makeBody(mName, 512, BodyType.StaticBody, andigin, false);
		mBody.setFixedRotation(true);
		mBody.getFixtureList().get(0).setSensor(true);
		setProp("Output", (Integer) 0);
		// invisible when running
		setProp("Visible", (Integer) 0);
		// gfx
		// magic number in pixels, based on textured
		mDrawable.mDrawables.add(new DebugActorLinkDrawable(this, "Input", "Output", Color.RED,
				Color.GREEN, new Vector2(0, 0)));
		mDrawable.mDrawables.add(new BodySprite(mBody, andigin, mTex));
	}

	@Override
	public void update() {
		Iterable<StableContact> contacts = Game.get().getLevel().getContactHandler()
				.getContacts(this);
		Iterable<Actor> actors = ContactHandler.getActors(contacts);
		mBlobs.clear();
		for (Actor a : actors) {
			if (a instanceof Blob) {
				mBlobs.add((Blob) a);
			}
		}
		// creates a propagation delay
		setProp("Output", (Integer) (mBlobs.size() > 0 ? 1 : 0));
	}

	public ArrayList<Blob> getBlobs() {
		return mBlobs;
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
