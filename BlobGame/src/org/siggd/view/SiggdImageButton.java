package org.siggd.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class SiggdImageButton {
	ImageButton mImageButton;

	public SiggdImageButton(String up, String down, String disabled, String metaData) {
		Texture texture1 = new Texture(Gdx.files.internal(up));
		Texture texture2 = new Texture(Gdx.files.internal(down));
		Texture texture3 = new Texture(Gdx.files.internal(disabled));
		TextureRegion imageUp = new TextureRegion(texture1);
		TextureRegion imageDown = new TextureRegion(texture2);
		TextureRegion imageDisabled = new TextureRegion(texture3);
		TextureRegionDrawable drawableUp = new TextureRegionDrawable(imageUp);
		TextureRegionDrawable drawableDown = new TextureRegionDrawable(imageDown);
		TextureRegionDrawable drawableDisabled = new TextureRegionDrawable(imageDisabled);
		ImageButtonStyle imageButtonStyle = new ImageButtonStyle(drawableUp, drawableDown,
				drawableDown, drawableDown, drawableDown, drawableDown);
		imageButtonStyle.imageDisabled = drawableDisabled;
		mImageButton = new ImageButton(imageButtonStyle);
		mImageButton.setName(metaData);
	}

	public SiggdImageButton(String up, String down) {
		this(up, down, up, null);
	}

	public ImageButton getButton() {
		return mImageButton;
	}
}
