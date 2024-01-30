// VirtualJoystickInputAdapter.java
package com.demolution.game.UI;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.demolution.game.screens.Town;

public class VirtualJoystickInputAdapter extends InputListener implements InputProcessor {
    private Town playScreen;  // Hier auf die richtige Klasse verweisen, falls erforderlich
    private Touchpad touchpad;  // Neue Referenz auf das Touchpad
    private float radius;  // Radius des Joysticks
    private boolean isJoystickActive;

    public VirtualJoystickInputAdapter(Town playScreen, Touchpad touchpad) {
        this.playScreen = playScreen;
        this.touchpad = touchpad;
        this.radius = touchpad.getWidth() / 2f;

    }


    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        // Verarbeiten Sie den Beginn der Berührung
        isJoystickActive = true;
        return true;
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        // Verarbeiten Sie das Ende der Berührung
        isJoystickActive = false;
    }

    /*
    @Override
    public void touchDragged(InputEvent event, float x, float y, int pointer) {
        // Hier wird die Bewegung basierend auf dem Winkel des Joysticks gesteuert
        float deltaX = x - touchpad.getWidth() / 2f;  // Verschiebung des Joystick-Knopfs in x-Richtung
        float deltaY = y - touchpad.getHeight() / 2f;  // Verschiebung des Joystick-Knopfs in y-Richtung


        // Überprüfe, ob der Joystick innerhalb des Radius verschoben wurde
        if (isJoystickActive && Math.sqrt(deltaX * deltaX + deltaY * deltaY) <= radius) {
            // Berechnung des Winkels in Radians
            float angleRad = MathUtils.atan2(deltaY, deltaX);

            // Steuere die Bewegung basierend auf dem Winkel
            playScreen.getPlayer().handleJoystickAngle(angleRad);

        }
    }

     */
    @Override
    public void touchDragged(InputEvent event, float x, float y, int pointer) {
        // Direkte Umrechnung der Joystick-Bewegung in Weltkoordinaten
        float deltaX = x - touchpad.getWidth() / 2f;
        float deltaY = y - touchpad.getHeight() / 2f;

        // Berechnung des Winkels in Radians
        float angleRad = MathUtils.atan2(deltaY, deltaX);

        // Setze die Bewegungsrichtung basierend auf dem Joystick-Winkel
        playScreen.getPlayer().handleJoystickAngle(angleRad);
    }

    // Methode zum Abrufen des aktuellen Joystick-Winkels
    public float getCurrentJoystickAngle() {
        return radius;
    }

    public boolean isJoystickActive() {
        return isJoystickActive;
    }


    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {return false;}

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }


}
