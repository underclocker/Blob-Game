package org.siggd;

import java.util.ArrayList;
import java.util.Arrays;

import org.siggd.actor.Blob;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.Color;

public class Player {
	// Wraps around a Controllable and a Controller to do input, see also:
	// PlayerListener
	public enum ControlType {
		Controller, WASD, Arrows
	}

	public Controllable mActor;
	public int id;
	public ControlType controltype;
	public Controller controller;
	public boolean mLeader;
	public boolean active;
	public boolean mustache;
	public static ArrayList<Color> AVAILABLE_COLORS;
	static {
		AVAILABLE_COLORS = new ArrayList<Color>(Arrays.asList(Blob.COLORS));
	}

	public Player(int i) {
		id = i;
		mLeader = false;
		// Players start active
		active = true;
		mustache = Math.random() < (Level.COMPLETE ? .1f : .001f);
		Blob.COLORS[i] = AVAILABLE_COLORS.remove(0);
	}

	public void swapColor() {
		for (int i = 0; i < Blob.COLORS.length; i++) {
			//System.out.print(Blob.COLORS[i] + " ");
		}
		//System.out.println();
		Color curc = Blob.COLORS[id].cpy();
		AVAILABLE_COLORS.add(curc);
		Color nextc = AVAILABLE_COLORS.remove(0);
		for (int i = 0; i < Blob.COLORS.length; i++) {
			if (Blob.COLORS[i].equals(nextc)) {
				Blob.COLORS[i] = curc;
			}
		}
		Blob.COLORS[id] = nextc;
		for (int i = 0; i < Blob.COLORS.length; i++) {
			//System.out.print(Blob.COLORS[i]+ " ");
		}
		//System.out.println();
		//System.out.println("---");
	}
}
