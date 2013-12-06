package org.siggd.actor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.box2dLight.PointLight;
import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.Player;
import org.siggd.Timer;
import org.siggd.actor.meta.IObservable;
import org.siggd.view.BodySprite;
import org.siggd.view.Drawable;
import org.siggd.view.LevelView;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Spawner extends Actor implements IObservable {
	private ArrayList<Actor> mSpawnees;
	private String mTex;
	private String offTex;
	private Drawable mDefaultDrawable;
	private Drawable mOffDrawable;
	private Timer mSpawnTimer;
	private Timer mTexTimer;
	private int maxBlobs;
	private int texChangeTime = 60;
	private PointLight mPointLight;
	private String mSoundFile = "data/sfx/spawn.wav";
	// For Spawning Offset
	// private Vector2 spawnOffset = new Vector2(0, 1.5f);
	// private Vector2 rotatedOffset;
	private int mInitDelay = 0;

	public Spawner(Level level, long id) {
		super(level, id);
		mSpawnees = new ArrayList<Actor>();
		mName = "Spawner";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		offTex = mTex.substring(0, mTex.length() - 6) + "off.png";
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 512, BodyType.StaticBody, origin, false);
		mDefaultDrawable = (new BodySprite(mBody, origin, mTex));
		mOffDrawable = (new BodySprite(mBody, origin, offTex));
		mDrawable.mDrawables.add(mOffDrawable);
		maxBlobs = Game.get().getNumberOfPlayers();

		mSpawnTimer = new Timer();
		mSpawnTimer.unpause();

		mTexTimer = new Timer();
		mTexTimer.setTimer(texChangeTime);
		mTexTimer.unpause();

		this.setProp("Blob Spawner", 0);
		this.setProp("Rate", 60);
		this.setProp("Exit Velocity", 2);
		this.setProp("Initial Delay", 0);
		this.setFriction(.1f);
		this.setProp("Mute", 0);

		if (LevelView.mUseLights) {
			mPointLight = new PointLight(Game.get().getLevelView().getRayHandler(), 16);
			mPointLight.setDistance(1.5f);
			mPointLight.attachToBody(mBody, 0, 0);
			mPointLight.setSoftnessLenght(1f);
			mPointLight.setXray(true);
		}

		setState(true);
	}

	@Override
	public void loadResources() {
		AssetManager man = Game.get().getAssetManager();
		man.load(mTex, Texture.class);
		man.load(offTex, Texture.class);
		man.load(mSoundFile, Sound.class);

	}

	@Override
	public void update() {
		super.update();
		if (--mInitDelay > 0)
			return;
		mSpawnTimer.update();
		mTexTimer.update();
		if (mSpawnees.size() > 0) {
			setState(true);
			if (mSpawnTimer.isTriggered() && Game.get().getLevel().musicTick()) {
				String curmap = Game.get().getLevel().getAssetKey();
				if (Convert.getInt(getProp("Mute")) == 0) {
					AssetManager man = Game.get().getAssetManager();
					Sound sound;
					long soundID;
					if (man.isLoaded(mSoundFile)
							&& !"earth".equals(Game.get().getLevel().getAssetKey())) {
						sound = man.get(mSoundFile, Sound.class);
						soundID = sound.play();
						sound.setVolume(soundID, .45f);
					}
				}
				spawnActor();
				mSpawnTimer.reset();
				mTexTimer.reset();
			}
		}
		if (mSpawnees.size() == 0 && mTexTimer.isTriggered())
			setState(false);
	}

	@Override
	public void loadBodies() {
	}

	@Override
	public void postLoad() {
		mInitDelay = Convert.getInt(getProp("Initial Delay"));
		if ("earth".equals(Game.get().getLevel().getAssetKey())) {
			maxBlobs = 8;
		}
		// find all actors that are pointing to this spawner
		for (Actor a : this.getLevel()) {
			if (this.getId() == Convert.getInt(a.getProp("Spawner"))) {
				mSpawnees.add(a);
				a.setActive(false);
			}
		}
		// sort by the Y val
		Collections.sort(mSpawnees, new Comparator<Actor>() {
			@Override
			public int compare(Actor o1, Actor o2) {
				return (int) (o1.getY() - o2.getY());
			}
		});
		// check whether it is a blob spawner
		if (Convert.getInt(this.getProp("Blob Spawner")) == 1) {
			for (int i = 0; i < maxBlobs; i++) {
				// construct blob
				if ("earth".equals(Game.get().getLevel().getAssetKey())) {
					spawnBlob(i);
				} else {
					Player player = Game.get().getPlayer(i);
					if (player != null && player.active) {
						player.mActor = spawnBlob(i);
					}
				}
			}
		}

		LevelView lv = Game.get().getLevelView();

		if (mLevel.getAssetKey() != null && Convert.getInt(this.getProp("Blob Spawner")) == 1) {
			lv.setCameraPosition(mBody.getPosition());
			if (Game.get().getState() != Game.MENU) {
				lv.positionCamera(false);
			}
		}
	}

	private Blob spawnBlob(int id) {
		Blob blob = new Blob(this.getLevel(), this.getLevel().getId());
		blob.setProp("Player ID", id);
		blob.postLoad();
		// assign blob to player
		blob.setActive(false);
		// set the layer to the layer of the placeholder blob
		int layer = Convert.getInt(mLevel.getBlobs(false).get(0).getProp("Layer"));
		blob.setProp("Layer", (Integer) layer);
		mSpawnees.add((int) Math.floor(Math.random() * mSpawnees.size()), blob);
		return blob;
	}

	public void addToSpawn(Actor a, int delay) {
		if (!mSpawnees.contains(a)) {
			if (a.isActive()) {
				a.setActive(false);
			}
			if (mSpawnees.size() == 0) {
				mSpawnTimer.unpause();
				mSpawnTimer.mCurTime = -delay;
			}
			mSpawnees.add(a);
		}
	}

	private void spawnActor() {
		Actor spawnee = mSpawnees.get(0);
		if (spawnee instanceof Blob) {
			if (!getLevel().getBlobs(false).contains(spawnee)) {
				this.getLevel().addActor(spawnee);
			}
		}

		// For Spawning Offset
		// Vector2 pos = new Vector2(getX() + rotatedOffset.x, getY() +
		// rotatedOffset.y);
		Vector2 pos = mBody.getPosition().cpy();
		spawnee.setProp("X", pos.x);
		spawnee.setProp("Y", pos.y);
		spawnee.setProp("Angle", mBody.getAngle());
		if (spawnee instanceof Blob) {
			((Blob) spawnee).mSpawning = true;
		}
		// actor is now spawned
		spawnee.setActive(true);
		// spawner defaults facing up, hence: (0,1)
		Vector2 vel = new Vector2(0, Convert.getInt(getProp("Exit Velocity")));
		vel.rotate(Convert.getDegrees(mBody.getAngle()) + (float) (10f * Math.random() - 5f));
		spawnee.setVelocityX(vel.x);
		spawnee.setVelocityY(vel.y);
		if (spawnee instanceof Blob) {
			if (((Blob) spawnee).isSolid()) {
				((Blob) spawnee).transform();
			}

		}
		mSpawnees.remove(spawnee);
	}

	public int blobsContained() {
		int count = 0;
		for (Actor a : mSpawnees) {
			if (a instanceof Blob) {
				count++;
			}
		}
		return count;
	}

	public Object observe() {
		return getState();
	}

	public boolean getState() {
		return mState;
	}

	private void setState(boolean state) {
		if (mPointLight != null)
			mPointLight.setColor(state ? 0 : .3f, state ? .3f : 0, 0, 0.8f);
		if (state == mState)
			return;
		if (state) {
			mDrawable.mDrawables.remove(mOffDrawable);
			mDrawable.mDrawables.add(mDefaultDrawable);
		} else {
			mDrawable.mDrawables.remove(mDefaultDrawable);
			mDrawable.mDrawables.add(mOffDrawable);
		}
		mState = state;
	}

	// LOOK I DIDNT COPY PASTERINIO MACORONI DONGERINI
	private boolean mState = false;

	@Override
	public void setProp(String name, Object val) {
		// undefined behavior if game is running
		if (name.equals("Rate")) {
			mSpawnTimer.setTimer(Convert.getInt(val));
		}

		// For Spawning Offset
		// if (name.equals("Angle")) {
		// rotatedOffset = spawnOffset.cpy().rotate(Convert.getFloat(val));
		// }

		super.setProp(name, val);
	}
}
