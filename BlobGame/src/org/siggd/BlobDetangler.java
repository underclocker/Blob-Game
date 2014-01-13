package org.siggd;

import java.util.ArrayList;

import org.siggd.actor.Actor;
import org.siggd.actor.Blob;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

public class BlobDetangler implements ContactListener {

	@Override
	public void beginContact(Contact contact) {

	}

	@Override
	public void endContact(Contact contact) {

	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		if (true) return;
		Fixture fixtureA = contact.getFixtureA();
		Fixture fixtureB = contact.getFixtureB();
		Body bodyA = fixtureA.getBody();
		Body bodyB = fixtureB.getBody();
		Actor actorA = (Actor) bodyA.getUserData();
		Actor actorB = (Actor) bodyB.getUserData();
		if (actorA != null && actorB != null && actorA instanceof Blob && actorB instanceof Blob) {
			ArrayList<Blob> blobsA = (ArrayList<Blob>) fixtureA.getUserData();
			ArrayList<Blob> blobsB = (ArrayList<Blob>) fixtureB.getUserData();
			Blob blobA = ((Blob) actorA);
			Blob blobB = ((Blob) actorB);
			if ((blobsA.contains(actorB) || blobA.isSolid())
					&& (blobsB.contains(actorA) || blobB.isSolid())
					&& !(blobB.isSolid() && blobA.isSolid())) {
				contact.setEnabled(false);
			}
		}
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {

	}

}
