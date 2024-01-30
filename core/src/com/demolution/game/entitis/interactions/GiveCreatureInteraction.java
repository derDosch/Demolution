package com.demolution.game.entitis.interactions;

import com.demolution.game.entitis.Creature;
import com.demolution.game.entitis.NPC;

public class GiveCreatureInteraction implements NPCInteraction {
    private Creature creatureToGive;

    public GiveCreatureInteraction(Creature creatureToGive) {
        this.creatureToGive = creatureToGive;
    }

    @Override
    public void executeInteraction(NPC npc) {
        System.out.println("Executing GiveCreatureInteraction");

        // Führe die Interaktion durch, z.B. gib die Kreatur an den Spieler
        npc.getPlayer().addCreature(creatureToGive);

        System.out.println("Creature given to player");
    }

}