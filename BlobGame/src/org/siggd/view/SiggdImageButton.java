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

	public SiggdImageButton(String down, String disabled, String metaData) {
		Texture texture2 = new Texture(Gdx.files.internal(down));
		Texture texture3 = new Texture(Gdx.files.internal(disabled));
		TextureRegion imageDown = new TextureRegion(texture2);
		TextureRegion imageDisabled = new TextureRegion(texture3);
		TextureRegionDrawable drawableDown = new TextureRegionDrawable(imageDown);
		TextureRegionDrawable drawableDisabled = new TextureRegionDrawable(imageDisabled);
		ImageButtonStyle imageButtonStyle = new ImageButtonStyle(drawableDown, drawableDown,
				drawableDown, drawableDown, drawableDown, drawableDown);
		imageButtonStyle.imageDisabled = drawableDisabled;
		mImageButton = new ImageButton(imageButtonStyle);
		mImageButton.setName(metaData);
	}

	public SiggdImageButton(String down) {
		this(down, down, null);
	}

	public ImageButton getButton() {
		return mImageButton;
	}
}
