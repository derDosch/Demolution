package com.demolution.game.entitis;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.demolution.game.entitis.interactions.NPCInteraction;

public class NPC {

    private static final int SPALTEN = 12;
    private static final int REIHEN = 8;

    private Vector2 position;
    private Vector2 velocity;
    private Rectangle boundingBox;
    private float speed;

    private Texture npcSheetTexture;
    private TextureRegion[][] npcSheet;
    private Animation<TextureRegion> currentAnimation;
    private float stateTime;

    private Array<Vector2> destinations;
    private int currentDestinationIndex;
    private boolean movementLoop;
    private float timeSinceLastPlayerCollision; // Zeit seit der letzten Kollision mit dem Spieler
    private static final float TIME_TO_RESUME_MOVEMENT = 2.0f; // Zeit, nach der der NPC seine normale Route wieder aufnimmt

    private Player player; // Referenz auf den Spieler

    private Vector2 previousDestination; // Hinzugefügte Variable für die vorherige Destination

    private boolean isCollidiert;

    private Array<Creature> NPCcreatures;


    private int previousDirection;

    private boolean isInteracting;
    private BitmapFont font;
    private int displayedMessageLength; // Variable, um die Anzahl der bereits angezeigten Zeichen zu verfolgen

    String message;
    String SpecialMessage;

    private  boolean isTrader;
    private  boolean isDeamonUser;
    private boolean hasOneTimeInteraction;
    private NPCInteraction specialInteraction;



    public NPC(float x, float y, float speed, String npcSheetPath, Player player, boolean isTrader, boolean isDeamonUser, boolean hasOneTimeInteraction, BitmapFont font, String message, NPCInteraction specialInteraction, String SpecialMessage,Array<Creature> NPCcreatures) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2();
        this.speed = speed;
        this.player = player;
        this.isTrader = isTrader;
        this.isDeamonUser = isDeamonUser;
        this.font = font;
        this.message =message;
        this.hasOneTimeInteraction = hasOneTimeInteraction;
        this.specialInteraction = specialInteraction;
        this.SpecialMessage = SpecialMessage;
        this.NPCcreatures = NPCcreatures;

        previousDestination = new Vector2(); // Initialisiere die vorherige Destination

        // Initialize bounding box
        boundingBox = new Rectangle();
        updateBoundingBox();

        // Load NPC SpriteSheet
        npcSheetTexture = new Texture(Gdx.files.internal(npcSheetPath));
        TextureRegion npcTexture = new TextureRegion(npcSheetTexture);
        npcSheet = npcTexture.split(
                npcTexture.getRegionWidth() / SPALTEN,
                npcTexture.getRegionHeight() / REIHEN
        );

        // Set default animation
        currentAnimation = createAnimation(3, 0, 2, 0.25f); // Default: move down
        stateTime = 0;

        // Initialize destinations array
        destinations = new Array<>();
        currentDestinationIndex = 0;
        movementLoop = false;
    }

    public void interact(Batch batch, BitmapFont font, String message,boolean hasOneTimeInteraction, String SpecialMessage) {
        if (isInteracting) {
            // Überprüfen Sie, ob der gesamte Text bereits angezeigt wurde
            if(hasOneTimeInteraction) {
                // Führe die einmalige Interaktion durch
                if (displayedMessageLength < SpecialMessage.length()) {
                    // Extrahiere den Teil des Textes, der bisher angezeigt werden soll
                    String partialMessage = SpecialMessage.substring(0, displayedMessageLength + 1);

                    // Zeichne den Teil des Textes auf den Bildschirm mit Zeilenumbruch
                    drawWrappedText(batch, font, partialMessage, position.x + 80, position.y + getHeight() + 10, 5);

                    displayedMessageLength += 1;
                } else if (displayedMessageLength == SpecialMessage.length() && isInteracting) {
                    // Zeichne den gesamten Text auf den Bildschirm mit Zeilenumbruch
                    drawWrappedText(batch, font, SpecialMessage, position.x + 80, position.y + getHeight() + 10, 5);
                    executeSpecialInteraction();
                    setHasOneTimeInteraction(false);
                }

            }else{
                if (displayedMessageLength < message.length()) {
                    // Extrahiere den Teil des Textes, der bisher angezeigt werden soll
                    String partialMessage = message.substring(0, displayedMessageLength + 1);

                    // Zeichne den Teil des Textes auf den Bildschirm mit Zeilenumbruch
                    drawWrappedText(batch, font, partialMessage, position.x + 80, position.y + getHeight() + 10, 5);

                    displayedMessageLength += 1;
                } else if (displayedMessageLength == message.length() && isInteracting) {
                    // Zeichne den gesamten Text auf den Bildschirm mit Zeilenumbruch
                    drawWrappedText(batch, font, message, position.x + 80, position.y + getHeight() + 10, 5);
                }
            }

            // Der gesamte Text wurde angezeigt
            if (isDeamonUser) {
                // Spezifische Logik für Daemon-User
            } else if (isTrader) {
                // Spezifische Logik für Trader
            }
        }
    }

    // Methode zum Abfragen des hasOneTimeInteraction-Status
    public boolean hasOneTimeInteraction() {
        return hasOneTimeInteraction;
    }

    //Setzt die SPezielle Interaction
    public void setSpecialInteraction(NPCInteraction specialInteraction) {
        this.specialInteraction = specialInteraction;
    }

    public void executeSpecialInteraction() {
        if (specialInteraction != null) {
            specialInteraction.executeInteraction(this);
        }
    }

    // Methode zum Setzen des hasOneTimeInteraction-Status
    public void setHasOneTimeInteraction(boolean hasOneTimeInteraction) {
        this.hasOneTimeInteraction = hasOneTimeInteraction;
    }

    // Methode zum Zeichnen von Text mit Zeilenumbruch nach einer bestimmten Anzahl von Wörtern
    private void drawWrappedText(Batch batch, BitmapFont font, String text, float x, float y, int wordsPerLine) {
        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            currentLine.append(words[i]).append(" ");

            if ((i + 1) % wordsPerLine == 0 || i == words.length - 1) {
                // Setze die Farbe des Texts auf Schwarz
                font.setColor(0, 0, 0, 1); // Schwarz: R=0, G=0, B=0, Alpha=1
                // Zeichne die aktuelle Zeile auf den Bildschirm
                font.draw(batch, currentLine.toString().trim(), x, y);
                // Setze die Farbe des Texts zurück
                font.setColor(1, 1, 1, 1); // Zurücksetzen auf Weiß: R=1, G=1, B=1, Alpha=1

                // Setze die X-Position für die nächste Zeile
                y -= font.getLineHeight();

                // Leere die aktuelle Zeile
                currentLine.setLength(0);
            }
        }
    }



    public void setIsInteracting(boolean isInteracting) {
        this.isInteracting = isInteracting;
    }


    public float getWidth() {
        // Breite des NPCs (zum Beispiel die Breite des aktuellen Frames der Animation)
        TextureRegion frame = currentAnimation.getKeyFrame(stateTime, true);
        return frame.getRegionWidth();
    }

    public float getHeight() {
        // Höhe des NPCs (zum Beispiel die Höhe des aktuellen Frames der Animation)
        TextureRegion frame = currentAnimation.getKeyFrame(stateTime, true);
        return frame.getRegionHeight();
    }


    private void updateBoundingBox() {
        boundingBox.set(position.x + (36), position.y, 34, 36);
    }

    private Animation<TextureRegion> createAnimation(int row, int startCol, int endCol, float frameDuration) {
        TextureRegion[] frames = new TextureRegion[endCol - startCol + 1];
        int index = 0;
        for (int col = startCol; col <= endCol; col++) {
            frames[index++] = npcSheet[row][col];
        }
        return new Animation<>(frameDuration, frames);
    }

    public void update(float deltaTime) {

        stateTime += deltaTime;
        position.add(velocity.x * deltaTime, velocity.y * deltaTime);
        updateBoundingBox();

        if (hasReachedDestination()) {
            if (movementLoop) {
                currentDestinationIndex = (currentDestinationIndex + 1) % destinations.size;
            } else {
                currentDestinationIndex = Math.min(currentDestinationIndex + 1, destinations.size - 1);
            }

            if (currentDestinationIndex < destinations.size) {
                setDestination(destinations.get(currentDestinationIndex));
                updateAnimation(); // Update Animation basierend auf der neuen Richtung
            }
        }

       checkPlayerCollision(deltaTime); // Überprüfe Kollision mit Spieler



// Wenn Zeit seit der letzten Spielerkollision größer als TIME_TO_RESUME_MOVEMENT ist, setze normale Route fort
        if (timeSinceLastPlayerCollision > TIME_TO_RESUME_MOVEMENT && isCollidiert ) {
            if(destinations.size != 0) {
                setDestination(previousDestination);
            }
            isCollidiert = false;
            isInteracting = false;
            displayedMessageLength = 0;
        }


        // Hier sicherstellen, dass die updateAnimation-Methode aufgerufen wird
        updateAnimation();
    }

    private boolean isPlayerNearME(Player player) {
        float interactionDistance = 50; // Ändern Sie die Entfernung je nach Bedarf

        float playerX = player.getX() + player.getWidth() / 2;
        float playerY = player.getY() + player.getHeight() / 2;

        float npcX = getPosition().x + getWidth() / 2;
        float npcY = getPosition().y + getHeight() / 2;

        float distance = Vector2.dst(playerX, playerY, npcX, npcY);

        return distance <= interactionDistance;
    }

    private void checkPlayerCollision(float deltaTime) {
        Rectangle playerBoundingBox = player.getBoundingBox();

        if (playerBoundingBox.overlaps(getBoundingBox()) || (destinations.size == 0 && isPlayerNearME(player))) {
            // Spielerkollision gefunden
            timeSinceLastPlayerCollision = 0; // Setze die Zeit zurück

            // Speichere die aktuelle Destination als vorherige Destination
            if(destinations.size != 0) {
                previousDestination = new Vector2(destinations.get(currentDestinationIndex));
            }

            velocity.setZero();

            isCollidiert = true;
        } else if(!isPlayerNearME(player)) {
            timeSinceLastPlayerCollision += deltaTime;
        }
    }





    public Vector2 getPosition() {
        return position.cpy(); // Verwende .cpy(), um eine Kopie der Position zurückzugeben und unerwartetes Verhalten zu vermeiden
    }

    private void updateAnimation() {
        // Wenn die Geschwindigkeit null ist, zeige das Standbild der letzten Richtung
        if (velocity.len2() == 0) {
            currentAnimation = createStandingNPC(previousDirection);
        } else {
            // Berechne die Bewegungsrichtung
            float angle = velocity.angleDeg();
            int row ; // Standard-Row (z.B., wenn der NPC steht)
            int startCol ;
            int endCol ;

            // Definiere die Winkelbereiche für jede Richtung und aktualisiere die Row entsprechend
            if ((angle >= 292.5 && angle < 337.5)) {
                row = 1; // Nach unten rechts gehen
                startCol = 2;
                endCol = 4;
                previousDirection = 5;
            } else if (angle >= 112.5 && angle < 157.5) {
                row = 2; // Nach oben links gehen
                startCol = 2;
                endCol = 4;
                previousDirection = 1;
            } else if (angle >= 67.5 && angle < 112.5) {
                row = 3; // Nach oben gehen
                startCol = 0;
                endCol = 2;
                previousDirection = 7;
            } else if (angle >= 22.5 && angle < 67.5) {
                row = 3; // Nach oben rechts gehen
                startCol = 2;
                endCol = 4;
                previousDirection = 4;
            } else if (angle >= 157.5 && angle < 202.5) {
                row = 1; // Nach links gehen
                startCol = 0;
                endCol = 2;
                previousDirection = 3;
            } else if (angle >= 247.5 && angle < 292.5) {
                row = 0; // Nach unten gehen
                startCol = 0;
                endCol = 2;
                previousDirection = 8;
            } else if (angle >= 337.5 || angle < 22.5) {
                row = 2; // Nach rechts gehen
                startCol = 0;
                endCol = 2;
                previousDirection = 6;
            } else {
                row = 0; // Nach unten links gehen
                startCol = 2;
                endCol = 4;
                previousDirection = 2;
            }

            currentAnimation = createAnimation(row, startCol, endCol, 0.25f);
        }
    }

    private Animation<TextureRegion> createStandingNPC(int i) {
        if (i == 1) {
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

    private boolean hasReachedDestination() {
        if (!destinations.isEmpty() && currentDestinationIndex < destinations.size) {
            float distance = position.dst(destinations.get(currentDestinationIndex));
            return distance < speed * Gdx.graphics.getDeltaTime();
        }
        return false;
    }

    public void setDestinations(Array<Vector2> destinations, boolean loop) {
        this.destinations.clear();
        this.destinations.addAll(destinations);
        this.movementLoop = loop;

        if (!destinations.isEmpty()) {
            currentDestinationIndex = 0;
            setDestination(destinations.first());
        }
    }

    public void setDestination(Vector2 destination) {
        Vector2 direction = destination.cpy().sub(position).nor();
        velocity.set(direction.scl(speed));
    }

    public void render(Batch batch) {
        TextureRegion frame = currentAnimation.getKeyFrame(stateTime, true);
        batch.draw(frame, position.x, position.y);
        // Überprüfe und zeichne den Interaktionstext
        if (isInteracting) {
            interact(batch, font, message, hasOneTimeInteraction, SpecialMessage);
        }
    }


    public Rectangle getBoundingBox() {
        return boundingBox;
    }

    public void dispose() {
        npcSheetTexture.dispose();
    }


    public Player getPlayer() {
        return player;
    }
}

