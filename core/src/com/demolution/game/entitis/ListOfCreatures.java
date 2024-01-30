package com.demolution.game.entitis;
import java.util.ArrayList;
import java.util.List;

public class ListOfCreatures {
    public static List<Creature> creatures = new ArrayList<>();

    static {
        // Hier fügst du deine Kreaturen hinzu
        creatures.add(new Creature("Lizzo", "Creature/Lizzo.png"));
        creatures.get(0).setMoves(new Move[]{ListOfMoves.moves.get(0)});
        creatures.get(0).setTamed(true);
        // ...
    }
}
