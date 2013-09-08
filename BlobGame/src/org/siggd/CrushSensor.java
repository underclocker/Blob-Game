package org.siggd;

import java.util.HashSet;
import java.util.LinkedList;

import org.siggd.actor.Actor;
import org.siggd.actor.Blob;
import org.siggd.actor.ExplodeBall;
import org.siggd.actor.Gear;
import org.siggd.actor.Platform;
import org.siggd.actor.Redirector;
import org.siggd.actor.Wind;
import org.siggd.actor.ImplodeBall;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.JointEdge;

/**
 * Utility class to detect if a body is touching a static object, through a
 * chain of dynamic objects. Does le fancy graph things.
 * 
 * @author mysterymath
 * 
 */
public class CrushSensor {
	/**
	 * Returns true if the body is touching a static object, through a chain of
	 * dynamic objects.
	 * 
	 * @param contactHandler
	 *            The contacts
	 * @param a
	 *            The actor to check
	 */
	public static boolean canCrush(ContactHandler contactHandler, Actor a) {

		// TODO: replace all checks for instanceof with parameter flags if
		// possible.

		// Set of bodies already seen, to handle loops in the graph
		HashSet<Actor> alreadySeen = new HashSet<Actor>();
		alreadySeen.add(a);

		// Do a bfs search, ignoring back-links
		LinkedList<Actor> bfsQueue = new LinkedList<Actor>();
		bfsQueue.add(a);
		while (!bfsQueue.isEmpty()) {
			// Pull the next actor off the queue
			Actor cur = bfsQueue.poll();

			// Mark current node as already seen
			alreadySeen.add(cur);

			// Ignore redirectors and wind AND SPLODERS
			if (cur instanceof Redirector || cur instanceof Wind || cur instanceof ImplodeBall
					|| cur instanceof ExplodeBall) {
				continue;
			}

			// Get connected bodies
			Iterable<Body> bodies = ContactHandler.getBodies(contactHandler.getContacts(cur));

			// Return true if any of these touched bodies are static
			for (Body b : bodies) {
				if (!a.equals((Actor) b.getUserData())
						&& (b.getType() == BodyType.StaticBody || b.getType() == BodyType.KinematicBody)
						&& !b.getFixtureList().get(0).isSensor()) {
					return true;
				}
			}
			if (cur instanceof Blob || cur instanceof Platform || cur instanceof Gear) {
				Iterable<JointEdge> joints = cur.getMainBody().getJointList();
				for (JointEdge j : joints) {
					if (!a.equals((Actor) j.other.getUserData())
							&& (j.other.getType() == BodyType.StaticBody || j.other.getType() == BodyType.KinematicBody)
							&& !j.other.getFixtureList().get(0).isSensor()) {
						return true;
					}
				}
			}
			// Get connected actors
			Iterable<Actor> actors = ContactHandler.getActors(contactHandler.getContacts(cur));

			// Add any actors to the search that we haven't already seen
			for (Actor edge : actors) {
				if (!alreadySeen.contains(edge)) {
					bfsQueue.add(edge);
				}
			}
			if (cur instanceof Blob || cur instanceof Platform || cur instanceof Gear) {
				Iterable<JointEdge> joints = cur.getMainBody().getJointList();
				joints = cur.getMainBody().getJointList();
				for (JointEdge j : joints) {
					Actor actor = (Actor) j.other.getUserData();
					if (!alreadySeen.contains(actor)) {
						bfsQueue.add(actor);
					}
				}
			}
		}

		// Went through the whole graph, no static bodies. We're in the clear!
		return false;
	}
}
