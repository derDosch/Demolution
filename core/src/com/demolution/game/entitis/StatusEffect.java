package com.demolution.game.entitis;

public class StatusEffect {

    public enum Type {
        DAMAGE_OVER_TIME,
        INCREASE_ATTACK,
        DECREASE_DEFENSE,
        NONE,
        // Weitere Typen je nach Bedarf
    }

    private Type type;
    private int duration; // Dauer des Effekts in Runden
    private int value; // Wert des Effekts, z.B. Schaden pro Runde
    private boolean targetSelfe;

    public StatusEffect(Type type, int duration, int value, boolean targetSelfe) {
        this.type = type;
        this.duration = duration;
        this.value = value;
        this.targetSelfe = targetSelfe;
    }

    public Type getType() {
        return type;
    }

    public int getDuration() {
        return duration;
    }

    public int getValue() {
        return value;
    }

    public boolean getTarget(){return targetSelfe;}
}
