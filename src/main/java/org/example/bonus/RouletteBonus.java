package org.example.bonus;

import java.util.function.BiConsumer;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import org.example.Participant;
import org.example.Resultat;
import org.example.wheel.BaseWheel;

public final class RouletteBonus extends BaseWheel<Bonus> {

    private static final Color FIRE_START  = Color.web("#00c6ff");
    private static final Color FIRE_END    = Color.web("#0072ff");
    private static final Color HIGHLIGHT   = Color.web("#00ffea");
    private static final String PREFIX     = "Bonus : ";

    private BiConsumer<Participant, String> callback;
    private Participant currentPlayer;

    public RouletteBonus(Resultat resultat) {
        super(resultat);
    }

    public void setOnBonusWon(BiConsumer<Participant, String> cb) {
        callback = cb;
    }

    public void spinTheWheel(ObservableList<Bonus> bonus, Participant player) {
        currentPlayer = player;
        super.spinTheWheel(bonus);
    }

    @Override protected Color getFireStart()      { return FIRE_START; }
    @Override protected Color getFireEnd()        { return FIRE_END; }
    @Override protected Color getHighlightColor() { return HIGHLIGHT; }
    @Override protected String prefix()           { return PREFIX; }
    @Override protected String itemToString(Bonus item) { return item.description(); }

    @Override protected void onItemWon(String label) {
        if (callback != null && currentPlayer != null) callback.accept(currentPlayer, label);
    }
}
