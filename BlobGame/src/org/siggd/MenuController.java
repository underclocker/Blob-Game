package org.siggd;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.siggd.actor.Blob;
import org.siggd.view.MenuView;
import org.siggd.ControllerFilterAPI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.esotericsoftware.tablelayout.Cell;

public class MenuController implements InputProcessor, ControllerListener {
	private static int FILTER_AMOUNT = 7;
	private static int KEY_FILTER_AMOUNT = 1;
	private static final Set<Integer> NAV_KEYS = new HashSet<Integer>(
			Arrays.asList(new Integer[] { Input.Keys.A, Input.Keys.LEFT,
					Input.Keys.D, Input.Keys.RIGHT, Input.Keys.W,
					Input.Keys.UP, Input.Keys.S, Input.Keys.DOWN }));

	private Table mTable;
	private Controller mController;
	private int mPlayerId;
	private int mX, mY;
	private int mControllerFilter;
	private int mKeyFilter;
	private int mFilteredKey;
	private boolean mEscDown = false;
	private boolean mEnterDown = false;
	private static float sLineWidth = 3;

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
		mController = Controllers.getControllers().get(0);
		mControllerFilter = 0;
		mFilteredKey = -42;
		mTable = null;
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

	public void setTable(Table t) {
		mTable = t;
		if (t != null) {
			for (Cell c : t.getCells()) {
				Actor a = (Actor) c.getWidget();
				if (a != null) {
					a.addListener(hoverListener);
				}
			}
		}
	}
	public void setController(Controller c){
		mController = c;
	}
	public void setPlayerId(int id){
		mPlayerId = id;
	}
	/**
	 * Polls the controller to update selection or fire change event
	 */
	public void update() {
		boolean enterDown = false;
		if (Game.get().getState() == Game.MENU && mTable != null) {
			if (mControllerFilter != 0) {
				mControllerFilter = (mControllerFilter + 1) % FILTER_AMOUNT;
			} else {
				boolean up = false;
				boolean down = false;
				boolean r = false;
				boolean l = false;
				if (mController != null) {
					if (mController.getButton(ControllerFilterAPI
							.getButtonFromFilteredId(mController,
									ControllerFilterAPI.BUTTON_A))) {
						enterDown = true;
						if (!mEnterDown) {
							((Actor) getCell(mX, mY).getWidget())
									.fire(new ChangeEvent());
							mControllerFilter = 0;
						}
					}

					float leftRight = mController.getAxis(ControllerFilterAPI
							.getAxisFromFilteredAxis(mController,
									ControllerFilterAPI.AXIS_LEFT_LR));
					float upDown = mController.getAxis(ControllerFilterAPI
							.getAxisFromFilteredAxis(mController,
									ControllerFilterAPI.AXIS_LEFT_UD));
					up = up || upDown < -0.5;
					down = down || upDown > 0.5;
					r = r || leftRight > 0.5;
					l = l || leftRight < -0.5;
				}
				if (NAV_KEYS.contains(mFilteredKey)) {
					if (mKeyFilter > KEY_FILTER_AMOUNT
							&& Gdx.input.isKeyPressed(mFilteredKey)) {
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
					} else {
						mKeyFilter++;
					}
				}
				int deltaX = (l ? -1 : 0) + (r ? 1 : 0);
				int deltaY = (up ? -1 : 0) + (down ? 1 : 0);

				if (getCell(mX + deltaX, mY + deltaY) != null) {
					mX = mX + deltaX;
					mY = mY + deltaY;
					mControllerFilter++;
				}
			}
		}
		mEnterDown = enterDown;
	}

	/**
	 * Draws the selection box around the selected element
	 * 
	 * @param shapeRender
	 */
	public void draw(ShapeRenderer shapeRender) {
		if (mTable != null) {
			Cell c = getCell(mX, mY);
			if (c != null) {
				Actor selected = (Actor) (c.getWidget());
				shapeRender.setColor(Blob.COLORS[mPlayerId]);
				GLCommon gl = Gdx.graphics.getGLCommon();
				shapeRender.begin(ShapeType.Line);
				shapeRender.box(mTable.getX() + selected.getX(), mTable.getY()
						+ selected.getY(), 0, selected.getWidth(),
						selected.getHeight(), 0);
				gl.glLineWidth(sLineWidth);
				shapeRender.end();
				gl.glLineWidth(1);
			}
		}
	}

	private Cell getCell(int x, int y) {
		if (mTable != null) {
			for (Cell c : mTable.getCells()) {
				if (c.getColumn() == x && c.getRow() == y) {
					Actor cellActor = (Actor) (c.getWidget());
					if (cellActor instanceof ImageButton
							&& ((ImageButton) cellActor).isDisabled()) {
						return null;
					} else {
						return c;
					}
				}
			}
		}
		// System.out.println("CELL " + x + "," + y + " not found");
		return null;
	}

	private final InputListener hoverListener = new InputListener() {
		public void enter(InputEvent event, float x, float y, int pointer,
				Actor fromActor) {
			if (fromActor instanceof ImageButton
					&& ((ImageButton) fromActor).isDisabled()) {
				return;
			} else if (event.getListenerActor() instanceof ImageButton
					&& ((ImageButton) event.getListenerActor()).isDisabled()) {
				return;
			}
			Cell selected = mTable.getCell(event.getListenerActor());
			if (selected != null) {
				mX = selected.getColumn();
				mY = selected.getRow();
			}
		};
	};

	private void handleEscape() {
		if (Game.get().getState() == Game.PLAY) {
			Game.get().setPaused(true);
			Game.get().setState(Game.MENU);
			Game.get().getMenuView().setMenu(MenuView.PAUSE);
			mControllerFilter = 0;
		} else if (Game.get().getState() == Game.MENU) {
			if (MenuView.PAUSE
					.equals(Game.get().getMenuView().getCurrentMenu())) {
				Game.get().setState(Game.PLAY);
			} else if (MenuView.LEVELS.equals(Game.get().getMenuView()
					.getCurrentMenu())) {
				//Game.get().getMenuView().setMenu(MenuView.MAIN);
				mControllerFilter = 0;
			} else if (MenuView.CUSTOMIZE.equals(Game.get().getMenuView()
					.getCurrentMenu())) {
				// Customize Menu
				// deactivate any players that may have joined
				Game.get().deactivatePlayers();
				Game.get().setLevel("earth");
				Game.get().getMenuView().setMenu(MenuView.LEVELS);
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
		} else if (Game.get().getState() == Game.MENU
				&& MenuView.FAKE_PAUSE.equals(Game.get().getMenuView()
						.getCurrentMenu())) {
			Game.get().setState(Game.PLAY);
		}
	}

	private boolean controllerPermission(Controller c, int button) {
		int gameState = Game.get().getState();
		MenuView menuView = Game.get().getMenuView();
		if (button == ControllerFilterAPI.BUTTON_START) {
			if (gameState == Game.PLAY
					|| (gameState == Game.MENU && MenuView.PAUSE.equals(menuView.getCurrentMenu()))) {
				return true;
			}else if(gameState == Game.MENU && MenuView.FAKE_PAUSE.equals(menuView.getCurrentMenu()) && c == menuView.getFakePauseController()){
				return true;
			}
		} else if (c == mController) {
			return true;
		}
		return false;
	}

	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {
		case Input.Keys.A:
		case Input.Keys.LEFT:
			if (getCell(mX - 1, mY) != null)
				mX--;
			break;
		case Input.Keys.D:
		case Input.Keys.RIGHT:
			if (getCell(mX + 1, mY) != null)
				mX++;
			break;
		case Input.Keys.W:
		case Input.Keys.UP:
			if (getCell(mX, mY - 1) != null)
				mY--;
			break;
		case Input.Keys.S:
		case Input.Keys.DOWN:
			if (getCell(mX, mY + 1) != null)
				mY++;
			break;
		case Input.Keys.ENTER:
		case Input.Keys.SPACE:
			if (mTable != null) {
				Actor a = (Actor) getCell(mX, mY).getWidget();
				if (a != null) {
					a.fire(new ChangeEvent());
				}
			}
			break;
		case Input.Keys.ESCAPE:
			handleEscape();
			break;
		}
		mFilteredKey = keycode;
		mKeyFilter = 0;
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
	public boolean buttonDown(Controller c, int button) {
		int realButton = ControllerFilterAPI.getButtonFromFilteredId(c, button);
		if (controllerPermission(c, realButton)) {
			switch (realButton) {
			case ControllerFilterAPI.BUTTON_B:
				handleEscape();
				break;
			case ControllerFilterAPI.BUTTON_START:
				if (!MenuView.CUSTOMIZE.equals(Game.get().getMenuView()
						.getCurrentMenu())
						&& c == mController) {
					handleEscape();
				} else if (c != mController) {
					fakePause(c);
				}
				break;

			}
		}
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
