package org.siggd;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.siggd.actor.Blob;
import org.siggd.actor.Teleport;
import org.siggd.view.MenuView;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.tablelayout.Cell;

public class MenuController implements InputProcessor, ControllerListener {
	private static final int FILTER_AMOUNT = 10;
	private static final int REPEATS = 10;
	private static final Set<Integer> NAV_KEYS = new HashSet<Integer>(Arrays.asList(new Integer[] {
			Input.Keys.A, Input.Keys.LEFT, Input.Keys.D, Input.Keys.RIGHT, Input.Keys.W,
			Input.Keys.UP, Input.Keys.S, Input.Keys.DOWN }));

	private Table mTable;
	private Controller mController;
	private int mPlayerId;
	private int mX, mY;
	private int mControllerFilter;
	private int mFilteredKey;
	private int mRepeats = 0;

	public boolean ignore; // > Flag whether to ignore any further inputs;

	private static float sLineWidth = 6f;

	/**
	 * Constructs a new MenuController, defaults selected index to 0
	 * 
	 * @param id
	 *            controller number
	 */
	public MenuController() {
		this(0);
	}

	/**
	 * Constructs a new MenuController for the specified controller starting at
	 * the specified index
	 * 
	 * @param id
	 *            controller number
	 * @param index
	 *            element index that starts selected
	 */
	public MenuController(int index) {
		mX = 0;
		mY = 0;
		mPlayerId = 0;
		Array<Controller> controllers = Controllers.getControllers();
		mController = controllers.size > 0 ? controllers.get(0) : null;
		mControllerFilter = 0;
		mFilteredKey = -42;
		mTable = null;
		ignore = false;
	}

	public void setIndex(int i) {
		int j = 0;
		Cell target = null;
		for (Cell c : mTable.getCells()) {
			if (j == i) {
				target = c;
				break;
			}
			j++;
		}
		if (target != null) {
			mX = target.getColumn();
			mY = target.getRow();
		}
	}

	public void selectFirstAvailable() {
		for (int i = 0; i < mTable.getCells().size(); i++) {
			setIndex(i);
			if (getCell(mX, mY) != null) {
				break;
			}
		}
	}

	public void selectFirstCheckedAvailable() {
		for (int i = 0; i < mTable.getCells().size(); i++) {
			setIndex(i);
			if (getTransCell(mX, mY) != null) {
				break;
			}
		}
	}

	public void setTable(Table t) {
		mTable = t;
		if (t != null) {
			for (Cell c : t.getCells()) {
				Actor a = (Actor) c.getWidget();
				if (a != null) {
					a.addListener(hoverListener);
				}
			}
			selectFirstAvailable();
		}
	}

	public void setController(Controller c) {
		mController = c;
	}

	public void setPlayerId(int id) {
		mPlayerId = id;
	}

	/**
	 * Polls the controller to update selection or fire change event
	 */
	public void update() {
		if (Game.get().getState() == Game.MENU && mTable != null) {
			if (mControllerFilter != 0) {
				mControllerFilter = (mControllerFilter + 1) % FILTER_AMOUNT;
			} else {
				boolean up = false;
				boolean down = false;
				boolean r = false;
				boolean l = false;
				if (mController != null) {
					/*
					 * if (mController.getButton(ControllerFilterAPI
					 * .getButtonFromFilteredId(mController,
					 * ControllerFilterAPI.BUTTON_A))) { enterDown = true; if
					 * (!mEnterDown) { ((Actor) getCell(mX, mY).getWidget())
					 * .fire(new ChangeEvent()); mControllerFilter = 0; } }
					 */

					float leftRight = mController
							.getAxis(ControllerFilterAPI.getAxisFromFilteredAxis(mController,
									ControllerFilterAPI.AXIS_LEFT_LR));
					float upDown = mController.getAxis(ControllerFilterAPI.getAxisFromFilteredAxis(
							mController, ControllerFilterAPI.AXIS_LEFT_UD));
					up = up || upDown < -0.5;
					down = down || upDown > 0.5;
					r = r || leftRight > 0.5;
					l = l || leftRight < -0.5;

					if (mController.getPov(0) == PovDirection.east)
						r = true;
					if (mController.getPov(0) == PovDirection.west)
						l = true;
					if (mController.getPov(0) == PovDirection.north)
						up = true;
					if (mController.getPov(0) == PovDirection.south)
						down = true;
				}

				if (NAV_KEYS.contains(mFilteredKey)) {
					if (Gdx.input.isKeyPressed(mFilteredKey)) {
						switch (mFilteredKey) {
						case Input.Keys.A:
						case Input.Keys.LEFT:
							l = true;
							break;
						case Input.Keys.D:
						case Input.Keys.RIGHT:
							r = true;
							break;
						case Input.Keys.W:
						case Input.Keys.UP:
							up = true;
							break;
						case Input.Keys.S:
						case Input.Keys.DOWN:
							down = true;
							break;
						}
					}
				}
				if (up || down || r || l) {
					mRepeats++;
				} else {
					mRepeats = -1;
				}
				if (mRepeats < REPEATS && mRepeats > 0) {
					up = false;
					down = false;
					l = false;
					r = false;
				}
				int deltaX = (l ? -1 : 0) + (r ? 1 : 0);
				int deltaY = (up ? -1 : 0) + (down ? 1 : 0);

				if (getCell(mX + deltaX, mY + deltaY) != null) {
					mX = mX + deltaX;
					mY = mY + deltaY;
					if (deltaX != 0 || deltaY != 0) {
						Game.get().playTickSound();
						mControllerFilter++;
					}
				}
			}
		}
	}

	/**
	 * Draws the selection box around the selected element
	 * 
	 * @param shapeRender
	 */
	public void draw(ShapeRenderer shapeRender) {
		if (mTable != null && !ignore) {
			Cell c = getCell(mX, mY);
			if (c != null) {
				Actor selected = (Actor) (c.getWidget());
				shapeRender.setColor(Blob.colors(mPlayerId));
				shapeRender.begin(ShapeType.Line);
				Vector2 topleft = new Vector2(mTable.getX() + selected.getX(), mTable.getY()
						+ selected.getY());
				Vector2 topright = new Vector2(mTable.getX() + selected.getX()
						+ selected.getWidth(), mTable.getY() + selected.getY());
				Vector2 bottomleft = new Vector2(mTable.getX() + selected.getX(), mTable.getY()
						+ selected.getY() + selected.getHeight());
				Vector2 bottomright = new Vector2(mTable.getX() + selected.getX()
						+ selected.getWidth(), mTable.getY() + selected.getY()
						+ selected.getHeight());

				float arcsize = selected.getWidth() / 7f;
				Vector2 lt = topleft.cpy().add(0, arcsize);
				Vector2 lb = bottomleft.cpy().add(0, -arcsize);
				Vector2 rt = bottomright.cpy().add(0, -arcsize);
				Vector2 rb = topright.cpy().add(0, arcsize);
				Vector2 tl = topleft.cpy().add(arcsize, 0);
				Vector2 tr = topright.cpy().add(-arcsize, 0);
				Vector2 bl = bottomleft.cpy().add(arcsize, 0);
				Vector2 br = bottomright.cpy().add(-arcsize, 0);

				shapeRender.line(tl, tr); // top
				shapeRender.line(bl, br); // bottom
				shapeRender.line(lt, lb); // left
				shapeRender.line(rt, rb); // right

				Vector2 start = lb.cpy();
				Vector2 trav = new Vector2(0, arcsize * 1.74f);
				int iter = 16;
				for (int i = 0; i < iter; i++) {
					start.sub(trav.cpy().scl(.045f / iter));
					trav.rotate(-90f / iter);
					shapeRender.line(start.cpy(), start.add(trav.cpy().scl(1f / iter))); // right
				}

				start = br.cpy();
				trav = new Vector2(arcsize * 1.74f, 0);
				for (int i = 0; i < iter; i++) {
					start.sub(trav.cpy().scl(.045f / iter));
					trav.rotate(-90f / iter);
					shapeRender.line(start.cpy(), start.add(trav.cpy().scl(1f / iter))); // right
				}

				start = rb.cpy();
				trav = new Vector2(0, -arcsize * 1.74f);
				for (int i = 0; i < iter; i++) {
					start.sub(trav.cpy().scl(.045f / iter));
					trav.rotate(-90f / iter);
					shapeRender.line(start.cpy(), start.add(trav.cpy().scl(1f / iter))); // right
				}

				start = tl.cpy();
				trav = new Vector2(-arcsize * 1.74f, 0);
				for (int i = 0; i < iter; i++) {
					start.sub(trav.cpy().scl(.045f / iter));
					trav.rotate(-90f / iter);
					shapeRender.line(start.cpy(), start.add(trav.cpy().scl(1f / iter))); // right
				}

				GLCommon gl10 = Gdx.graphics.getGLCommon();
				gl10.glEnable(GL10.GL_BLEND);
				if (!Gdx.graphics.isGL20Available()) {
					gl10.glLineWidth(sLineWidth);
					shapeRender.end();
					gl10.glLineWidth(1);
				} else {
					Gdx.gl20.glLineWidth(sLineWidth);
					shapeRender.end();
					Gdx.gl20.glLineWidth(1);
				}

				gl10.glDisable(GL10.GL_BLEND);
			}
		}
	}

	public Color getCurColor() {
		return Blob.colors(mPlayerId);
	}

	private Cell getCell(int x, int y) {
		if (mTable != null) {
			for (Cell c : mTable.getCells()) {
				if (c.getColumn() == x && c.getRow() == y) {
					Actor cellActor = (Actor) (c.getWidget());
					if (cellActor instanceof Button
							&& (((Button) cellActor).isDisabled() || !((Button) cellActor)
									.isVisible())) {
						return null;
					} else {
						return c;
					}
				}
			}
		}
		return null;
	}

	private Cell getTransCell(int x, int y) {
		if (mTable != null) {
			for (Cell c : mTable.getCells()) {
				if (c.getColumn() == x && c.getRow() == y) {
					Actor cellActor = (Actor) (c.getWidget());
					if (cellActor instanceof Button
							&& (((Button) cellActor).isDisabled()
									|| !((Button) cellActor).isVisible() || !((Button) cellActor)
										.isTransform())) {
						return null;
					} else {
						return c;
					}
				}
			}
		}
		return null;
	}

	private final InputListener hoverListener = new InputListener() {
		public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
			if (fromActor instanceof ImageButton && ((ImageButton) fromActor).isDisabled()) {
				return;
			} else if (event.getListenerActor() instanceof ImageButton
					&& ((ImageButton) event.getListenerActor()).isDisabled()) {
				return;
			}
			Cell selected = mTable.getCell(event.getListenerActor());
			if (selected != null && (mX != selected.getColumn() || mY != selected.getRow())) {
				Actor cellActor = (Actor) (selected.getWidget());
				if (cellActor instanceof Button
						&& (((Button) cellActor).isDisabled() || !((Button) cellActor).isVisible())) {
					// nothing don't navigate
				} else {
					Game.get().playTickSound();
					mX = selected.getColumn();
					mY = selected.getRow();
				}
			}
		};
	};

	private void handleEscape() {
		if (Game.get().getState() == Game.PLAY) {
			String[] skips = new String[4];
			skips[0] = "opening";
			skips[1] = "opening_med";
			skips[2] = "opening_hard";
			skips[3] = "closing";
			for (int i = 0; i < skips.length; i++) {
				if (skips[i].equals(Game.get().getLevel().getAssetKey())) {
					for (org.siggd.actor.Actor a : Game.get().getLevel().getActors()) {
						if (a instanceof Teleport) {
							Teleport t = (Teleport) a;
							t.changeLevel();
							return;
						}
					}
					return;
				}
			}
			Game.get().setPaused(true);
			Game.get().setState(Game.MENU);
			Game.get().getMenuView().setMenu(MenuView.PAUSE);
			mControllerFilter = 0;
		} else if (Game.get().getState() == Game.MENU) {
			if (MenuView.PAUSE.equals(Game.get().getMenuView().getCurrentMenu())) {
				Game.get().setState(Game.PLAY);
			} else if (MenuView.LEVELS.equals(Game.get().getMenuView().getCurrentMenu())) {
				Game.get().getMenuView().setMenu(MenuView.MAIN);
				mControllerFilter = 0;
			} else if (MenuView.CONTROLLER.equals(Game.get().getMenuView().getCurrentMenu())) {
				Game.get().getMenuView().setMenu(MenuView.MAIN);
			} else if (MenuView.CUSTOMIZE.equals(Game.get().getMenuView().getCurrentMenu())) {
				boolean inRace = Game.get().getMenuView().mSelectedLevel.equals("gen");
				// Customize Menu
				// deactivate any players that may have joined
				Game.get().deactivatePlayers();
				Game.get().setLevel("earth");
				Game.get().getLevel().killFade();
				Game.get().getMenuView().setMenu(inRace ? MenuView.MAIN : MenuView.LEVELS);
				mControllerFilter = 0;
			}
		}

	}

	private void fakePause(Controller c) {
		if (Game.get().getState() == Game.PLAY) {
			Game.get().setPaused(true);
			Game.get().setState(Game.MENU);
			Game.get().getMenuView().setMenu(MenuView.FAKE_PAUSE);
			Game.get().getMenuView().setFakePauseController(c);
			mPlayerId = Game.get().getPlayer(c).id;
		} else if (Game.get().getState() == Game.MENU
				&& MenuView.FAKE_PAUSE.equals(Game.get().getMenuView().getCurrentMenu())) {
			Game.get().setState(Game.PLAY);
			mPlayerId = Game.get().getPlayer(mController).id;
		}
	}

	private boolean controllerPermission(Controller c, int button) {
		if (ignore) {
			return false;
		}
		int gameState = Game.get().getState();
		MenuView menuView = Game.get().getMenuView();
		if (gameState == Game.MENU && MenuView.FAKE_PAUSE.equals(menuView.getCurrentMenu())
				&& c == menuView.getFakePauseController()) {
			return true;
		}
		if (button == ControllerFilterAPI.BUTTON_START) {
			if (gameState == Game.PLAY
					|| (gameState == Game.MENU && MenuView.PAUSE.equals(menuView.getCurrentMenu()))) {
				return true;
			}
		} else if (c == mController) {
			return true;
		}
		return false;
	}

	@Override
	public boolean buttonDown(Controller c, int button) {
		int realButton = ControllerFilterAPI.getFilteredId(c, button);
		MenuView menuView = Game.get().getMenuView();
		if (controllerPermission(c, realButton)) {
			switch (realButton) {
			case ControllerFilterAPI.BUTTON_B:
				if (Game.get().getState() == Game.MENU && c == mController) {
					if (!MenuView.CUSTOMIZE.equals(Game.get().getMenuView().getCurrentMenu()))
						Game.get().playNomSound();
					handleEscape();
				} else if (MenuView.FAKE_PAUSE.equals(menuView.getCurrentMenu())
						&& menuView.getFakePauseController() == c) {
					Game.get().playNomSound();
					Player p = Game.get().getPlayer(c);
					if (p != null && p.active) {
						fakePause(c);
					}
				}
				break;
			case ControllerFilterAPI.BUTTON_START:
				Game.get().playNomSound();
				if (!MenuView.CUSTOMIZE.equals(Game.get().getMenuView().getCurrentMenu())
						&& c == mController) {
					handleEscape();
				} else if (c != mController) {
					Player p = Game.get().getPlayer(c);
					if (p != null && p.active) {
						fakePause(c);
					}
				}
				break;
			case ControllerFilterAPI.BUTTON_A:
				if (Game.get().getState() == Game.MENU) {
					if (!MenuView.CUSTOMIZE.equals(Game.get().getMenuView().getCurrentMenu()))
						Game.get().playNomSound();

					if (MenuView.FAKE_PAUSE.equals(menuView.getCurrentMenu())
							&& menuView.getFakePauseController() == c) {
						Player p = Game.get().getPlayer(c);
						if (p != null && p.active) {
							fakePause(c);
						}
					} else if (!MenuView.FAKE_PAUSE.equals(menuView.getCurrentMenu())) {
						Cell cell = getCell(mX, mY);
						if (cell != null && cell.getWidget() != null) {
							((Actor) cell.getWidget()).fire(new ChangeEvent());
							mControllerFilter = 0;
						}
					}
				}
				break;
			}
		}
		return false;
	}

	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {
		case Input.Keys.A:
		case Input.Keys.LEFT:
			if (getCell(mX - 1, mY) != null) {
				if (Game.get().getState() == Game.MENU
						&& !MenuView.CUSTOMIZE.equals(Game.get().getMenuView().getCurrentMenu()))
					Game.get().playTickSound();
				mX--;
			}
			mControllerFilter = 1;
			mRepeats = 0;
			break;
		case Input.Keys.D:
		case Input.Keys.RIGHT:
			if (getCell(mX + 1, mY) != null) {
				if (Game.get().getState() == Game.MENU
						&& !MenuView.CUSTOMIZE.equals(Game.get().getMenuView().getCurrentMenu()))
					Game.get().playTickSound();
				mX++;
			}
			mControllerFilter = 1;
			mRepeats = 0;
			break;
		case Input.Keys.W:
		case Input.Keys.UP:
			if (getCell(mX, mY - 1) != null) {
				if (Game.get().getState() == Game.MENU
						&& !MenuView.CUSTOMIZE.equals(Game.get().getMenuView().getCurrentMenu()))
					Game.get().playTickSound();
				mY--;
			}
			mControllerFilter = 1;
			mRepeats = 0;
			break;
		case Input.Keys.S:
		case Input.Keys.DOWN:
			if (getCell(mX, mY + 1) != null) {
				if (Game.get().getState() == Game.MENU
						&& !MenuView.CUSTOMIZE.equals(Game.get().getMenuView().getCurrentMenu()))
					Game.get().playTickSound();
				mY++;
			}
			mControllerFilter = 1;
			mRepeats = 0;
			break;

		case Input.Keys.ENTER:
		case Input.Keys.SPACE:
			if (Game.get().getState() != Game.PLAY) {
				if (mTable != null) {
					Actor a = (Actor) getCell(mX, mY).getWidget();
					if (a != null) {
						a.fire(new ChangeEvent());
						Game.get().playNomSound();
					}
				}
			}
			break;
		case Input.Keys.ESCAPE:
			handleEscape();
			Game.get().playNomSound();
			break;
		case Input.Keys.F12:
			Game.get().saveScreenshot();
			break;
		}
		mFilteredKey = keycode;
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

	@Override
	public boolean accelerometerMoved(Controller arg0, int arg1, Vector3 arg2) {
		return false;
	}

	@Override
	public boolean axisMoved(Controller c, int arg1, float arg2) {
		return false;
	}

	@Override
	public boolean buttonUp(Controller c, int arg1) {
		return false;
	}

	@Override
	public void connected(Controller c) {
	}

	@Override
	public void disconnected(Controller c) {
	}

	@Override
	public boolean povMoved(Controller c, int arg1, PovDirection arg2) {
		return false;
	}

	@Override
	public boolean xSliderMoved(Controller c, int arg1, boolean arg2) {
		return false;
	}

	@Override
	public boolean ySliderMoved(Controller c, int arg1, boolean arg2) {
		return false;
	}
}
