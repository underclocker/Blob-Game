package org.siggd;

import java.io.File;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.files.FileHandle;

public class ControllerFilterAPI {

	public final static int BUTTON_A = 0;
	public final static int BUTTON_B = 1;
	public final static int BUTTON_X = 2;
	public final static int BUTTON_Y = 3;
	public final static int BUTTON_LB = 4;
	public final static int BUTTON_RB = 5;
	public final static int BUTTON_SELECT = 6;
	public final static int BUTTON_START = 7;
	public final static int BUTTON_LS = 8;
	public final static int BUTTON_RS = 9;
	public final static int NOBUTTON = -1;

	public final static int AXIS_LEFT_UD = 0;
	public final static int AXIS_RIGHT_LR = 3;
	public final static int AXIS_RIGHT_UD = 2;
	public final static int AXIS_LEFT_LR = 1;
	public final static int AXIS_TRIGGER = 4;
	public final static int AXIS_NO = -1;

	private static JSONObject json;
	private static String CUSTOM_BINDINGS = ".BlobGame/BlobBindings.json";

	public static void load() throws JSONException {
		FileHandle handle = (new InternalFileHandleResolver())
				.resolve("data/ctrl/controllers.json");
		String jsonstr = handle.readString();
		json = new JSONObject(jsonstr);
		// DebugOutput.info(new Object(), "This is the JSON " + jsonstr);
		loadCustomBindings();
	}
	/**
	 * Loads custom bindings from ".BlobGame/BlobBindings.json"
	 * @throws JSONException
	 */
	public static  void loadCustomBindings() throws JSONException{
		File f = new File(Gdx.files.getExternalStoragePath() + CUSTOM_BINDINGS);
		FileHandle bindingsFile;
		if (f.exists()) {
			String osName = System.getProperty("os.name");
			JSONObject osSpecificBindings = json.getJSONObject(osName);
			bindingsFile = Gdx.files.external(CUSTOM_BINDINGS);
			String bindingsJson = bindingsFile.readString();
			if(!bindingsJson.isEmpty()){
				JSONObject customBindings = new JSONObject(bindingsJson);
				Iterator<String> t = customBindings.keys();
				while(t.hasNext()){
					String controller = t.next();
					JSONObject controllerBindings = customBindings.getJSONObject(controller);
					osSpecificBindings.put(controller, controllerBindings);
				}
			}
		}
	}
	public static void saveCustomBinding(String controllerName, JSONObject bindings) throws JSONException{
		File f = new File(Gdx.files.getExternalStoragePath() + CUSTOM_BINDINGS);
		FileHandle bindingsFile;
		JSONObject customBindings;
		if (f.exists()) {
			bindingsFile = Gdx.files.external(CUSTOM_BINDINGS);
			String bindingsJson = bindingsFile.readString();
			if(!bindingsJson.isEmpty()){
				customBindings = new JSONObject(bindingsJson);
			}else{
				customBindings = new JSONObject();
			}
		}else{
			bindingsFile = new FileHandle(f);
			customBindings = new JSONObject();
		}
		customBindings.put(controllerName, bindings);
		System.out.println("SAVING: "+bindings.toString());
		bindingsFile.writeString(customBindings.toString(), false);
		loadCustomBindings();
	}
/**
 * use this to take a real button ID to the XBOX equivelant
 * @param c
 * @param id
 * @return
 */
	public static int getFilteredId(Controller c, int id) {
		String osName = System.getProperty("os.name");
		String ctrlName = c.getName();

		JSONObject os = null;
		JSONObject ctrl = null;
		JSONObject buttons = null;
		try {
			os = json.getJSONObject(osName);
			ctrl = os.getJSONObject(ctrlName);
			buttons = ctrl.getJSONObject("Buttons");

			String[] bNames = JSONObject.getNames(buttons);

			for (int i = 0; i < buttons.length(); i++) {
				if (checkMatch(bNames[i], buttons, id)) {
					if (bNames[i].equals("BUTTON_A"))
						return BUTTON_A;
					if (bNames[i].equals("BUTTON_X"))
						return BUTTON_X;
					if (bNames[i].equals("BUTTON_Y"))
						return BUTTON_Y;
					if (bNames[i].equals("BUTTON_B"))
						return BUTTON_B;
					if (bNames[i].equals("BUTTON_START"))
						return BUTTON_START;
					if (bNames[i].equals("BUTTON_SELECT"))
						return BUTTON_SELECT;
					if (bNames[i].equals("BUTTON_LB"))
						return BUTTON_LB;
					if (bNames[i].equals("BUTTON_RB"))
						return BUTTON_RB;
					if (bNames[i].equals("BUTTON_LS"))
						return BUTTON_LS;
					if (bNames[i].equals("BUTTON_RS"))
						return BUTTON_RS;
				}
			}
		} catch (Exception e) {
			DebugOutput.fine(new Object(),
					"WARNING! CONTROLLER NOT RECOGNIZED. NAME: " + c.getName());
		}

		return id;
	}

	public static boolean checkMatch(String str, JSONObject buttons, int id) {
		int registeredID = -1;
		try {
			registeredID = buttons.getInt(str);
		} catch (JSONException e) {
		}

		if (registeredID == id) {
			return true;
		}

		return false;
	}

	public static int getButtonFromFilteredId(Controller c, int id) {

		String osName = System.getProperty("os.name");
		String ctrlName = c.getName();

		JSONObject os = null;
		JSONObject ctrl = null;
		JSONObject buttons = null;
		try {
			os = json.getJSONObject(osName);
			ctrl = os.getJSONObject(ctrlName);
			buttons = ctrl.getJSONObject("Buttons");

			if (id == BUTTON_A)
				return buttons.getInt("BUTTON_A");
			;
			if (id == BUTTON_B)
				return buttons.getInt("BUTTON_B");
			;
			if (id == BUTTON_X)
				return buttons.getInt("BUTTON_X");
			;
			if (id == BUTTON_Y)
				return buttons.getInt("BUTTON_Y");
			;
			if (id == BUTTON_LB)
				return buttons.getInt("BUTTON_LB");
			;
			if (id == BUTTON_RB)
				return buttons.getInt("BUTTON_RB");
			;
			if (id == BUTTON_START)
				return buttons.getInt("BUTTON_START");
			if (id == BUTTON_SELECT)
				return buttons.getInt("BUTTON_SELECT");
			if (id == BUTTON_LS)
				return buttons.getInt("BUTTON_LS");
			if (id == BUTTON_RS)
				return buttons.getInt("BUTTON_RS");
		} catch (Exception e) {

			DebugOutput.fine(new Object(),
					"WARNING! CONTROLLER NOT RECOGNIZED. NAME: " + c.getName());

		}
		return id;

	}

	public static int getFilteredAxis(Controller c, int id) {
		String osName = System.getProperty("os.name");
		String ctrlName = c.getName();

		JSONObject os = null;
		JSONObject ctrl = null;
		JSONObject axes = null;
		try {
			os = json.getJSONObject(osName);
			ctrl = os.getJSONObject(ctrlName);
			axes = ctrl.getJSONObject("Axes");

			String[] bNames = JSONObject.getNames(axes);
			for (int i = 0; i < axes.length(); i++) {
				if (checkMatch(bNames[i], axes, id)) {
					if (bNames[i].equals("AXIS_LEFT_LR"))
						return AXIS_LEFT_LR;
					if (bNames[i].equals("AXIS_LEFT_UD"))
						return AXIS_LEFT_UD;
					if (bNames[i].equals("AXIS_RIGHT_LR"))
						return AXIS_RIGHT_LR;
					if (bNames[i].equals("AXIS_RIGHT_UD"))
						return AXIS_RIGHT_UD;
					if (bNames[i].equals("AXIS_TRIGGERS"))
						return AXIS_LEFT_LR;

				}
			}
		} catch (Exception e) {

			DebugOutput.fine(new Object(),
					"WARNING! CONTROLLER NOT RECOGNIZED. NAME: " + c.getName());

		}
		return id;
	}

	public static int getAxisFromFilteredAxis(Controller c, int id) {
		String osName = System.getProperty("os.name");
		String ctrlName = c.getName();

		JSONObject os = null;
		JSONObject ctrl = null;
		JSONObject axes = null;
		try {
			os = json.getJSONObject(osName);
			ctrl = os.getJSONObject(ctrlName);
			axes = ctrl.getJSONObject("Axes");

			if (id == AXIS_LEFT_UD)
				return axes.getInt("AXIS_LEFT_UD");
			if (id == AXIS_LEFT_LR)
				return axes.getInt("AXIS_LEFT_LR");
			if (id == AXIS_RIGHT_UD)
				return axes.getInt("AXIS_RIGHT_UD");
			if (id == AXIS_RIGHT_LR)
				return axes.getInt("AXIS_RIGHT_LR");
			if (id == AXIS_TRIGGER)
				return axes.getInt("AXIS_TRIGGER");
		} catch (Exception e) {

			DebugOutput.fine(new Object(),
					"WARNING! CONTROLLER NOT RECOGNIZED. NAME: " + c.getName());

		}
		return id;
	}

}
