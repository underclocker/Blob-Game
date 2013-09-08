package org.siggd.editor;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.actor.Actor;
import org.siggd.actor.Background;
import org.siggd.actor.Blob;
import org.siggd.editor.ActorPanel.Action;
import org.siggd.view.Drawable;
import org.siggd.view.LevelView;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * This class contains the overarching control functionality needed to edit a
 * map
 * 
 * @author mysterymath
 * 
 */
public class Editor extends JFrame implements InputProcessor, ChangeListener {
	private static final float SCROLL_SPEED = 3;
	private JTabbedPane mTabs; // /< The tabs
	private ActorPanel mActorPanel; // /< The actor tab
	private ParamPanel mParamPanel; // /< The param tab
	public LayerPanel mLayerPanel; // / < The Layer tab
	private WorldPanel mWorldPanel; // / < The World tab
	private SysPanel mSysPanel; // /< The sys tab
	private Vector3 lastDiff; // /< Diff between last clicked actor's pos and
								// click location, corrected for projection
	private Actor mSelected; // /< The currently selected actor (null if none).
								// Note its different uses depending on whether
								// in add/move/remove mode
	private Actor mLastAdded; // /< Actor most recently added to the level
	private SelectMoveCommand selectMove; // /< A SelectMoveCommand that is in
											// progress

	private Stack<Command> mUndo; // /< Stack of performed actions
	private Stack<Command> mRedo; // /< Stack of undone actions
	public String selectPoint = "data/selectpoint.png"; // /< used to draw the
														// select/move image

	private String mBody; // /< The current body to add, or null if what we're
							// adding isn't a body

	private boolean mLeftBtnIsDown = false;
	private boolean mRightBtnIsDown = false;
	private boolean ctrlIsPressed = false;
	private boolean mRunOnRelease = false;

	private Vector2 mMouseDownPosWorld;
	private Vector2 mMouseMovePosScreen;

	private LinkedBlockingQueue<Runnable> mSwingQueue; // /< Queue for
														// operations that are
														// initiated on the
														// Swing thread

	/**
	 * Constructor
	 */
	public Editor() {
		// Create undo list
		mUndo = new Stack<Command>();
		mRedo = new Stack<Command>();
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		// Get screen size
		Rectangle scrSz = getGraphicsConfiguration().getBounds();

		// Set params
		setSize(600, 260);
		// setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setLocation(scrSz.width / 2 - 300, scrSz.height / 2 + 230);
		setTitle("Level Edit Tools");
		setAlwaysOnTop(true);
		// Create tab pane
		mTabs = new JTabbedPane(JTabbedPane.TOP);

		// Create tabs
		mActorPanel = new ActorPanel();
		mParamPanel = new ParamPanel();
		mLayerPanel = new LayerPanel();
		mWorldPanel = new WorldPanel();
		mSysPanel = new SysPanel();

		// Install tabs
		mTabs.add("Actor", mActorPanel);
		mTabs.add("Param", mParamPanel);
		mTabs.add("Layer", mLayerPanel);
		mTabs.add("World", mWorldPanel);

		mTabs.add("Sys", mSysPanel);
		mTabs.addChangeListener(this); // Set the change listener to the Editor
										// itself

		// Install tab pane
		add(mTabs);

		// Set visible
		setVisible(true);
		mActorPanel.mSearchField.requestFocusInWindow(); // set textfield focus
															// on startup

		// Setup click listener on Level View
		Game.get().getInput().addProcessor(this);
		// nullify
		mLastAdded = null;

		// Create message queue
		mSwingQueue = new LinkedBlockingQueue<Runnable>();
	}

	// ACCESSORS and MUTATORS

	/**
	 * Returns the currently selected actor
	 * 
	 * @return The currently selected actor, or null if none selected
	 */
	public Actor getSelectedActor() {
		return mSelected;
	}

	// InputProcessor INTERFACE:
	@Override
	public boolean keyDown(int keycode) {
		if (Game.get().getState() == Game.EDIT) {
			Vector2 pan = new Vector2(0, 0);
			if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D) {
				pan = new Vector2(SCROLL_SPEED, 0);
			} else if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A) {
				pan = new Vector2(-SCROLL_SPEED, 0);
			} else if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
				pan = new Vector2(0, SCROLL_SPEED);
			} else if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
				pan = new Vector2(0, -SCROLL_SPEED);
			} else if (keycode == Input.Keys.F2) {
				mActorPanel.setCurrentAction(ActorPanel.Action.ADD);
			} else if (keycode == Input.Keys.F3) {
				mActorPanel.setCurrentAction(ActorPanel.Action.SELECTMOVE);
			} else if (keycode == Input.Keys.F4) {
				mActorPanel.setCurrentAction(ActorPanel.Action.REMOVE);
			} else if (keycode == Input.Keys.CONTROL_LEFT || keycode == Input.Keys.CONTROL_RIGHT) {
				ctrlIsPressed = true;
			}

			LevelView lv = Game.get().getLevelView();
			Vector2 pos = lv.getCameraPosition();
			pos.x += pan.x;
			pos.y += pan.y;
			lv.setCameraPosition(pos);
		}

		if (keycode == Input.Keys.F5) {
			mSysPanel.toggleState();
		} else if (keycode == Input.Keys.F6) {
			Game.get().getLevelView().toggleFullscreen();
		} else if (keycode == Input.Keys.F7) {
			Game.get().getLevelView().toggleDebugRender();
		}

		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		if (keycode == Input.Keys.CONTROL_LEFT || keycode == Input.Keys.CONTROL_RIGHT) {
			ctrlIsPressed = false;
		}
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		LevelView lv = Game.get().getLevelView();
		Vector2 pos = new Vector2(x, y);
		lv.unproject(pos);

		if (button == 1) {
			mRightBtnIsDown = true;
			mMouseDownPosWorld = new Vector2(pos.x, pos.y);
		}
		if (button == 0) {
			mLeftBtnIsDown = true;
		}
		if (Game.get().getState() == Game.PLAY) {
			mRunOnRelease = true;
			Game.get().setState(Game.EDIT);
		}
		// TODO: read the 1 and 0 from somewhere that stores the mouse button
		// mappings
		if (Game.get().getState() == Game.EDIT) {
			if (button == 0) {
				if (getActorEditState() == ActorPanel.Action.SELECTMOVE) {
					mSelected = isActorUnder(pos.x, pos.y);
					if (mSelected != null) {
						if (ctrlIsPressed) {
							performEdit(new CopyCommand(mSelected));
						} else if (selectMove == null) { // An Actor is not in
															// the process of
															// being moved
							selectMove = new SelectMoveCommand(mSelected, new Vector2(
									mSelected.getX(), mSelected.getY())); // Create
																			// command
																			// for
																			// the
																			// currently
																			// selected
																			// actor
						} else { // An Actor is in the process of being moved
							if (mSelected.getId() != selectMove.getVictim().getId()) { // Currently
																						// selected
																						// Actor
																						// does
																						// not
																						// match
																						// the
																						// victim
																						// of
																						// the
																						// command
								finish(selectMove); // finish the command
								selectMove = new SelectMoveCommand(mSelected, new Vector2(
										mSelected.getX(), mSelected.getY())); // Create
																				// command
																				// for
																				// the
																				// newly
																				// selected
																				// actor
							} else {
								// Currently selected actor is still the one
								// that is under the command
							}
						}
						lastDiff = new Vector3(pos.x - mSelected.getX(), pos.y - mSelected.getY(),
								0); // difference between click loc and actor
									// pos
					} else { // Nothing selected, a command may have finished
						if (selectMove != null) { // There is an unfinished
													// command.
							finish(selectMove);
							selectMove = null; // the command is finished
						}
					}
				}
				// Used to detect if a point is inside a blob
				/*
				 * ArrayList<Blob> blobs = Game.get().getLevel().getBlobs();
				 * System.out.print("In Blobs: "); for (Blob blob : blobs){ if
				 * (blob.isPointInBlob(pos,0)){
				 * System.out.print(" "+blob.getId()); } } System.out.println();
				 */
			}
		}
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {

		LevelView lv = Game.get().getLevelView();
		Vector2 pos = new Vector2(x, y);
		lv.unproject(pos);

		if (button == 1) {
			mRightBtnIsDown = false;
		} else if (button == 0) {
			mLeftBtnIsDown = false;
			// Action depends on editor state
			if (getActorEditState() == ActorPanel.Action.REMOVE) {
				// Find actor under cursor
				Actor hover = isActorUnder(pos.x, pos.y);
				if (hover != null) {
					if (mSelected.getId() == hover.getId()) {
						// perform the RemoveCommand
						performEdit(new RemoveCommand(mSelected));
						mSelected = null;
					}
				}
				mSelected = hover;
			}
			if (getActorEditState() == ActorPanel.Action.SELECTMOVE) {
				if (selectMove != null) { // if stop dragging, finish
											// SelectMoveCommand
					finish(selectMove);
					selectMove = null;
				}
				mRedo.clear(); // an action has been done, clear redo until an
								// undo happens.
			}
			if (getActorEditState() == ActorPanel.Action.ADD) {
				Class actorClass = mActorPanel.getCurrentActor();
				if (actorClass != null) {
					AddCommand leCommand = new AddCommand(actorClass, pos.x, pos.y);
					performEdit(leCommand);
					if (mBody != null) {
						Actor a = leCommand.getActor();

						// Set it if it is a background
						if (a instanceof Background) {
							Background b = (Background) a;
							b.setInEditor();
							b.setProp("Body", mBody);
						}
						mLastAdded = a;
					}
				}
			}
		}

		if (mRunOnRelease && !mRightBtnIsDown && !mLeftBtnIsDown) {
			Game.get().setState(Game.PLAY);
			mRunOnRelease = false;
		}

		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		LevelView lv = Game.get().getLevelView();
		Vector2 pos = new Vector2(x, y);
		lv.unproject(pos);
		mWorldPanel.setCoordinateValues(pos.x, pos.y);
		if (Game.get().getState() == Game.EDIT) {
			if (mRightBtnIsDown) {
				Vector2 camPos = lv.getCameraPosition();
				camPos.x += (mMouseDownPosWorld.x - pos.x);
				camPos.y += (mMouseDownPosWorld.y - pos.y);
				lv.setCameraPosition(camPos);
			} else if (mLeftBtnIsDown) {
				if (mSelected != null) {
					if (getActorEditState() == ActorPanel.Action.SELECTMOVE) { // Is
																				// select/move
																				// mode
																				// active?
						if (selectMove == null) {
							selectMove = new SelectMoveCommand(mSelected, new Vector2(
									mSelected.getX(), mSelected.getY())); // Create
																			// command
																			// for
																			// the
																			// currently
																			// selected
																			// actor
						}
						mSelected.setProp("X", (pos.x - lastDiff.x));
						mSelected.setProp("Y", (pos.y - lastDiff.y));
					}
					mParamPanel.setActor(mSelected); // update the Actor's
														// position in
														// parampanel
				}
			}
		}
		return false;
	}

	@Override
	public boolean mouseMoved(int x, int y) {
		Vector2 pos = new Vector2(x, y);
		mMouseMovePosScreen = pos.cpy();
		LevelView lv = Game.get().getLevelView();
		lv.unproject(pos);

		mWorldPanel.setCoordinateValues(pos.x, pos.y);

		if (getActorEditState() == ActorPanel.Action.REMOVE) {
			mSelected = isActorUnder(pos.x, pos.y);
		}
		if (getActorEditState() == ActorPanel.Action.ADD) {
			Actor a = Game.get().getActorEnum().getActor(mActorPanel.getCurrentActor());
			if (a != null) {
				// Handle adding bodies
				if (mActorPanel.getCurrentActor() == Background.class && mBody != null) {
					Background b = (Background) a;
					b.setInEditor();
					b.setProp("Body", mBody);
				}
				float fX = pos.x;
				float fY = pos.y;
				a.setProp("X", fX);
				a.setProp("Y", fY);
				mSelected = a;
			}
		}

		// don't change selected outside conditional; it will mess up selection
		// assigned in touchDown()
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		if (Game.get().getState() == Game.EDIT) {
			LevelView lv = Game.get().getLevelView();
			Vector2 camPos = mMouseMovePosScreen.cpy();
			lv.unproject(camPos);
			camPos.sub(lv.getCameraPosition());
			float zoom = amount / 8f;
			camPos.scl(-zoom);
			lv.zoom(zoom);
			lv.setCameraPosition(camPos.add(lv.getCameraPosition()));
		}
		return false;
	}

	/**
	 * 
	 * @param a
	 *            Actor to be selected, null will deselect all.
	 */
	public void setSelected(Actor a) {
		if (selectMove != null) { // There is a SelectMoveCommand currently in
									// progress
			finish(selectMove);
			selectMove = null;
		}
		mSelected = a;
	}

	public void update() {
		mWorldPanel.setFramerate();
		if (getActorEditState() == ActorPanel.Action.SELECTMOVE) {
			mParamPanel.setActor(mSelected);// update param panel
		}

		// If there's something for us to do on the message queue, do it
		Runnable r = mSwingQueue.poll();
		if (r != null) {
			r.run();
		}
	}

	public void updateWorldProperties() {
		mWorldPanel.setWorldProperties();
	}

	/**
	 * 
	 * @return Whether Actors are currently in select/move, add, or remove
	 *         state.
	 */
	public Action getActorEditState() {
		return mActorPanel.getCurrentAction();
	}

	/**
	 * Perform an edit command
	 */
	public void performEdit(Command c) {
		c.doit();
		if (c instanceof CopyCommand) {
			mSelected = ((CopyCommand) c).getActor();
		}
		mUndo.push(c);
		mRedo.clear();
	}

	/**
	 * Undo an edit command
	 * 
	 * @returns false if there was nothing to undo
	 */
	public boolean undo() {
		if (selectMove != null) { // there is a SelectMoveCommand in progress.
			finish(selectMove);
			selectMove = null; // command is finished
		}
		if (mUndo.isEmpty()) {
			return false;
		}
		Command c = mUndo.pop();
		mRedo.push(c);

		c.undo();

		return true;
	}

	/**
	 * Redo an edit command
	 * 
	 * @returns false if there was nothing to redo
	 */
	public boolean redo() {
		if (mRedo.isEmpty()) {
			return false;
		}

		Command c = mRedo.pop();
		mUndo.push(c);

		c.doit();

		return true;
	}

	/**
	 * Detects the topmost actor under a screen position
	 * 
	 * @param x
	 *            unprojected x
	 * @param y
	 *            unprojected y
	 * @return Actor under the pointer, null if none
	 */
	public Actor isActorUnder(float x, float y) {

		// Accumulated information about best actor found
		Actor under = null;
		int pressedLayer = -1;

		// Search through all the actors
		for (Actor a : Game.get().getLevel().getActors()) {
			float aX = a.getX();
			float aY = a.getY();
			int layer = Convert.getInt(a.getProp("Layer"));
			if (mLayerPanel.noShow.contains(layer)) {
				// don't select actors not being shown
				continue;
			}

			float maxDist = .27f;

			float dx = x - aX;
			float dy = y - aY;
			float dist = dx * dx + dy * dy;

			// Determine if the click was on an actor (for now, actor images'
			// lower left corners are at actor pos
			if (dist < maxDist * maxDist) {
				if (under == null || layer > pressedLayer) {
					under = a;
					pressedLayer = layer;
				}
			}
		}

		// Return the best found actor
		return under;
	}

	/**
	 * Finishes a command.
	 * 
	 * @param c
	 *            Command to be finished.
	 * @return True if command finished, false otherwise
	 */
	public boolean finish(Command c) {
		if (c instanceof SelectMoveCommand) {
			SelectMoveCommand smc = (SelectMoveCommand) c;
			Actor a = Game.get().getLevel().getActorById(smc.getVictim().getId()); // get
																					// the
																					// victim
																					// of
																					// the
																					// command
			smc.setEnd(new Vector2(a.getX(), a.getY())); // Set the end of the
															// move
			mUndo.push(smc);
			return true;
		}
		return false;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// only called by the tab panel, therefore no conditional to distinguish
		// the source
		// tabs: 0:actors, 1:params, 2:layer, 3: world, 4:cyst
		switch (mTabs.getSelectedIndex()) {
		case 0:
			mActorPanel.mSearchField.requestFocusInWindow();
			break;
		case 1:
			if (mLastAdded != null) {
				mSelected = mLastAdded;
				mLastAdded = null;
			}
			mActorPanel.setCurrentAction(ActorPanel.Action.SELECTMOVE); // Switch
																		// to
																		// select
																		// mode
			break;
		case 2:
			mLayerPanel.lesLayers = Game.get().getLevelView().auditLayers(); // Update
																				// lesLayers
			mLayerPanel.updateLayers();
			break;
		}
	}

	public void setBody(String body) {
		mBody = body;
	}

	/**
	 * Get message queue used to ensure everything runs on Game thread
	 * 
	 * @return The message queue
	 */
	public LinkedBlockingQueue<Runnable> getQueue() {
		return mSwingQueue;
	}
}
