package com.demolution.game.entitis;

import java.util.ArrayList;
import java.util.List;

public class ListOfStatusEffects {

    public static List<StatusEffect> statusEffects = new ArrayList<>();

    static {
        // Hier fügst du deine StatusEffekte hinzu
        statusEffects.add(new StatusEffect(StatusEffect.Type.DAMAGE_OVER_TIME, 2, 5, false));//Burn oder Poisen effekte
        statusEffects.add(new StatusEffect(StatusEffect.Type.INCREASE_ATTACK, 2, 0, true));// selfe Buff
        statusEffects.add(new StatusEffect(StatusEffect.Type.DECREASE_DEFENSE, 2, 0, false));//Debuff
        // ...
    }
}
