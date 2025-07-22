package org.example.bonus;

import java.util.Objects;

public record Bonus(String description) {
    public Bonus {
        description = Objects.requireNonNull(description, "description").trim();
        if (description.isEmpty()) throw new IllegalArgumentException("description vide");
    }
}
