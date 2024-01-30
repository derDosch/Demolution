package com.demolution.game;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;


// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		System.out.println("Starting application...");
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setTitle("Demolution");
		config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());


		new Lwjgl3Application(new Demolution(), config);
	}
}
