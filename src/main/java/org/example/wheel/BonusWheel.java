package org.example.wheel;

import java.util.function.BiConsumer;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import org.example.Participant;
import org.example.Resultat;
import org.example.bonus.Bonus;

public final class BonusWheel extends BaseWheel<Bonus> {

    private static final Color FIRE_START  = Color.web("#00c6ff");
    private static final Color FIRE_END    = Color.web("#0072ff");
    private static final Color HIGHLIGHT   = Color.web("#00ffea");
    private static final String PREFIX     = "Bonus : ";

    private BiConsumer<? super Participant, ? super String> callback;
    private Participant currentPlayer;

    public BonusWheel(Resultat resultat) {
        super(resultat);
    }

    public void setOnBonusWon(BiConsumer<? super Participant, ? super String> cb) {
        callback = cb;
    }

    public void spinTheWheel(ObservableList<Bonus> bonuses, Participant player) {
        currentPlayer = player;
        super.spinTheWheel(bonuses);
    }

    @Override protected Color getFireStart()      { return FIRE_START; }
    @Override protected Color getFireEnd()        { return FIRE_END; }
    @Override protected Color getHighlightColor() { return HIGHLIGHT; }
    @Override protected String prefix()           { return PREFIX; }
    @Override protected String itemToString(Bonus b) { return b.description(); }

    @Override protected void onItemWon(String label) {
        if (callback != null && currentPlayer != null) callback.accept(currentPlayer, label);
    }
}
