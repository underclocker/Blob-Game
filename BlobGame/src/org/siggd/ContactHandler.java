package org.siggd;

import java.util.ArrayList;

import org.siggd.actor.Actor;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

public class ContactHandler implements ContactListener {
	private ArrayList<StableContact> mContacts;
	private ArrayList<ContactListener> mListeners;

	/**
	 * Constructor
	 */
	public ContactHandler() {
		mContacts = new ArrayList<StableContact>();
		mListeners = new ArrayList<ContactListener>();
	}

	@Override
	public void beginContact(Contact contact) {
		mContacts.add(new StableContact(contact));
		Actor aa = (Actor) contact.getFixtureA().getBody().getUserData();
		Actor ab = (Actor) contact.getFixtureB().getBody().getUserData();
		if (aa instanceof Knocked) {
			((Knocked) aa).knocked(ab);
		} else if (ab instanceof Knocked) {
			((Knocked) ab).knocked(aa);
		}
		for (ContactListener l : mListeners) {
			l.beginContact(contact);
		}
	}

	@Override
	public void endContact(Contact contact) {
		for (int i = 0; i < mContacts.size(); i++) {
			StableContact c = mContacts.get(i);
			if (c.getFixtureA() == contact.getFixtureA()
					&& c.getFixtureB() == contact.getFixtureB()) {
				mContacts.remove(i);
			}
		}

		for (ContactListener l : mListeners) {
			l.endContact(contact);
		}
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		for (ContactListener l : mListeners) {
			l.preSolve(contact, oldManifold);
		}
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		for (ContactListener l : mListeners) {
			l.postSolve(contact, impulse);
		}
	}

	public void addListener(ContactListener listen) {
		mListeners.add(listen);
	}

	public void removeListener(ContactListener listen) {
		mListeners.remove(listen);
	}

	/**
	 * Gets all the contacts
	 * 
	 * @return The contacts
	 */
	public Iterable<StableContact> getContacts() {
		return mContacts;
	}

	/**
	 * Returns contacts for a Fixture, with the specified fixture as fixture A
	 * 
	 * @param f
	 *            The fixture for which to get contacts
	 * @return A list of contacts, with the specified fixture as fixture A
	 */
	public Iterable<StableContact> getContacts(Fixture f) {
		ArrayList<StableContact> ret = new ArrayList<StableContact>();

		for (StableContact c : mContacts) {
			if (c.getFixtureA() == f) {
				ret.add(c);
			}

			if (c.getFixtureB() == f) {
				ret.add(new StableContact(c, true));
			}
		}

		return ret;
	}

	/**
	 * Returns contacts for a Body, with the specified Body owning fixture A
	 * 
	 * @param b
	 *            The Body for which to get contacts
	 * @return A list of contacts, with the specified Body owning fixture A
	 */
	public Iterable<StableContact> getContacts(Body b) {
		ArrayList<StableContact> ret = new ArrayList<StableContact>();

		for (StableContact c : mContacts) {
			if (c.getFixtureA().getBody() == b) {
				ret.add(c);
			}

			if (c.getFixtureB().getBody() == b) {
				ret.add(new StableContact(c, true));
			}
		}

		return ret;
	}

	/**
	 * Returns contacts for a Actor, with the specified Actor owning fixture A
	 * 
	 * @param a
	 *            The Actor for which to get contacts
	 * @return A list of contacts, with the specified Actor owning fixture A
	 */
	public Iterable<StableContact> getContacts(Actor a) {
		ArrayList<StableContact> ret = new ArrayList<StableContact>();

		for (StableContact c : mContacts) {
			if (c.getFixtureA().getBody().getUserData() == a) {
				ret.add(c);
			}

			if (c.getFixtureB().getBody().getUserData() == a) {
				ret.add(new StableContact(c, true));
			}
		}

		return ret;
	}

	/**
	 * Returns Fixtures from a set of contacts, considering only Fixture B for
	 * each contact
	 * 
	 * @param contacts
	 *            The contacts
	 * @return The fixtures being touched
	 */
	public static Iterable<Fixture> getFixturesB(Iterable<StableContact> contacts) {
		ArrayList<Fixture> ret = new ArrayList<Fixture>();

		for (StableContact c : contacts) {
			Fixture f = c.getFixtureB();
			if (!ret.contains(f)) {
				ret.add(f);
			}
		}

		return ret;
	}

	/**
	 * Returns Fixtures from a set of contacts, considering only Fixture A for
	 * each contact
	 * 
	 * @param contacts
	 *            The contacts
	 * @return The fixtures being touched
	 */
	public static Iterable<Fixture> getFixturesA(Iterable<StableContact> contacts) {
		ArrayList<Fixture> ret = new ArrayList<Fixture>();

		for (StableContact c : contacts) {
			Fixture f = c.getFixtureA();
			if (!ret.contains(f)) {
				ret.add(f);
			}
		}

		return ret;
	}

	public void destroyContacts(Body body) {
		StableContact c;
		ArrayList<Integer> removeList = new ArrayList<Integer>();
		for (int i = 0; i < mContacts.size(); i++) {
			c = mContacts.get(i);
			if (c.getFixtureA().getBody() == body || c.getFixtureB().getBody() == body) {
				removeList.add(i);
			}
		}
		for (int i = removeList.size() - 1; i >= 0; i--) {
			mContacts.remove((int) removeList.get(i));
		}
	}

	/**
	 * Returns Bodies from a set of contacts, considering only Fixture B for
	 * each contact
	 * 
	 * @param contacts
	 *            The contacts
	 * @return The bodies being touched
	 */
	public static Iterable<Body> getBodies(Iterable<StableContact> contacts) {
		ArrayList<Body> ret = new ArrayList<Body>();

		for (StableContact c : contacts) {
			Body b = c.getFixtureB().getBody();
			if (!ret.contains(b)) {
				ret.add(b);
			}
		}

		return ret;
	}

	/**
	 * Returns Actors from a set of contacts, considering only Fixture B for
	 * each contact
	 * 
	 * @param contacts
	 *            The contacts
	 * @return The actors being touched
	 */
	public static Iterable<Actor> getActors(Iterable<StableContact> contacts) {
		ArrayList<Actor> ret = new ArrayList<Actor>();

		for (StableContact c : contacts) {
			Object obj = c.getFixtureB().getBody().getUserData();
			if (obj instanceof Actor) {
				Actor a = (Actor) obj;
				if (!ret.contains(a)) {
					ret.add(a);
				}
			}
		}

		return ret;
	}
}
