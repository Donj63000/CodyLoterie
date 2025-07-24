package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.bonus.Bonus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Save {

    private Save() { }

    private static final Path FILE = Path.of("loterie-save.txt");

    public static void save(ObservableList<Participant> participants,
                            ObservableList<String> malus,
                            ObservableList<Bonus> bonusMaster) throws IOException {

        StringBuilder sb = new StringBuilder();

        sb.append("#Malus\n");
        for (String m : malus) sb.append(m.replace('\n', ' ')).append('\n');

        sb.append("#BonusMaster\n");
        for (Bonus b : bonusMaster) sb.append(b.description().replace('\n', ' ')).append('\n');

        sb.append("#Participants\n");
        for (Participant p : participants) {
            String bonus = String.join("|",
                    p.getBonusList().stream()
                            .map(Bonus::description)
                            .toList());
            sb.append(p.getName()).append(';')
                    .append(p.getLevel()).append(';')
                    .append(p.getClasse()).append(';')
                    .append(bonus).append('\n');
        }

        Files.writeString(FILE, sb.toString(), StandardCharsets.UTF_8);
    }

    public static void load(ObservableList<Participant> participants,
                            ObservableList<String> malus,
                            ObservableList<Bonus> bonusMaster) throws IOException {

        participants.clear();
        malus.clear();
        bonusMaster.clear();

        if (!Files.exists(FILE)) return;

        int section = 0; // 1=malus, 2=bonusMaster, 3=participants
        for (String raw : Files.readAllLines(FILE, StandardCharsets.UTF_8)) {
            String line = raw.trim();
            if (line.isEmpty()) continue;
            if (line.startsWith("#")) {
                if (line.equalsIgnoreCase("#Malus"))        section = 1;
                else if (line.equalsIgnoreCase("#BonusMaster")) section = 2;
                else if (line.equalsIgnoreCase("#Participants")) section = 3;
                else section = 0;
                continue;
            }
            switch (section) {
                case 1 -> malus.add(line);
                case 2 -> bonusMaster.add(new Bonus(line));
                case 3 -> {
                    String[] p = line.split(";", -1);
                    if (p.length < 3) continue;
                    Participant part = new Participant(p[0], Integer.parseInt(p[1]), p[2]);
                    if (p.length >= 4 && !p[3].isBlank())
                        for (String b : p[3].split("\\|"))
                            part.addBonus(new Bonus(b));
                    participants.add(part);
                }
            }
        }
    }

    public static void reset(ObservableList<Participant> participants,
                             ObservableList<String> malus,
                             ObservableList<Bonus> bonusMaster) {
        participants.clear();
        malus.clear();
        bonusMaster.clear();
    }
}
