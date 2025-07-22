package org.example;

import javafx.collections.ObservableList;
import org.example.bonus.Bonus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public final class Save {

    private Save() {}

    private static final Path FILE = Path.of("loterie-save.txt");

    public static void save(ObservableList<Participant> participants) throws IOException {
        StringBuilder sb = new StringBuilder("#Participants\n");
        for (Participant p : participants) {
            String bonus = p.getBonusList().stream()
                    .map(Bonus::description)
                    .collect(Collectors.joining("|"));
            sb.append(p.getName()).append(';')
                    .append(p.getLevel()).append(';')
                    .append(p.getClasse()).append(';')
                    .append(bonus).append('\n');
        }
        Files.writeString(FILE, sb, StandardCharsets.UTF_8);
    }

    public static void reset(ObservableList<Participant> participants) {
        participants.clear();
    }
}
