package org.siggd;

import org.siggd.Player.ControlType;
import org.siggd.actor.Blob;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.esotericsoftware.tablelayout.Cell;

public class CustomizeController {
	private Player mPlayer;
	private Table mTable;
	private int x;
	private int y;

	public CustomizeController(Player p) {
		mPlayer = p;
		x = 0;
		y = 0;
	}

	public void setTable(Table t) {
		mTable = t;
	}

	public void draw(ShapeRenderer shapeRender) {
		if (mTable != null) {
			Cell c = getSelectedCell(x, y);
			if (c != null) {
				Actor selected = (Actor) (c.getWidget());
				if (selected != null) {
					shapeRender.setColor(Blob.colors(mPlayer.id%Blob.COLORS.length));
					GLCommon gl = Gdx.graphics.getGLCommon();
					shapeRender.begin(ShapeType.Line);
					shapeRender.box(mTable.getX()+selected.getX(), mTable.getY()+selected.getY(), 0, selected.getWidth(),
							selected.getHeight(), 0);
					gl.glLineWidth(3);
					shapeRender.end();
					gl.glLineWidth(1);
				}
			}
		}
	}
	
	public void update(){
		int deltaY = 0;
		int deltaX = 0;
		if(mPlayer.controltype == ControlType.Arrows){
			boolean up = Gdx.input.isKeyPressed(Input.Keys.UP);
			boolean down = Gdx.input.isKeyPressed(Input.Keys.DOWN);
			deltaY = (up ? -1 : 0) + (down ? 1 : 0);
			boolean r = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
			boolean l = Gdx.input.isKeyPressed(Input.Keys.LEFT);
			deltaX = (l ? -1 : 0) + (r ? 1 : 0);
		}else if(mPlayer.controltype == ControlType.WASD){
			boolean up = Gdx.input.isKeyPressed(Input.Keys.W);
			boolean down = Gdx.input.isKeyPressed(Input.Keys.S);
			deltaY = (up ? -1 : 0) + (down ? 1 : 0);
			boolean r = Gdx.input.isKeyPressed(Input.Keys.D);
			boolean l = Gdx.input.isKeyPressed(Input.Keys.A);
			deltaX = (l ? -1 : 0) + (r ? 1 : 0);
		}else{
			Controller c = mPlayer.controller;
			if(c!=null){
				float leftRight = c.getAxis(ControllerFilterAPI.getAxisFromFilteredAxis(
						c, ControllerFilterAPI.AXIS_LEFT_LR));
				float upDown = c.getAxis(ControllerFilterAPI.getAxisFromFilteredAxis(
						c, ControllerFilterAPI.AXIS_LEFT_UD));
				boolean r = leftRight > 0.5;
				boolean l = leftRight < -0.5;
				boolean up = upDown < -0.5;
				boolean down = upDown > 0.5;
				deltaX = (l ? -1 : 0) + (r ? 1 : 0);
				deltaY = (up ? -1 : 0) + (down ? 1 : 0);
			}
		}
		if(getSelectedCell(x+deltaX, y+deltaY)!=null){
			x = x+deltaX;
			y = y+deltaY;
		}
	}
	
	public boolean isReady(){
		return x == 1 && y == 3; 
	}
	
	private Cell getSelectedCell(int x, int y){
		for (Cell c : mTable.getCells()) {
			if (c.getColumn() == x && c.getRow() == y) {
				return c;
			}
		}
		return null;
	}
}
