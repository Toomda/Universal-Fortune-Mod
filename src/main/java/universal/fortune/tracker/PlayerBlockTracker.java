package universal.fortune.tracker;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerBlockTracker {
    private static final Map<World, Map<BlockPos, Boolean>> playerPlacedBlocks = new ConcurrentHashMap<>();

    public static void markBlockAsPlayerPlaced(World world, BlockPos pos) {
        playerPlacedBlocks
                .computeIfAbsent(world, w -> new ConcurrentHashMap<>())
                .put(pos, true);
    }

    public static boolean isPlayerPlacedBlock(World world, BlockPos pos) {
        return playerPlacedBlocks.getOrDefault(world, Map.of()).getOrDefault(pos, false);
    }

    public static void removeBlock(World world, BlockPos pos) {
        Map<BlockPos, Boolean> worldMap = playerPlacedBlocks.get(world);
        if (worldMap != null) {
            worldMap.remove(pos);
            if (worldMap.isEmpty()) {
                playerPlacedBlocks.remove(world);
            }
        }
    }
}
