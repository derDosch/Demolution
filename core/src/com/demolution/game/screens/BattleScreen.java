package com.demolution.game.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.demolution.game.entitis.Creature;
import com.demolution.game.entitis.Move;
import com.demolution.game.entitis.NPC;
import com.demolution.game.entitis.Player;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.graphics.Texture.TextureFilter;


public class BattleScreen implements Screen, InputProcessor {
    private Game game;
    private NPC daemonNPC;
    private Player player;
    private boolean battleEnded = false;

    private Texture battleCommandTexture;
    private TextureRegion[][] buttonRegions;
    private TextureRegion fightButton;
    private TextureRegion creatureButton;
    private TextureRegion bagButton;
    private TextureRegion runButton;

    private TextureRegion[][] playerCreatureRegions;
    private TextureRegion[][] npcCreatureRegions;

    private Rectangle[] buttonRectangles = new Rectangle[4];

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private boolean menuChanged = false;

    float buttonSpacing = 10f;
    private Array<Creature> playerCreatures;
    private Array<Creature> npcCreatures;
    BitmapFont font;

    int activePlayerCreaturePosition = 0;
    int activeNpcCreaturePosition = 0;

    private TextureRegion battlePlayerBoxDRegion;
    private TextureRegion battleNPCBoxDRegion;
    private TextureRegion solidColorTextureRegion;

    private int buttonPressedIndex = -1;



    public BattleScreen(Game game, NPC daemonNPC, Player player) {
        this.game = game;
        this.daemonNPC = daemonNPC;
        this.player = player;
        this.playerCreatures = player.getCreatures();
        this.npcCreatures = daemonNPC.getNPCcreatures();

// Setzen der Schriftgröße basierend auf der Bildschirmhöhe
        float screenHeight = Gdx.graphics.getHeight();
        float fontSize = screenHeight / 200; // Beispiel: Schriftgröße als ein Zwanzigstel der Bildschirmhöhe

// Font initialisieren mit der berechneten Schriftgröße
        font = new BitmapFont();
        font.getData().setScale(fontSize);
    }

    @Override
    public void show() {
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.setToOrtho(false);

        if (playerCreatures.size > 0 && npcCreatures.size > 0) {
            batch = new SpriteBatch();
            battleCommandTexture = new Texture("NeutralBattleGUI/battleCommand.png");

            TextureRegion spriteSheet = new TextureRegion(new Texture("NeutralBattleGUI/battleCommandButtons.png"));
            buttonRegions = spriteSheet.split(spriteSheet.getRegionWidth() / 2, spriteSheet.getRegionHeight() / 9);
            fightButton = buttonRegions[0][1];
            creatureButton = buttonRegions[1][1];
            bagButton = buttonRegions[2][1];
            runButton = buttonRegions[3][1];

            playerCreatures = player.getCreatures();

            Creature activePlayerCreature = playerCreatures.get(activePlayerCreaturePosition);
            TextureRegion playerCreatureTexture = new TextureRegion(new Texture(activePlayerCreature.getSpriteSheetPath()));
            playerCreatureRegions = playerCreatureTexture.split(playerCreatureTexture.getRegionWidth() / 2, playerCreatureTexture.getRegionHeight());

            Creature activeNpcCreature = npcCreatures.get(activeNpcCreaturePosition);
            TextureRegion npcCreatureTexture = new TextureRegion(new Texture(activeNpcCreature.getSpriteSheetPath()));
            npcCreatureRegions = npcCreatureTexture.split(npcCreatureTexture.getRegionWidth() / 2, npcCreatureTexture.getRegionHeight());

            // Laden der TextureRegion für die BattlePlayerBoxD
            TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("NeutralBattleGUI/battle_ui.atlas"));
            battlePlayerBoxDRegion = atlas.findRegion("BattlePlayerBoxS");
            battleNPCBoxDRegion = atlas.findRegion("BattleEnemyBoxS");
            solidColorTextureRegion = createSolidColorTextureRegion(Color.GREEN, 1, 1); // Sie können die Breite und Höhe anpassen


            float buttonWidth = (Gdx.graphics.getWidth() / 2f - 3 * buttonSpacing) / 2f;
            float buttonHeight = (Gdx.graphics.getHeight() / 1.5f - 5 * buttonSpacing) / 9f;

            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 9; j++) {
                    if (i == 1 && j < 2) {
                        float x = Gdx.graphics.getWidth() / 2f + buttonSpacing + (buttonWidth + buttonSpacing) ;
                        float y = buttonSpacing + (buttonHeight + buttonSpacing) * j + Gdx.graphics.getHeight() / 6f - (buttonSpacing + (buttonHeight + buttonSpacing));
                        buttonRectangles[j] = new Rectangle(x, y, buttonWidth, buttonHeight);
                    }
                    if (i == 1 && j > 1 && j < 4) {
                        float x = Gdx.graphics.getWidth() / 2f + buttonSpacing ;
                        float y = buttonSpacing + (buttonHeight + buttonSpacing) * (j - 2) + Gdx.graphics.getHeight() / 6f - (buttonSpacing + (buttonHeight + buttonSpacing));
                        buttonRectangles[j] = new Rectangle(x, y, buttonWidth, buttonHeight);
                    }
                }
            }

            Gdx.input.setInputProcessor(this);
        } else {
            battleEnded = true;
            game.setScreen(new Town(game));
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        playerCreatures = player.getCreatures();

        Creature activePlayerCreature = playerCreatures.get(activePlayerCreaturePosition);

        batch.begin();
        batch.draw(npcCreatureRegions[0][1], 0f, Gdx.graphics.getHeight() * 2 / 7f, Gdx.graphics.getHeight() / 3f, Gdx.graphics.getWidth() / 3f);
        batch.draw(battleCommandTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() / 3f);
        batch.end();

        if (menuChanged) {
            // Wenn das Menü geändert wurde, rendern Sie das neue Menü
            renderChangedMenu();
        } else {
            // Andernfalls rendern Sie das Standardmenü
            renderDefaultMenu();
        }

        batch.begin();
        batch.draw(playerCreatureRegions[0][0], Gdx.graphics.getWidth() * 2 / 3f, Gdx.graphics.getHeight() * 1 / 2f, Gdx.graphics.getHeight() / 3f, Gdx.graphics.getWidth() / 3f);
        batch.draw(battlePlayerBoxDRegion, Gdx.graphics.getWidth()/20, Gdx.graphics.getHeight()/3 + 10, Gdx.graphics.getWidth()/5, Gdx.graphics.getHeight()/10);
        batch.draw(battleNPCBoxDRegion, Gdx.graphics.getWidth()*13/20, Gdx.graphics.getHeight()*320/640 + 10, Gdx.graphics.getWidth()/5, Gdx.graphics.getHeight()/10);
        // Berechnen des prozentualen Lebenswertes
        float healthPercentage = (float) activePlayerCreature.getCurrentHealth() / (float) activePlayerCreature.getMaxHealth();

        // Definieren der Breite des grünen Balkens basierend auf dem prozentualen Lebenswert
        float greenBarWidth = Gdx.graphics.getWidth()*43/400 * healthPercentage;

        // Zeichnen des grünen Balkens
        batch.setColor(Color.GREEN); // Setzen der Farbe auf Grün
        batch.draw(solidColorTextureRegion, Gdx.graphics.getWidth()*79/640, Gdx.graphics.getHeight()*248/640, greenBarWidth, Gdx.graphics.getHeight()/80); // Anpassen von solidColorTextureRegion entsprechend Ihrer Implementierung
        batch.setColor(Color.WHITE); // Zurücksetzen der Farbe auf Weiß
        batch.end();


    }

    private void renderDefaultMenu() {
        batch.begin();


        batch.draw(fightButton, buttonRectangles[0].x, buttonRectangles[0].y, buttonRectangles[0].width, buttonRectangles[0].height);
        batch.draw(creatureButton, buttonRectangles[1].x, buttonRectangles[1].y, buttonRectangles[1].width, buttonRectangles[1].height);
        batch.draw(bagButton, buttonRectangles[2].x, buttonRectangles[2].y, buttonRectangles[2].width, buttonRectangles[2].height);
        batch.draw(runButton, buttonRectangles[3].x, buttonRectangles[3].y, buttonRectangles[3].width, buttonRectangles[3].height);


        batch.end();
    }

    // Methode zum Rendern des geänderten Menüs
    private void renderChangedMenu() {
        batch.begin();
        // Zeichnen Sie das geänderte Menü
        batch.draw(new TextureRegion(new Texture("NeutralBattleGUI/battleFight.png")), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() / 3f);
        renderPlayerCreatureMoves();
        batch.end();
    }

    private void renderPlayerCreatureMoves() {
        Creature activePlayerCreature = playerCreatures.get(activePlayerCreaturePosition);
        Move[] moves = activePlayerCreature.getMoves();

        // Position und Größe der Textfelder für Move-Namen
        float x = Gdx.graphics.getWidth()*37/120;
        float y = Gdx.graphics.getHeight()*13/48;
        float spacing = Gdx.graphics.getHeight()*3/48;

        // Rendern der Move-Namen als Text
        for (int i = 0; i < moves.length; i++) {
            // Überprüfen, ob der Move nicht DefaultMove ist
            if (!moves[i].getName().equals("DefaultMove")) {
                if(i < 2){
                    // Setzen der Schriftfarbe auf Schwarz
                    font.setColor(0, 0, 0, 1);
                    // Rendern des Move-Namens als Text
                    font.draw(batch, moves[i].getName(), -x, y - (font.getLineHeight() + spacing) * i, Gdx.graphics.getWidth(), Align.center, false);
                    //font.draw(batch, moves[i].getName(), x, y- (font.getLineHeight() + spacing)*i);
                }else{
                    // Setzen der Schriftfarbe auf Schwarz
                    font.setColor(0, 0, 0, 1);
                    // Rendern des Move-Namens als Text
                    //font.draw(batch, moves[i].getName(), x + x*3, y - (font.getLineHeight() + spacing)*(i%2));
                    font.draw(batch, moves[i].getName(), x - x*4/5, y - (font.getLineHeight() + spacing) * (i%2), Gdx.graphics.getWidth(), Align.center, false);
                }

            }
        }
    }


    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        Vector3 touchPoint = new Vector3(screenX, screenY, 0);
        camera.unproject(touchPoint);
        System.out.println(buttonPressedIndex);
        if (buttonPressedIndex == 3 && buttonRectangles[3].contains(touchPoint.x, touchPoint.y)) {
            // Wenn der "Run" Button losgelassen wird, zurück zum Town Screen
            game.setScreen(new Town(game));
            return true; // Event abgefangen
        }
        if (buttonPressedIndex == 0 && buttonRectangles[0].contains(touchPoint.x, touchPoint.y)) {
            // Setzen Sie den Zustand des geänderten Menüs auf true, wenn der Benutzer den "fightButton" loslässt
            menuChanged = true;

            // Setzen Sie den Index des gedrückten Buttons zurück, da der Benutzer den Finger losgelassen hat
            buttonPressedIndex = -1;

            return true; // Rückgabe true, um anzuzeigen, dass das Event abgefangen wurde
        }


        return false;
    }

    // Definieren Sie eine einfarbige Textur dynamisch
    private TextureRegion createSolidColorTextureRegion(Color color, int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        TextureRegion textureRegion = new TextureRegion(texture);
        pixmap.dispose(); // Sie sollten den Pixmap freigeben, nachdem Sie die Textur erstellt haben
        return textureRegion;
    }


    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        if (batch != null) {
            batch.dispose();
        }
        if (battleCommandTexture != null) {
            battleCommandTexture.dispose();
        }
    }

    @Override
    public void dispose() {
        hide();
    }


    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }



    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // Umrechnen der Bildschirmkoordinaten in Weltkoordinaten
        Vector3 touchPoint = new Vector3(screenX, screenY, 0);
        camera.unproject(touchPoint);

        boolean fingerOverButton = false; // Variable zum Überprüfen, ob sich der Finger über einem Button befindet

        // Überprüfen, ob der Finger über einem Button ist
        if (buttonRectangles[0].contains(touchPoint.x, touchPoint.y)) {
            // Ändern Sie den Sprite des Buttons auf den "gedrückt" Zustand
            fightButton = buttonRegions[0][0];
            fingerOverButton = true;
            buttonPressedIndex = 0;
        } else {
            fightButton = buttonRegions[0][1];
        }

        if (buttonRectangles[1].contains(touchPoint.x, touchPoint.y)) {
            // Ändern Sie den Sprite des Buttons auf den "gedrückt" Zustand
            creatureButton = buttonRegions[1][0];
            fingerOverButton = true;
            buttonPressedIndex = 1;
        } else {
            creatureButton = buttonRegions[1][1];
        }

        if (buttonRectangles[2].contains(touchPoint.x, touchPoint.y)) {
            // Ändern Sie den Sprite des Buttons auf den "gedrückt" Zustand
            bagButton = buttonRegions[2][0];
            fingerOverButton = true;
            buttonPressedIndex = 2;
        } else {
            bagButton = buttonRegions[2][1];
        }

        if (buttonRectangles[3].contains(touchPoint.x, touchPoint.y)) {
            // Ändern Sie den Sprite des Buttons auf den "gedrückt" Zustand
            runButton = buttonRegions[3][0];
            fingerOverButton = true;
            buttonPressedIndex = 3;
        } else {
            runButton = buttonRegions[3][1];
        }

        // Falls der Finger nicht über einem Button ist und er vorher über einem Button war, setze den "normalen" Zustand des zuletzt gedrückten Buttons zurück
        if (!fingerOverButton && buttonPressedIndex != -1) {
            switch (buttonPressedIndex) {
                case 0:
                    fightButton = buttonRegions[0][1];
                    break;
                case 1:
                    creatureButton = buttonRegions[1][1];
                    break;
                case 2:
                    bagButton = buttonRegions[2][1];
                    break;
                case 3:
                    runButton = buttonRegions[3][1];
                    break;
            }
            // Rendere die Änderungen sofort
            render(Gdx.graphics.getDeltaTime());
        }

        render(Gdx.graphics.getDeltaTime());
        return false;
    }



    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
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
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
