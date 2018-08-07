package me.realized.de.arenaregen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import me.realized.de.arenaregen.util.BlockInfo;
import me.realized.de.arenaregen.util.Position;

public class BlockChange {

    @Getter
    private final List<Position> spaces = new ArrayList<>();
    @Getter
    private final Map<Position, BlockInfo> changes = new HashMap<>();

    public void setSpace(final Position position) {
        spaces.add(position);
    }

    public void setModified(final Position position, final BlockInfo info) {
        if (changes.get(position) != null) {
            return;
        }

        changes.put(position, info);
    }
}
