package com.demolution.game;

import com.badlogic.gdx.Game;
import com.demolution.game.screens.Town;


public class Demolution extends Game {

	
	@Override
	public void create () {

		setScreen(new Town(this));
	}


	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		super.dispose();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}

	@Override
	public void pause() {
		super.pause();
	}

	@Override
	public void resume() {
		super.resume();
	}
}
