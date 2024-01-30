package com.demolution.game.entitis;

import java.util.ArrayList;
import java.util.List;

public class ListOfMoves {
    public static List<Move> moves = new ArrayList<>();

    static {
        // Hier fügst du deine Moves hinzu
        moves.add(new Move("Tackle", 5, null));
        moves.add(new Move("Move1", 10, ListOfStatusEffects.statusEffects.get(0))); // Assuming index 0 is DAMAGE_OVER_TIME
        moves.add(new Move("Move2", 15, ListOfStatusEffects.statusEffects.get(1))); // Assuming index 1 is INCREASE_ATTACK
        moves.add(new Move("Move3", 8, ListOfStatusEffects.statusEffects.get(2))); // Assuming index 2 is DECREASE_DEFENSE
        // ...
    }
}
