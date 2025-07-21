import javafx.embed.swing.JFXPanel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.animation.RotateTransition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.example.Resultat;
import org.example.Participant;
import org.example.OptionRoue;
import org.example.bonus.Bonus;
import org.example.wheel.BonusWheel;
import org.example.wheel.BaseWheel;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class BonusWheelTest {

    @BeforeAll
    public static void initJFX() {
        // initializes JavaFX environment
        new JFXPanel();
    }

    @Test
    public void testUpdateWheelDisplayCreatesCorrectSectors() throws Exception {
        Resultat resultat = new Resultat();
        BonusWheel wheel = new BonusWheel(resultat);
        ObservableList<Bonus> bonus = FXCollections.observableArrayList(
                new Bonus("X"), new Bonus("Y"));

        wheel.updateWheelDisplay(bonus);

        Field seatNamesField = BaseWheel.class.getDeclaredField("seatNames");
        seatNamesField.setAccessible(true);
        String[] seatNames = (String[]) seatNamesField.get(wheel);
        assertArrayEquals(new String[]{"X", "Y"}, seatNames);

        Field arcsField = BaseWheel.class.getDeclaredField("arcs");
        arcsField.setAccessible(true);
        List<?> arcs = (List<?>) arcsField.get(wheel);
        assertEquals(2, arcs.size());
    }

    @Test
    public void testCallbackExecutedAfterSpin() throws Exception {
        Resultat resultat = new Resultat();
        BonusWheel wheel = new BonusWheel(resultat);
        ObservableList<Bonus> bonus = FXCollections.observableArrayList(new Bonus("Gain"));
        Participant player = new Participant("Alice", 10, "Mage");

        AtomicReference<Participant> playerRef = new AtomicReference<>();
        AtomicReference<String> bonusRef = new AtomicReference<>();
        wheel.setOnBonusWon((p, b) -> { playerRef.set(p); bonusRef.set(b); });

        Field durField = OptionRoue.class.getDeclaredField("spinDuration");
        durField.setAccessible(true);
        durField.setDouble(null, 0);

        wheel.spinTheWheel(bonus, player);

        Field spinField = BaseWheel.class.getDeclaredField("spinRT");
        spinField.setAccessible(true);
        RotateTransition rt = (RotateTransition) spinField.get(wheel);
        rt.getOnFinished().handle(new ActionEvent());

        assertSame(player, playerRef.get());
        assertEquals("Gain", bonusRef.get());
    }
}
