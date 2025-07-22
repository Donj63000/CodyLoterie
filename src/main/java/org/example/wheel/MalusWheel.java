package org.example.wheel;

import java.util.function.Consumer;
import javafx.scene.paint.Color;
import org.example.Resultat;

public final class MalusWheel extends BaseWheel<String> {

    private static final Color FIRE_START  = Color.web("#ff5722");
    private static final Color FIRE_END    = Color.web("#8b0000");
    private static final Color HIGHLIGHT   = Color.web("#ff2200");
    private static final String PREFIX     = "Malus : ";

    private Consumer<String> spinCallback;

    public MalusWheel(Resultat resultat) {
        super(resultat);
    }

    public void setOnSpinFinished(Consumer<String> callback) {
        this.spinCallback = callback;
    }

    @Override protected Color getFireStart()      { return FIRE_START; }
    @Override protected Color getFireEnd()        { return FIRE_END; }
    @Override protected Color getHighlightColor() { return HIGHLIGHT; }
    @Override protected String prefix()           { return PREFIX; }
    @Override protected String itemToString(String item) { return item; }

    @Override protected void onItemWon(String label) {
        if (spinCallback != null) spinCallback.accept(label);
    }
}
