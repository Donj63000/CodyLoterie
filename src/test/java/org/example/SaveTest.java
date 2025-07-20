import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.bonus.Bonus;
import org.example.Participant;
import org.example.Save;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class SaveTest {

    private Path file;

    @BeforeEach
    public void setup() throws IOException {
        file = Path.of("loterie-save.txt");
        Files.deleteIfExists(file);
    }

    @AfterEach
    public void cleanup() throws IOException {
        Files.deleteIfExists(file);
    }

    @Test
    public void testSaveFileContent() throws IOException {
        ObservableList<Participant> participants = FXCollections.observableArrayList();
        Participant p = new Participant("Alice", 12, "Mage");
        p.addBonus(new Bonus("Chance"));
        participants.add(p);

        Save.save(participants);

        String expected = "#Participants\nAlice;12;Mage;Chance\n";
        String content = Files.readString(file);
        assertEquals(expected, content);
    }

    @Test
    public void testResetClearsList() {
        ObservableList<Participant> participants = FXCollections.observableArrayList();
        participants.add(new Participant("Bob", 5, "Guerrier"));
        Save.reset(participants);
        assertTrue(participants.isEmpty());
    }
}
