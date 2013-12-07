package org.siggd.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class SiggdImageButton {
	private static Texture GLOW;
	ImageButton mImageButton;
	Image mGlow;

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

		if (GLOW == null)
			GLOW = new Texture("data/gfx/glow.png");
		mGlow = new Image(GLOW);
	}

	public SiggdImageButton(String down) {
		this(down, down, null);
	}

	public ImageButton getButton() {
		return mImageButton;
	}

	public Image getGlow() {
		return mGlow;
	}
}
