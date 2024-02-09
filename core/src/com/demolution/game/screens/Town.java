package com.demolution.game.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.demolution.game.UI.VirtualJoystickInputAdapter;
import com.demolution.game.entitis.Creature;
import com.demolution.game.entitis.ListOfCreatures;
import com.demolution.game.entitis.Move;
import com.demolution.game.entitis.NPC;
import com.demolution.game.entitis.Player;
import com.demolution.game.entitis.StatusEffect;
import com.demolution.game.entitis.interactions.GiveCreatureInteraction;

import java.util.Map;

public class Town extends ScreenAdapter {

    private Game game;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;

    private Player player;
    // Preferences-Schlüssel für Spielerposition
    private static final String PLAYER_X_KEY = "player_x";
    private static final String PLAYER_Y_KEY = "player_y";
    private Array<NPC> npcArray;
    private Stage stage;
    public Touchpad touchpad;

    private VirtualJoystickInputAdapter joystickInputAdapter;

    private Array<Creature> creatures;

    private Music backgroundMusic;



    public Town(Game game){
        this.game = game;
    }

    BitmapFont font = new BitmapFont();


    public Player getPlayer() {
        return player;
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Begrenze die Kamera-Bewegung innerhalb der Map-Grenzen
        float zoomFactor = camera.zoom;
        float cameraX = MathUtils.clamp(player.getX() + player.getWidth() / 2,
                (camera.viewportWidth * zoomFactor) / 2,
                map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class) - (camera.viewportWidth * zoomFactor) / 2);
        float cameraY = MathUtils.clamp(player.getY() + (player.getHeight() * zoomFactor) / 2,
                (camera.viewportHeight * zoomFactor) / 2,
                map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class) - (camera.viewportHeight * zoomFactor) / 2);

        camera.position.set(cameraX, cameraY, 0);
        camera.update();

        renderer.setView(camera);
        renderer.render();

        renderer.getBatch().begin();

        // NPC rendern die unter dem spieler sind
        for (NPC npc : npcArray) {
            if (npc.getPosition().y >= player.getY()) {
                npc.render(renderer.getBatch());
            }
        }

        // Spieler rendern
        player.draw(renderer.getBatch());

        // NPC rendern die über dem spieler sind
        for (NPC npc : npcArray) {
            if (npc.getPosition().y < player.getY()) {
                npc.render(renderer.getBatch());
            }
        }

        // Baum-Layer nach dem Spieler rendern
        renderer.renderTileLayer((TiledMapTileLayer) map.getLayers().get("Baum"));
        renderer.getBatch().end();

        // Aktualisiere den Spieler
        player.update(delta);

        for (NPC npc : npcArray) {
            npc.update(delta);
        }

        // Zeichnen Sie die Stage für den virtuellen Joystick
        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();

    }

    @Override
    public void resize(int width, int height) {
        camera.viewportHeight = height;
        camera.viewportWidth = width;
        camera.update();
    }

    @Override
    public void show() {
        TmxMapLoader loader = new TmxMapLoader();
        map = loader.load("maps/map.tmx");

        renderer = new OrthogonalTiledMapRenderer(map);
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.zoom = 0.35f; // Set initial zoom level
        camera.update();

        // Erstellen Sie die Stage und den Touchpad
        stage = new Stage(new ScreenViewport());
        Skin touchpadSkin = new Skin(Gdx.files.internal("uiskin.json"));
        Touchpad.TouchpadStyle touchpadStyle = touchpadSkin.get("default", Touchpad.TouchpadStyle.class);
        touchpad = new Touchpad(10, touchpadStyle);

        // Ändere die Position des Touchpads auf die andere Seite des Bildschirms
        touchpad.setBounds(Gdx.graphics.getWidth() - 315, 50, 200, 200);
        stage.addActor(touchpad);

        // VirtualJoystickInputAdapter erstellen
        joystickInputAdapter = new VirtualJoystickInputAdapter(this, touchpad);

        // Registrieren Sie den Touchpad-Listener für die VirtualJoystickInputAdapter
        touchpad.addListener(joystickInputAdapter);

        //TODO nicht vergessen zu löschen
        //clearPreferences();
        //printAllCreatures();


        // Erstellen Sie das NPC-Array
        npcArray = new Array<>();

        player = new Player(new Sprite(new Texture("image/Rothaarige_Mechanikerin/Frau_rotHaarig_Mechanikerin.png")), joystickInputAdapter, map, getNPCs());

        npcArray.add(new NPC(map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class) / 2 + 100,
                map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class) / 2 - 100,
                2 * 50,
                "image/NPCs/NPC1.png", player,
                false,
                false,
                false,
                font,
                "Once I was an adventurer like you, but then I took an arrow in my knee.",
                null,
                "",
                new Array<>(),
                game));

        npcArray.get(0).setDestinations(Array.with(
                new Vector2(map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class) / 2 + 100,
                        map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class) / 2 - 100),
                new Vector2(map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class) / 2 + 100 + 100,
                        map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class) / 2 - 100),
                new Vector2(map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class) / 2 + 100 + 100,
                        map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class) / 2 - 100 + 100),
                new Vector2(map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class) / 2 + 100,
                        map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class) / 2 - 100 + 100)
        ), true);


        npcArray.add(new NPC(map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class) / 2,
                map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class) / 2 - 300,
                2 * 50,
                "image/NPCs/NPC_Savety_Organisation.png", player,
                false,
                false,
                true,
                font,
                "You have your Deamon, now go on your Adventure!",
                new GiveCreatureInteraction(ListOfCreatures.creatures.get(0)),
                "You can't travel without a Daemon, here take this one. I'm sure you will take good care of him.",
                new Array<>(),
                game));

        Array<Creature> AntagonistcreatureList = new Array<>();
        AntagonistcreatureList.add(ListOfCreatures.creatures.get(0));

        npcArray.add(new NPC(map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class) / 2 - 320,
                map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class) / 2 + 300,
                2 * 50,
                "image/NPCs/NPC_Antagonist.png", player,
                false,
                true,
                false,
                font,
                "I will be your DOOM!",
                null,
                "",
                AntagonistcreatureList,
                game));

        for (int i = 0; i < npcArray.size; i++) {
            NPC npc = npcArray.get(i);
            Boolean hasOneTimeInteraction = getSavedNPCOneTimeInteraction(i);
            if (hasOneTimeInteraction != null) {
                npc.setHasOneTimeInteraction(hasOneTimeInteraction);
            }
        }


        float playerX = getSavedPlayerX();
        float playerY = getSavedPlayerY();
        player.setPosition(playerX, playerY);

        loadCreatures();

        // Setzen Sie den InputProcessor für die Stage
        Gdx.input.setInputProcessor(stage);

        // Erstellen Sie einen Button
        TextButton.TextButtonStyle textButtonStyle = touchpadSkin.get(TextButton.TextButtonStyle.class);
        Button button = new TextButton("", textButtonStyle);

        // Setzen Sie die Größe des Buttons
        button.setSize(200, 200);
        button.setPosition(100, 100);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Aktion bei Button-Klick
                handleInteractions();
            }
        });

        // Fügen Sie den Button zur Stage hinzu
        stage.addActor(button);

        // Initialisiere die Hintergrundmusik
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("sound/background_music/Background_Instrumental.mp3"));
        backgroundMusic.setLooping(true); // Setze die Musik auf Dauerloop
        backgroundMusic.setVolume(0.02f);
        backgroundMusic.play();
    }

    private void handleInteractions() {
        // Überprüfen Sie alle NPCs auf Interaktion
        for (NPC npc : npcArray) {
            if (isPlayerNearNPC(npc)) {
                // Der Spieler ist in der Nähe und kann mit dem NPC interagieren
                npc.setIsInteracting(true);

            } else {
                // Der Spieler ist nicht mehr in der Nähe des NPCs, setzen Sie isInteracting und isPlayerInFront auf false
                npc.setIsInteracting(false);

            }
        }
    }

    private boolean isPlayerNearNPC(NPC npc) {
        float interactionDistance = 50; // Ändern Sie die Entfernung je nach Bedarf

        float playerX = player.getX() + player.getWidth() / 2;
        float playerY = player.getY() + player.getHeight() / 2;

        float npcX = npc.getPosition().x + npc.getWidth() / 2;
        float npcY = npc.getPosition().y + npc.getHeight() / 2;

        float distance = Vector2.dst(playerX, playerY, npcX, npcY);

        return distance <= interactionDistance;
    }
    public Array<NPC> getNPCs() {
        return npcArray; // Hier wird angenommen, dass npcArray eine vorher erstellte Liste oder ein Array von NPCs ist
    }

    @Override
    public void pause() {
        // Speichere die Spielerposition, wenn das Spiel pausiert wird
        savePreferneces();
    }

    @Override
    public void resume() {
        // Lade die Spielerposition, wenn das Spiel fortgesetzt wird
        float playerX = getSavedPlayerX();
        float playerY = getSavedPlayerY();
        player.setPosition(playerX, playerY);
    }

    @Override
    public void hide() {
        // Speichere die Spielerposition, wenn das Spiel verborgen wird
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.dispose();
        }
        savePreferneces();
    }


    private void savePreferneces() {
        Preferences preferences = Gdx.app.getPreferences("player_preferences");

        // Speichere die aktuelle Spielerposition in den Preferences
        preferences.putFloat(PLAYER_X_KEY, player.getX());
        preferences.putFloat(PLAYER_Y_KEY, player.getY());

        // Speichere die Variable hasOneTimeInteraction für jeden NPC
        for (int i = 0; i < npcArray.size; i++) {
            NPC npc = npcArray.get(i);
            preferences.putBoolean("npc_" + i + "_hasOneTimeInteraction", npc.hasOneTimeInteraction());
        }

        player.saveCreatures();

        preferences.flush();
    }

    //TODO zum resetten der Preferences
    private void clearPreferences() {
        Preferences preferences = Gdx.app.getPreferences("player_preferences");

        // Lösche alle Daten aus den Preferences
        preferences.clear();

        // Speichere die Änderungen
        preferences.flush();
    }

    private float getSavedPlayerX() {
        // Lade die gespeicherte X-Position des Spielers aus den Preferences
        Preferences preferences = Gdx.app.getPreferences("player_preferences");
        return preferences.getFloat(PLAYER_X_KEY, (map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class) - player.getWidth()) / 2); // Standardwert
    }

    private float getSavedPlayerY() {
        // Lade die gespeicherte Y-Position des Spielers aus den Preferences
        Preferences preferences = Gdx.app.getPreferences("player_preferences");
        return preferences.getFloat(PLAYER_Y_KEY, (map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class) - player.getHeight()) / 2); // Standardwert
    }
    private Boolean getSavedNPCOneTimeInteraction(int npcIndex) {
        Preferences preferences = Gdx.app.getPreferences("player_preferences");
        if (preferences.contains("npc_" + npcIndex + "_hasOneTimeInteraction")) {
            return preferences.getBoolean("npc_" + npcIndex + "_hasOneTimeInteraction");
        }
        return null; // Gibt null zurück, wenn der Wert nicht vorhanden ist
    }


    private void loadCreatures() {
        Preferences preferences = Gdx.app.getPreferences("player_preferences");

        // Lies die Anzahl der Kreaturen aus den Preferences
        int creatureCount = preferences.getInteger("creature_count", 0);

        creatures = new Array<>();

        // Lade jede Kreatur aus den Preferences
        for (int i = 0; i < creatureCount; i++) {
            Creature creature = new Creature("", "");
            creature.loadFromPreferences(preferences, "creature_" + i);
            creatures.add(creature);
        }

        // Füge die geladenen Kreaturen dem Player hinzu
        player.setCreatures(creatures);
    }

    private void printAllCreatures() {
        Preferences preferences = Gdx.app.getPreferences("player_preferences");

        // Lies die Anzahl der Kreaturen aus den Preferences
        int creatureCount = preferences.getInteger("creature_count", 0);

        // Ausgabe der Kreaturen auf der Konsole
        System.out.println("List of All Creatures:");
        for (int i = 0; i < creatureCount; i++) {
            Creature creature = new Creature("", "");
            creature.loadFromPreferences(preferences, "creature_" + i);

            System.out.println("Creature " + (i + 1) + ": Name=" + creature.getName());
            System.out.println("  Tamed: " + creature.isTamed());
            System.out.println("  Max Health: " + creature.getMaxHealth());
            System.out.println("  Current Health: " + creature.getCurrentHealth());
            System.out.println("  Attack Damage: " + creature.getAttackDamage());
            System.out.println("  Arcane: " + creature.getArcane());
            System.out.println("  Defense: " + creature.getDefense());
            System.out.println("  Arcane Resistance: " + creature.getArcaneResistance());
            System.out.println("  Experience: " + creature.getExperience());
            System.out.println("  Level: " + creature.getLevel());

            // Ausgabe der Moves
            System.out.println("  Moves:");
            for (int j = 0; j < creature.getMoves().length; j++) {
                Move move = creature.getMoves()[j];
                if (move != null) {
                    System.out.println("    Move " + (j + 1) + ":");
                    System.out.println("      Name: " + move.getName());
                    System.out.println("      Damage: " + move.getDamage());

                    StatusEffect statusEffect = move.getStatusEffect();
                    if (statusEffect != null) {
                        System.out.println("      Status Effect:");
                        System.out.println("        Type: " + statusEffect.getType());
                        System.out.println("        Duration: " + statusEffect.getDuration());
                        System.out.println("        Value: " + statusEffect.getValue());
                        System.out.println("        Target Self: " + statusEffect.getTarget());
                    } else {
                        System.out.println("      Status Effect: None");
                    }
                }
            }
        }
    }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        player.getTexture().dispose();
        player.dispose();
        hide();
        for (NPC npc : npcArray) {
            npc.dispose();
        }

        // Stoppe und release die Hintergrundmusik
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.dispose();
        }

        font = null;
        // Speichere die Spielerposition, wenn das Spiel geschlossen wird
        savePreferneces();
    }


}
