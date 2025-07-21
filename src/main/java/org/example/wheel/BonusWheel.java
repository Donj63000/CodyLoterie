package org.example.wheel;

import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import java.util.function.BiConsumer;
import org.example.Resultat;
import org.example.Participant;
import org.example.bonus.Bonus;

public class BonusWheel extends BaseWheel<Bonus> {
    private BiConsumer<Participant, String> bonusCallback;
    private Participant currentPlayer;

    public BonusWheel(Resultat res) {
        super(res);
    }

    public void setOnBonusWon(BiConsumer<Participant,String> cb) { bonusCallback = cb; }

    public void spinTheWheel(ObservableList<Bonus> bonus, Participant p) {
        currentPlayer = p;
        super.spinTheWheel(bonus);
    }

    @Override
    protected Color getFireStart() { return Color.web("#00c6ff"); }

    @Override
    protected Color getFireEnd() { return Color.web("#0072ff"); }

    @Override
    protected Color getHighlightColor() { return Color.web("#00ffea"); }

    @Override
    protected String prefix() { return "Bonus : "; }

    @Override
    protected String itemToString(Bonus item) { return item.description(); }

    @Override
    protected void onItemWon(String label) {
        if (bonusCallback != null && currentPlayer != null)
            bonusCallback.accept(currentPlayer, label);
    }
}
