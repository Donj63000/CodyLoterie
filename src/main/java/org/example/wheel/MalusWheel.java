package org.example.wheel;

import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import java.util.function.Consumer;
import org.example.Resultat;

public class MalusWheel extends BaseWheel<String> {
    private Consumer<String> spinCallback;

    public MalusWheel(Resultat res) {
        super(res);
    }

    public void setOnSpinFinished(Consumer<String> cb) { spinCallback = cb; }

    @Override
    protected Color getFireStart() { return Color.web("#ff5722"); }

    @Override
    protected Color getFireEnd() { return Color.web("#8b0000"); }

    @Override
    protected Color getHighlightColor() { return Color.web("#ff2200"); }

    @Override
    protected String prefix() { return "Malus : "; }

    @Override
    protected String itemToString(String item) { return item; }

    @Override
    protected void onItemWon(String label) { if (spinCallback != null) spinCallback.accept(label); }
}
