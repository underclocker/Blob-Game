package org.siggd;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;
import org.siggd.platform.Reflector;
import org.siggd.platform.ReflectorImpl;
import org.siggd.view.LevelView;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.files.FileHandle;

public class Main {
	private final static String mPrefFileName = ".BlobGame/BlobPref.txt";
	private static JSONObject mPrefs;
	public static void main(String[] args) {

		LwjglFiles tempLwFiles = new LwjglFiles();

		File f = new File(tempLwFiles.getExternalStoragePath() + mPrefFileName);
		FileHandle handle;
		if (!f.exists()) {

			mPrefs = new JSONObject();
			handle = new FileHandle(f);

			// Set Default Prefs
			saveToPref("width", (Integer) 1280);
			saveToPref("height", (Integer) 720);
			saveToPref("fullscreen", (Integer) 1);
			saveToPref("vSyncEnabled", (Integer) 1);
			saveToPref("useLights", (Integer) 1);
			saveToPref("usePreload", (Integer) 1);
			saveToPref("useCalmColors", (Integer) 0);
			// Flush
			handle.writeString(mPrefs.toString(), false);
		} else {
			handle = tempLwFiles.external(mPrefFileName);
			String json = handle.readString();
			if ("".equals(json))
				json = "{}";
			try {
				mPrefs = new JSONObject(json);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Blob Game";
		try {
			cfg.width = mPrefs.getInt("width");
			cfg.height = mPrefs.getInt("height");
			cfg.useGL20 = true;
			cfg.fullscreen = mPrefs.getInt("fullscreen") != 0;
			cfg.vSyncEnabled = mPrefs.getInt("vSyncEnabled") != 0;
			LevelView.mUseLights = mPrefs.getInt("useLights") != 0;
			Game.PRELOAD = mPrefs.getInt("usePreload") != 0;
			Game.CALM = mPrefs.getInt("useCalmColors") != 0;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		cfg.depth = 0;
		if (!Game.RELEASE)
			cfg.fullscreen = false;
		if (cfg.fullscreen) {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			cfg.width = (int) screenSize.getWidth();
			cfg.height = (int) screenSize.getHeight();
		}
		cfg.addIcon("data/gfx/icon16.png", FileType.Internal);
		cfg.addIcon("data/gfx/icon32.png", FileType.Internal);
		cfg.addIcon("data/gfx/icon128.png", FileType.Internal);
		new LwjglApplication(new Game(new ReflectorImpl()), cfg);
	}

	private static void saveToPref(String key, Object value) {
		try {
			mPrefs.put(key, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
