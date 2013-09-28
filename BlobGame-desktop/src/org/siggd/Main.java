package org.siggd;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.files.FileHandle;

public class Main {
	private final static String mPrefFileName = ".BlobGame/BlobPref.json";
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
			saveToPref("useGL20", (Integer) 1);
			saveToPref("vSyncEnabled", (Integer) 1);
			// Related to harrison's computer speedups.
			saveToPref("useCPUSynch", (Integer) 0);

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
			cfg.fullscreen = mPrefs.getInt("fullscreen") != 0;
			cfg.useGL20 = mPrefs.getInt("useGL20") != 0;
			cfg.vSyncEnabled = mPrefs.getInt("vSyncEnabled") != 0;
			//cfg.useCPUSynch = mPrefs.getInt("useCPUSynch") != 0;
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
		new LwjglApplication(Game.get(), cfg);
	}

	private static void saveToPref(String key, Object value) {
		try {
			mPrefs.put(key, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	
}
