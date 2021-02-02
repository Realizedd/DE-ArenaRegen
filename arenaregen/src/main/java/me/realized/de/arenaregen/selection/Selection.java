package me.realized.de.arenaregen.selection;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

public class Selection {

    @Getter
    @Setter
    private Location first, second;

    public boolean isSelected() {
        return first != null && second != null;
    }
}
