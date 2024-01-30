package com.demolution.game.entitis;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Array;

public class Creature {


    private int maxHealth;
    private int currentHealth;
    private int attackDamage;
    private int arcane;
    private int defense;
    private int arcaneResistance;
    private int experience;
    private int level;
    private  String spriteSheetPath;

    private boolean tamed;

    private String name; // Name der Kreatur
    private Move[] moves; // Moves der Kreatur (angenommen, dass Move eine eigene Klasse ist)

    public Creature(String name, String spriteSheetPath) {
        this.name = name;
        this.spriteSheetPath = spriteSheetPath;
        this.tamed = false;
        this.moves = new Move[4]; // Platz für 4 Moves
        initializeStats();
    }

    private void initializeStats() {
        // Setze Anfangswerte und Level 1 für die Stats
        this.maxHealth = calculateStatAtLevel(10, 2.5f);
        this.currentHealth = maxHealth;
        this.attackDamage = calculateStatAtLevel(5, 2.0f);
        this.arcane = calculateStatAtLevel(3, 1.5f);
        this.defense = calculateStatAtLevel(4, 2.0f);
        this.arcaneResistance = calculateStatAtLevel(2, 1.0f);
        this.experience = 0;
        this.level = 1;
    }

    private int calculateStatAtLevel(int baseValue, float scalingFactor) {
        // Eine einfache Beispiel-Skalierungsfunktion
        return (int) (baseValue + (level - 1) * scalingFactor);
    }

    public void levelUp() {
        // Erhöhe Level und aktualisiere Stats entsprechend
        level++;
        maxHealth = calculateStatAtLevel(10, 2.5f);
        currentHealth = maxHealth;
        attackDamage = calculateStatAtLevel(5, 2.0f);
        arcane = calculateStatAtLevel(3, 1.5f);
        defense = calculateStatAtLevel(4, 2.0f);
        arcaneResistance = calculateStatAtLevel(2, 1.0f);
    }

    public void takeDamage(int damage) {
        // Reduziere die Gesundheit basierend auf dem erlittenen Schaden
        currentHealth -= damage;
        if (currentHealth < 0) {
            currentHealth = 0;
        }
    }

    // Weitere Methoden für das Fangen und Zähmen der Kreatur können hinzugefügt werden

    public boolean isTamed() {
        return tamed;
    }

    public void setTamed(boolean tamed) {
        this.tamed = tamed;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public int getAttackDamage() {
        return attackDamage;
    }

    public int getArcane() {
        return arcane;
    }

    public int getDefense() {
        return defense;
    }

    public int getArcaneResistance() {
        return arcaneResistance;
    }

    public int getExperience() {
        return experience;
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public Move[] getMoves() {
        return moves;
    }

    public void setMoves(Move[] moves) {
        this.moves = moves;
    }
    public String getSpriteSheetPath() {
        return spriteSheetPath;
    }

    public void setSpriteSheetPath(String spriteSheetPath) {
        this.spriteSheetPath = spriteSheetPath;
    }

    public void loadFromPreferences(Preferences preferences, String prefix) {
        this.name = preferences.getString(prefix + "_name", "DefaultName");
        this.tamed = preferences.getBoolean(prefix + "_tamed", false);
        this.maxHealth = preferences.getInteger(prefix + "_maxHealth", 0);
        this.currentHealth = preferences.getInteger(prefix + "_currentHealth", 0);
        this.attackDamage = preferences.getInteger(prefix + "_attackDamage", 0);
        this.arcane = preferences.getInteger(prefix + "_arcane", 0);
        this.defense = preferences.getInteger(prefix + "_defense", 0);
        this.arcaneResistance = preferences.getInteger(prefix + "_arcaneResistance", 0);
        this.experience = preferences.getInteger(prefix + "_experience", 0);
        this.level = preferences.getInteger(prefix + "_level", 1);

        // Lade Moves
        this.moves = new Move[4];
        for (int i = 0; i < moves.length; i++) {
            this.moves[i] = new Move(
                    preferences.getString(prefix + "_move_" + i + "_name", "DefaultMove"),
                    preferences.getInteger(prefix + "_move_" + i + "_damage", 0),
                    loadStatusEffectFromPreferences(preferences, prefix, i)
            );
        }
    }

    private StatusEffect loadStatusEffectFromPreferences(Preferences preferences, String prefix, int moveIndex) {
        String typeString = preferences.getString(prefix + "_move_" + moveIndex + "_statusEffectType", "");
        StatusEffect.Type type = StatusEffect.Type.NONE;; // Standardwert, wenn der Wert nicht gefunden wird

        try {
            type = StatusEffect.Type.valueOf(typeString);
        } catch (IllegalArgumentException e) {
            // Wenn der Enum-Wert nicht gefunden wird, wird der Standardwert beibehalten
        }

        int duration = preferences.getInteger(prefix + "_move_" + moveIndex + "_statusEffectDuration", 0);
        int value = preferences.getInteger(prefix + "_move_" + moveIndex + "_statusEffectValue", 0);
        boolean targetSelf = preferences.getBoolean(prefix + "_move_" + moveIndex + "_statusEffectTargetSelfe", false);

        return new StatusEffect(type, duration, value, targetSelf);
    }


    public void saveToPreferences(Preferences preferences, String prefix) {
        preferences.putString(prefix + "_name", this.name);
        preferences.putBoolean(prefix + "_tamed", this.tamed);
        preferences.putInteger(prefix + "_maxHealth", this.maxHealth);
        preferences.putInteger(prefix + "_currentHealth", this.currentHealth);
        preferences.putInteger(prefix + "_attackDamage", this.attackDamage);
        preferences.putInteger(prefix + "_arcane", this.arcane);
        preferences.putInteger(prefix + "_defense", this.defense);
        preferences.putInteger(prefix + "_arcaneResistance", this.arcaneResistance);
        preferences.putInteger(prefix + "_experience", this.experience);
        preferences.putInteger(prefix + "_level", this.level);



        // Speichere Moves
        for (int i = 0; i < moves.length; i++) {
            if (moves[i] != null) {
                preferences.putString(prefix + "_move_" + i + "_name", this.moves[i].getName());
                preferences.putInteger(prefix + "_move_" + i + "_damage", this.moves[i].getDamage());

                // Prüfe, ob StatusEffect vorhanden ist
                StatusEffect statusEffect = this.moves[i].getStatusEffect();
                if (statusEffect != null) {
                    preferences.putString(prefix + "_move_" + i + "_statusEffectType", statusEffect.getType().name());
                    preferences.putInteger(prefix + "_move_" + i + "_statusEffectDuration", statusEffect.getDuration());
                    preferences.putInteger(prefix + "_move_" + i + "_statusEffectValue", statusEffect.getValue());
                    preferences.putBoolean(prefix + "_move_" + i + "_statusEffectTargetSelfe", statusEffect.getTarget());
                } else {
                    // Wenn StatusEffect null ist, setze Standardwerte oder lasse die Einträge leer
                    preferences.putString(prefix + "_move_" + i + "_statusEffectType", "");
                    preferences.putInteger(prefix + "_move_" + i + "_statusEffectDuration", 0);
                    preferences.putInteger(prefix + "_move_" + i + "_statusEffectValue", 0);
                    preferences.putBoolean(prefix + "_move_" + i + "_statusEffectTargetSelfe", false);
                }
            }
        }

    }


}
