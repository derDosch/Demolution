package com.demolution.game.entitis;

public class Move {

    private String name;
    private int damage;
    private StatusEffect statusEffect;

    public Move(String name, int damage, StatusEffect statusEffect) {
        this.name = name;
        this.damage = damage;
        this.statusEffect = statusEffect;
    }

    public String getName() {
        return name;
    }

    public int getDamage() {
        return damage;
    }

    public StatusEffect getStatusEffect() {
        return statusEffect;
    }
}
