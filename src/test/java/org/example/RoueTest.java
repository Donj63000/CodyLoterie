import javafx.embed.swing.JFXPanel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.example.Resultat;
import org.example.Roue;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RoueTest {

    @BeforeAll
    public static void initJFX() {
        // initializes JavaFX environment
        new JFXPanel();
    }

    @Test
    public void testWheelUpdateCreatesCorrectSeats() throws Exception {
        Resultat resultat = new Resultat();
        Roue roue = new Roue(resultat);
        ObservableList<String> malus = FXCollections.observableArrayList("A", "B", "C");

        roue.updateWheelDisplay(malus);

        Field seatNamesField = Roue.class.getDeclaredField("seatNames");
        seatNamesField.setAccessible(true);
        String[] seatNames = (String[]) seatNamesField.get(roue);
        assertArrayEquals(new String[]{"A", "B", "C"}, seatNames);

        Field arcsField = Roue.class.getDeclaredField("arcs");
        arcsField.setAccessible(true);
        List<?> arcs = (List<?>) arcsField.get(roue);
        assertEquals(3, arcs.size());
    }
}
