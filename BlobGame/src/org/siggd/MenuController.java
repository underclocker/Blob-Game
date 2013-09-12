package org.siggd;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.esotericsoftware.tablelayout.Cell;

public class MenuController implements InputProcessor {
	private static int FILTER_AMOUNT = 7;
	private static int KEY_FILTER_AMOUNT = 1;
	private static final boolean ROLLOVER = false;
	private static final Set<Integer> NAV_KEYS = new HashSet<Integer>(
			Arrays.asList(new Integer[] { Input.Keys.A, Input.Keys.LEFT,
					Input.Keys.D, Input.Keys.RIGHT, Input.Keys.W,
					Input.Keys.UP, Input.Keys.S, Input.Keys.DOWN }));

	private Table mTable;
	private int mPlayerId;
	private Controller mController;
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
	public MenuController(int id) {
		this(id, 0);
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
	public MenuController(int id, int index) {
		mPlayerId = id;
		if (id < Controllers.getControllers().size) {
			mController = Controllers.getControllers().get(id);
		} else {
			mController = null;
		}
		;
		mX = 0;
		mY = 0;
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

	/**
	 * Polls the controller to update selection or fire change event
	 */
	public void update() {
		boolean escDown = false;
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

			if (mController != null
					&& mController.getButton(ControllerFilterAPI
							.getButtonFromFilteredId(mController,
									ControllerFilterAPI.BUTTON_START))) {
				escDown = true;
				if (!mEscDown && Game.get().getPaused()) {
					if (!"earth".equals(Game.get().getLevel().getAssetKey())) {
						Game.get().setState(Game.PLAY);
					}
				}
			}
			if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
				escDown = true;
				if (!mEscDown) {
					if (Game.get().getPaused()
							&& !"earth".equals(Game.get().getLevel()
									.getAssetKey())) {
						Game.get().setState(Game.PLAY);
						mControllerFilter = 0;
					}
					if ("Levels".equals(Game.get().getMenuView()
							.getCurrentMenu())) {
						Game.get().getMenuView().setMenu("Main");
					}
				}
			}

		} else if (Game.get().getState() == Game.PLAY) {
			if (mController != null
					&& mController.getButton(ControllerFilterAPI
							.getButtonFromFilteredId(mController,
									ControllerFilterAPI.BUTTON_START))) {
				escDown = true;
				if (!mEscDown) {
					Game.get().setPaused(true);
					Game.get().setState(Game.MENU);
					Game.get().getMenuView().setMenu("Pause");
					mControllerFilter = 0;
				}
			}
			if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
				escDown = true;
				if (!mEscDown) {
					Game.get().setPaused(true);
					Game.get().setState(Game.MENU);
					Game.get().getMenuView().setMenu("Pause");
					mControllerFilter = 0;
				}
			}
		}
		mEnterDown = enterDown;
		mEscDown = escDown;
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
				shapeRender.setColor(Color.GREEN);
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

	public int getPlayerId() {
		return mPlayerId;
	}

	public void setPlayerId(int Id) {
		mPlayerId = Id;
		mController = Controllers.getControllers().get(Id);
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
		}
		mFilteredKey = keycode;
		mKeyFilter = 0;
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if (mTable == null)
			return false;
		if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
			Actor a = (Actor) getCell(mX, mY).getWidget();
			if (a != null) {
				a.fire(new ChangeEvent());
			}
		}
		mFilteredKey = keycode;
		if (NAV_KEYS.contains(keycode)) {
			// ignore key once again
			mFilteredKey = -255;
		}
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}
}
