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
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.JointEdge;

public class Respawner extends Actor {
	private String mTex;

	public Respawner(Level level, long id) {
		super(level, id);
		mName = "Respawner";
		mTex = "data/" + Game.get().getBodyEditorLoader().getImagePath(mName);
		Vector2 origin = new Vector2();
		mBody = makeBody(mName, 512, BodyType.StaticBody, origin, true);
		((CompositeDrawable) mDrawable).mDrawables.add(new BodySprite(mBody, origin, mTex));
		((CompositeDrawable) mDrawable).mDrawables.add(new DebugActorLinkDrawable(this,
				"Target Spawner", Color.RED));
		setProp("Target Spawner", -1);
		setProp("Ignore Blobs", 0);
		setProp("Ignore Non-Blobs", 0);
		setProp("Visible", 0);
	}

	public void update() {
		Iterable<StableContact> contacts = Game.get().getLevel().getContactHandler()
				.getContacts(this);
		// get the bodies touching this actor
		Iterable<Body> bodies = ContactHandler.getBodies(contacts);
		Actor actor;
		for (Body b : bodies) {
			// only concerned with dynamic bodies (we don't want to respond
			// background bodies)
			if (b.getType() != BodyType.DynamicBody)
				continue;
			actor = (Actor) b.getUserData();

			// skip the blob body if respawner ignores blobs
			if (Convert.getInt(getProp("Ignore Blobs")) == 1 && actor instanceof Blob)
				continue;
			// skip non-blob body if respawner ignores non-blobs
			if (Convert.getInt(getProp("Ignore Non-Blobs")) == 1 && !(actor instanceof Blob))
				continue;

			int targetId = Convert.getInt(getProp("Target Spawner"));
			// there is a targeted spawner to send actors to
			if (targetId != -1) {
				Spawner sp = (Spawner) mLevel.getActorById(targetId);
				sp.addToSpawn(actor);
				ArrayList<JointEdge> edges = b.getJointList();
				for (int i = 0; i < edges.size(); i++) {
					JointEdge edge = edges.get(i);
					// get the other actor this joint is connected to
					// if the other actor is not itself and is a blob
					Actor otherActor = (Actor) edge.other.getUserData();
					if (otherActor != actor && (otherActor instanceof Blob)) {
						// you need to watch house of cards
						Joint congressmanRusso = edge.joint;
						((Blob) otherActor).removeJoint(congressmanRusso);
						if (actor instanceof Blob) {
							((Blob) actor).removeJoint(congressmanRusso);
						}
						Game.get().getLevel().getWorld().destroyJoint(congressmanRusso);
					}
				}
				Game.get().getLevel().getContactHandler().destroyContacts(b);
			}
		}
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
