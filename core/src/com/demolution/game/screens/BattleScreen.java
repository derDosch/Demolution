package com.demolution.game.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.demolution.game.entitis.NPC;
import com.demolution.game.entitis.Player;

// BattleScreen.java
public class BattleScreen implements Screen {
    private Game game;
    private NPC daemonNPC;
    private Player player;
    private boolean battleEnded = false;

    private Music battleMusic;

    //TODO delete later (Testzwecke)
    private float elapsedBattleTime = 0;
    private static final float BATTLE_DURATION = 5.0f; // Dauer des Kampfes in Sekunden

    public BattleScreen(Game game, NPC daemonNPC, Player player) {
        this.game = game;
        this.daemonNPC = daemonNPC;
        this.player = player;
    }

    @Override
    public void show() {
        // Initialisierung für den Kampf
        battleMusic = Gdx.audio.newMusic(Gdx.files.internal("sound/background_music/kampf_music.mp3"));
        battleMusic.setLooping(true);
        battleMusic.setVolume(0.02f);
        battleMusic.play();
    }

    @Override
    public void render(float delta) {
        // Setzen Sie die Farbe des Hintergrunds auf Weiß
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Logik für den Kampf
        // ...

        // Überwache die verstrichene Zeit
        elapsedBattleTime += delta;

        // Überprüfen, ob die Kampfdauer erreicht wurde
        if (elapsedBattleTime >= BATTLE_DURATION) {
            // Setze battleEnded auf true
            battleEnded = true;
        }

        if (battleEnded) {
            // Wechsel zurück zum TownScreen
            game.setScreen(new Town(game));// TODO Aktuell sit der aufzurufende Screen Hardcodiert Bessere lösung wäre ein Screen Manager
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        if (battleMusic != null) {
            battleMusic.stop();
            battleMusic.dispose();
        }
    }

    @Override
    public void dispose() {
        hide();
        if (battleMusic != null) {
            battleMusic.stop();
            battleMusic.dispose();
        }
    }

}
