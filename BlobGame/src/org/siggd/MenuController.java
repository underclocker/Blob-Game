package org.siggd;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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

public class MenuController {
	private static int FILTER_AMOUNT = 7;
	private static final boolean ROLLOVER = false;

	private Table mTable;
	private int mPlayerId;
	private Controller mController;
	private int x, y;
	private int mFilter;
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
		x = 0;
		y = 0;
		mFilter = 0;
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
			x = target.getColumn();
			y = target.getRow();
		}
	}

	public void setTable(Table t) {
		mTable = t;
	}

	/**
	 * Polls the controller to update selection or fire change event
	 */
	public void update() {
		boolean escDown = false;
		boolean enterDown = false;
		if (Game.get().getState() == Game.MENU && mTable != null) {
			if (mFilter != 0) {
				mFilter = (mFilter + 1) % FILTER_AMOUNT;
			} else {
				if (mController != null
						&& mController.getButton(ControllerFilterAPI
								.getButtonFromFilteredId(mController,
										ControllerFilterAPI.BUTTON_A))) {
					enterDown = true;
					if (!mEnterDown) {
						((Actor) getCell(x, y).getWidget())
								.fire(new ChangeEvent());
						mFilter = 0;
					}
				}
				if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
					enterDown = true;
					if (!mEnterDown) {
						((Actor) getCell(x, y).getWidget())
								.fire(new ChangeEvent());
						mFilter = 0;
					}
				}

				float leftRight = 0;
				float upDown = 0;

				int deltaY = 0;
				int deltaX = 0;

				if (mController != null) {
					leftRight = mController.getAxis(ControllerFilterAPI
							.getAxisFromFilteredAxis(mController,
									ControllerFilterAPI.AXIS_LEFT_LR));
					upDown = mController.getAxis(ControllerFilterAPI
							.getAxisFromFilteredAxis(mController,
									ControllerFilterAPI.AXIS_LEFT_UD));
				}

				boolean up = Gdx.input.isKeyPressed(Input.Keys.UP)
						|| Gdx.input.isKeyPressed(Input.Keys.W)
						|| mController != null ? upDown < -0.5 : false;
				boolean down = Gdx.input.isKeyPressed(Input.Keys.DOWN)
						|| Gdx.input.isKeyPressed(Input.Keys.S)
						|| mController != null ? upDown > 0.5 : false;
				boolean r = Gdx.input.isKeyPressed(Input.Keys.RIGHT)
						|| Gdx.input.isKeyPressed(Input.Keys.D)
						|| (mController != null ? leftRight > 0.5 : false);
				boolean l = Gdx.input.isKeyPressed(Input.Keys.LEFT)
						|| Gdx.input.isKeyPressed(Input.Keys.A)
						|| (mController != null ? leftRight < -0.5 : false);

				deltaX = (l ? -1 : 0) + (r ? 1 : 0);
				deltaY = (up ? -1 : 0) + (down ? 1 : 0);

				if (getCell(x + deltaX, y + deltaY) != null) {
					x = x + deltaX;
					y = y + deltaY;
					mFilter++;
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
						mFilter = 0;
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
					mFilter = 0;
				}
			}
			if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
				escDown = true;
				if (!mEscDown) {
					Game.get().setPaused(true);
					Game.get().setState(Game.MENU);
					Game.get().getMenuView().setMenu("Pause");
					mFilter = 0;
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
			Cell c = getCell(x, y);
			if (c != null) {
				Actor selected = (Actor) (c.getWidget());
				shapeRender.setColor(Color.GREEN);
				GLCommon gl = Gdx.graphics.getGLCommon();
				shapeRender.begin(ShapeType.Line);
				shapeRender.box(c.getWidgetX(), c.getWidgetY(), 0,
						c.getWidgetWidth(), c.getWidgetHeight(), 0);
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
			}
			Cell selected = mTable.getCell(event.getListenerActor());
			if (selected != null) {
				x = selected.getColumn();
				y = selected.getRow();
			}
		};
	};
}
