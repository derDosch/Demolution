// Player.java

package com.demolution.game.entitis;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.demolution.game.UI.VirtualJoystickInputAdapter;
import java.io.Serializable;


public class Player extends Sprite {

    private final Vector2 movement = new Vector2();
    private int lastDirection;

    private final float speed = 60 * 2;
    private final Texture playerSheetTexture;
    private final TextureRegion[][] playerSheet;

    private Animation<TextureRegion> currentAnimation;
    private float animationTime;

    private TiledMap map;

    private static final int REIHEN = 16;
    private static final int SPALTEN = 12;

    private final VirtualJoystickInputAdapter joystickInputAdapter;

    private float previousX;
    private float previousY;

    private final Rectangle playerBoundingBox;

    private Array<NPC> npcList;

    // Verkleinere die Begrenzungsbox um einen Padding-Wert
    private final float boundingBoxPadding = 10;

    //Zu speichernde Variablen
    private Array<Creature> creatures;



    public Player(Sprite sprite, VirtualJoystickInputAdapter joystickInputAdapter,TiledMap map, Array<NPC> npcList) {
        super(sprite);
        this.joystickInputAdapter = joystickInputAdapter;
        this.map = map;
        this.npcList = npcList;
        playerBoundingBox = new Rectangle();


        // Lade das Sprite-Sheet
        playerSheetTexture = new Texture("image/Rothaarige_Mechanikerin/Frau_rotHaarig_Mechanikerin.png");
        TextureRegion playerTexture = new TextureRegion(playerSheetTexture);
        playerSheet = playerTexture.split(
                playerTexture.getRegionWidth() / SPALTEN,
                playerTexture.getRegionHeight() / REIHEN
        );

        // Setze die anfängliche Animation (zum Beispiel nach unten schauen)
        currentAnimation = createAnimation(0, 2, 0.25f);

        // Setze die Größe des Spielers auf 96x96 Pixel
        setSize(96, 96);

        // Passe die Begrenzungsbox anhand des Paddings an
        playerBoundingBox.setSize(getWidth() - 4 * boundingBoxPadding, getHeight() - 4 * boundingBoxPadding);
    }


    @Override
    public void draw(Batch batch) {
        super.draw(batch);

    }

    public void update(float delta) {
        handleInput();
        checkMapBounds();
        checkObjectCollisions();
        move(delta);
        checkNPCCollisions();


        // Überprüfe, ob die Animation abgeschlossen ist, bevor sie aktualisiert wird
        if (!currentAnimation.isAnimationFinished(animationTime)) {
            updateAnimation(delta);
        } else {
            // Setze die Animation-Time auf Null, wenn die Animation abgeschlossen ist
            animationTime = 0;
        }
    }


    public Vector2 getMovement(){
        return movement;
    }

    private void checkNPCCollisions() {
        for (NPC npc : npcList) {
            if (getBoundingBox().overlaps(npc.getBoundingBox())) {
                // Kollision mit NPC gefunden
                Rectangle intersection = new Rectangle();
                Intersector.intersectRectangles(getBoundingBox(), npc.getBoundingBox(), intersection);

                // Bewegungsrichtung anhand der Kollisionsseite anpassen
                if (intersection.width > intersection.height) {
                    // Vertikale Kollision
                    if (getY() > npc.getPosition().y) {
                        setY(npc.getPosition().y + npc.getBoundingBox().height);
                    } else {
                        setY(npc.getPosition().y - getBoundingBox().height);
                    }
                } else {
                    // Horizontale Kollision
                    if (getX() > npc.getPosition().x) {
                        setX(npc.getPosition().x + npc.getBoundingBox().width);
                    } else {
                        setX(npc.getPosition().x - getBoundingBox().width);
                    }
                }
                movement.setZero();
            }
        }
    }




    private void checkMapBounds() {
        // Begrenze den Spieler innerhalb der Kartenränder
        float minX = 0;
        float minY = 0;
        float maxX = map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class) - getWidth();
        float maxY = map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class) - getHeight();

        setX(MathUtils.clamp(getX(), minX, maxX));
        setY(MathUtils.clamp(getY(), minY, maxY));
    }




    private void checkObjectCollisions() {
        // Setze die Begrenzungsbox des Spielers basierend auf seiner Position und dem Padding
        playerBoundingBox.set(getX() + (getWidth() - 6 * boundingBoxPadding), getY(),
                getWidth() - 7 * boundingBoxPadding, getHeight() - 6 * boundingBoxPadding);

        // Hole die "Collision"-Objektebene aus der Tiled Map
        MapObjects objects = map.getLayers().get("Collision").getObjects();

        for (MapObject object : objects) {
            if (object instanceof PolygonMapObject) {
                Polygon polygon = ((PolygonMapObject) object).getPolygon();

                // Überprüfe, ob die Spieler-Begrenzungsbox mit dem Polygon kollidiert
                if (Intersector.overlapConvexPolygons(getPlayerBoundingPolygon(), polygon)) {
                    // Kollision gefunden, setze die Spielerposition zurück
                    setX(previousX);
                    setY(previousY);
                }
            }
        }

        // Speichere die vorherige Position des Spielers
        previousX = getX();
        previousY = getY();
    }

    private Polygon getPlayerBoundingPolygon() {
        float[] vertices = {
                playerBoundingBox.x, playerBoundingBox.y,
                playerBoundingBox.x + playerBoundingBox.width, playerBoundingBox.y,
                playerBoundingBox.x + playerBoundingBox.width, playerBoundingBox.y + playerBoundingBox.height,
                playerBoundingBox.x, playerBoundingBox.y + playerBoundingBox.height
        };
        return new Polygon(vertices);
    }

    public Rectangle getBoundingBox() {
        float boundingBoxPadding = 10;
        return new Rectangle(getX() + (getWidth() - 6 * boundingBoxPadding), getY(),
                getWidth() - 7 * boundingBoxPadding, getHeight() - 6 * boundingBoxPadding);
    }


    private void handleInput() {

        if (!joystickInputAdapter.isJoystickActive()) {
            movement.set(0, 0);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            movement.x = -speed;
            if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                movement.y = speed;
                currentAnimation = createAnimation(2, 3, 5, 0.25f); // Nach oben links gehen
                lastDirection = 1;
            } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                movement.y = -speed;
                currentAnimation = createAnimation(0, 3, 5, 0.25f); // Nach unten links gehen
                lastDirection = 2;
            } else {
                currentAnimation = createAnimation(1, 0, 2, 0.25f); // Nach links gehen
                lastDirection = 3;
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            movement.x = speed;
            if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                movement.y = speed;
                currentAnimation = createAnimation(3, 3, 5, 0.25f); // Nach oben rechts gehen
                lastDirection = 4;
            } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                movement.y = -speed;
                currentAnimation = createAnimation(1, 3, 5, 0.25f); // Nach unten rechts gehen
                lastDirection = 5;
            } else {
                currentAnimation = createAnimation(2, 0, 2, 0.25f); // Nach rechts gehen
                lastDirection = 6;
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            movement.y = speed;
            currentAnimation = createAnimation(3, 0, 2, 0.25f); // Nach oben gehen
            lastDirection = 7;
        } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            movement.y = -speed;
            currentAnimation = createAnimation(0, 0, 2, 0.25f); // Nach unten gehen
            lastDirection = 8;
        }

        // Normalisiere den Bewegungsvektor, wenn er nicht null ist
        if (movement.len2() > 0) {
            movement.nor().scl(speed);
        }

        if (movement.len2() == 0){
            currentAnimation = createStandingPlayer(lastDirection);
        }
    }


    public void handleJoystickAngle(float angleRad) {
        // Setze die Bewegungsrichtung basierend auf dem Winkel
        movement.set(MathUtils.cos(angleRad), MathUtils.sin(angleRad)).nor().scl(speed);


        // Setze die Geschwindigkeit des Spielers unabhängig von der Joystick-Bewegung
        if (movement.len() > 0) {
            movement.setLength(speed);
        }

        // Aktualisiere die Animation basierend auf der Bewegungsrichtung
        currentAnimation = createAnimationBasedOnMovement();

    }



    private Animation<TextureRegion> createAnimationBasedOnMovement() {

        if (movement.y > 30) {
            // Bewegung nach oben
            if (movement.x > 30) {
                // Nach oben rechts gehen
                lastDirection = 6;
                return createAnimation(3, 3, 5, 0.25f);
            } else if (movement.x < -30) {
                // Nach oben links gehen
                lastDirection = 1;
                return createAnimation(2, 3, 5, 0.25f);
            } else {
                // Nach oben gehen
                lastDirection = 7;
                return createAnimation(3, 0, 2, 0.25f);
            }
        } else if (movement.y < -30) {
            // Bewegung nach unten
            if (movement.x > 30) {
                // Nach unten rechts gehen
                lastDirection = 5;
                return createAnimation(1, 3, 5, 0.25f);
            } else if (movement.x < -30) {
                // Nach unten links gehen
                lastDirection = 2;
                return createAnimation(0, 3, 5, 0.25f);
            } else {
                // Nach unten gehen
                lastDirection = 8;
                return createAnimation(0, 0, 2, 0.25f);
            }
        } else {
            // Keine vertikale Bewegung
            if (movement.x > 0) {
                // Bewegung nach rechts
                lastDirection = 6;
                return createAnimation(2, 0, 2, 0.25f);
            } else if (movement.x < 0) {
                // Bewegung nach links
                lastDirection = 3;
                return createAnimation(1, 0, 2, 0.25f);
            } else {
                // Keine Bewegung, verwende Standbild
                return createStandingPlayer(lastDirection);
            }
        }
    }

    private Animation<TextureRegion> createStandingPlayer(int i) {
        if(i == 1){
            return createAnimation(2, 4, 4, 0.25f); // Nach oben links stehen
        } else if (i == 2) {
            return createAnimation(0, 4, 4, 0.25f); // Nach unten links stehen
        } else if (i == 3) {
            return createAnimation(1, 1, 1, 0.25f); // Nach links stehen
        } else if (i == 4) {
            return createAnimation(3, 4, 4, 0.25f); // Nach oben rechts stehen
        } else if (i == 5) {
            return createAnimation(1, 4, 4, 0.25f); // Nach unten rechts stehen
        } else if (i == 6) {
            return createAnimation(2, 1, 1, 0.25f); // Nach rechts stehen
        } else if (i == 7) {
            return createAnimation(3, 1, 1, 0.25f); // Nach oben stehen
        } else {
            return createAnimation(0, 1, 1, 0.25f); // Nach unten stehen
        }
    }


    private Animation<TextureRegion> createAnimation(int reihe, int startSpalte, int endSpalte, float frameDuration) {
        TextureRegion[] frames = new TextureRegion[endSpalte - startSpalte + 1];
        int index = 0;

        for (int spalte = startSpalte; spalte <= endSpalte; spalte++) {
            frames[index++] = playerSheet[reihe][spalte];
        }

        return new Animation<>(frameDuration, frames);
    }

    public void move(float delta) {
        setX(getX() + movement.x * delta);
        setY(getY() + movement.y * delta);
    }

    private void updateAnimation(float delta) {

        if (currentAnimation.isAnimationFinished(animationTime)) {
            // Setze die Animation-Time auf null zurück
            animationTime = 0;
        } else {
            // Ansonsten erhöhe die Animation-Time weiter
            animationTime += delta;
        }

        // Aktualisiere die Animation nur, wenn sie nicht abgeschlossen ist
        if (!currentAnimation.isAnimationFinished(animationTime)) {
            TextureRegion currentFrame = currentAnimation.getKeyFrame(animationTime, true);
            setRegion(currentFrame);
        }
    }


    private Animation<TextureRegion> createAnimation(int reihe, int endSpalte, float frameDuration) {
        TextureRegion[] frames = new TextureRegion[endSpalte + 1];
        int index = 0;

        for (int spalte = 0; spalte <= endSpalte; spalte++) {
            frames[index++] = playerSheet[reihe][spalte];
        }

        return new Animation<>(frameDuration, frames);
    }


    public void setCreatures(Array<Creature> creatures) {
        this.creatures = creatures;
    }
    public void addCreature(Creature creature){
        creatures.add(creature);
    }

    public void saveCreatures() {
        Preferences preferences = Gdx.app.getPreferences("player_preferences");

        // Speichere die Anzahl der Kreaturen in den Preferences
        preferences.putInteger("creature_count", creatures.size);

        // Speichere jede Kreatur in den Preferences
        for (int i = 0; i < creatures.size; i++) {
            creatures.get(i).saveToPreferences(preferences, "creature_" + i);
        }

        preferences.flush();
    }

    public Array<Creature> getCreatures() {
        return creatures;
    }

    // Methode zum Freigeben von Ressourcen
    public void dispose() {
        playerSheetTexture.dispose();
        saveCreatures();
    }
}
