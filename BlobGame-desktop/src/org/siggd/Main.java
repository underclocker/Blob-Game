package org.siggd;

import java.awt.Dimension;
import java.awt.Toolkit;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Blob Game";
		cfg.width = 1280;
		cfg.height = 720;
		cfg.useGL20 = true;
		cfg.depth = 0;
		cfg.vSyncEnabled = true;
		// Related to harrison's computer speedups.  Don't commit this enabled.
		// cfg.useCPUSynch = true;
		if (Game.RELEASE) {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			cfg.width = (int) screenSize.getWidth();
			cfg.height = (int) screenSize.getHeight();
			cfg.fullscreen = true;
		}
		cfg.addIcon("data/gfx/icon16.png", FileType.Internal);
		cfg.addIcon("data/gfx/icon32.png", FileType.Internal);
		cfg.addIcon("data/gfx/icon128.png", FileType.Internal);
		new LwjglApplication(Game.get(), cfg);
	}
}
