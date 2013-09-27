package org.siggd;

import org.json.JSONException;
import org.json.JSONObject;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

public class LevelLoader extends AsynchronousAssetLoader<Level, LevelLoader.LevelParameter> {
	Level level;

	public LevelLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public void loadAsync(AssetManager manager, String fileName, FileHandle handle, LevelParameter parameter) {
		level = null;
		String json = handle.readString();
		level = new Level(fileName);
		try {
			level.load(new JSONObject(json));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Level loadSync(AssetManager manager, String fileName, FileHandle handle, LevelParameter parameter) {
		return level;
	}

	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle handle, LevelParameter parameter) {
		return null;
	}

	static public class LevelParameter extends AssetLoaderParameters<Level> {
	}
}
