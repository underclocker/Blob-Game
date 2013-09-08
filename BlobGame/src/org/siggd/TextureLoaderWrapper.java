package org.siggd;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;

public class TextureLoaderWrapper extends TextureLoader {
	public TextureLoaderWrapper(FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public void loadAsync(AssetManager manager, String fileName, TextureParameter parameter) {
		TextureParameter mTextureParameter = new TextureParameter();
		if (parameter != null) {
			// copy old vars
			mTextureParameter.format = parameter.format;
			mTextureParameter.genMipMaps = parameter.genMipMaps;
			mTextureParameter.texture = parameter.texture;
			mTextureParameter.textureData = parameter.textureData;
		}

		// our settings
		mTextureParameter.magFilter = TextureFilter.Linear;
		mTextureParameter.minFilter = TextureFilter.Linear;
		mTextureParameter.wrapU = Texture.TextureWrap.MirroredRepeat;
		mTextureParameter.wrapV = Texture.TextureWrap.MirroredRepeat;
		super.loadAsync(manager, fileName, mTextureParameter);
	}

	@Override
	public Texture loadSync(AssetManager manager, String fileName, TextureParameter parameter) {
		TextureParameter mTextureParameter = new TextureParameter();
		if (parameter != null) {
			// copy old vars
			mTextureParameter.format = parameter.format;
			mTextureParameter.genMipMaps = parameter.genMipMaps;
			mTextureParameter.texture = parameter.texture;
			mTextureParameter.textureData = parameter.textureData;
		}

		// our settings
		mTextureParameter.magFilter = TextureFilter.Linear;
		mTextureParameter.minFilter = TextureFilter.Linear;
		mTextureParameter.wrapU = Texture.TextureWrap.MirroredRepeat;
		mTextureParameter.wrapV = Texture.TextureWrap.MirroredRepeat;
		return super.loadSync(manager, fileName, mTextureParameter);
	}
}
