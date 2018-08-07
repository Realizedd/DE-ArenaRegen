package me.realized.de.arenaregen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import me.realized.de.arenaregen.util.Position;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class BlockChange {

    @Getter
    private final Set<Position> spaces = new HashSet<>();
    @Getter
    private final Map<Location, Block> places = new HashMap<>();

    public BlockChange() {}

    private class BlockData {

        @Getter
        private final Material type;
        @Getter
        private final byte data;

        @SuppressWarnings("deprecation")
        public BlockData(final Block block) {
            this.type = block.getType();
            this.data = block.getData();
        }
    }
}
