package universal.fortune;

import net.fabricmc.api.ModInitializer;

import net.minecraft.block.Blocks;
import universal.fortune.tracker.PlayerBlockTracker;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

public class UniversalFortune implements ModInitializer {
	public static final String MOD_ID = "universal-fortune";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final Set<Block> FORTUNE_COMPATIBLE_BLOCKS = Set.of(
			// Oress
			Blocks.COAL_ORE,
			Blocks.DEEPSLATE_COAL_ORE,
			Blocks.IRON_ORE,
			Blocks.DEEPSLATE_IRON_ORE,
			Blocks.COPPER_ORE,
			Blocks.DEEPSLATE_COPPER_ORE,
			Blocks.GOLD_ORE,
			Blocks.DEEPSLATE_GOLD_ORE,
			Blocks.REDSTONE_ORE,
			Blocks.DEEPSLATE_REDSTONE_ORE,
			Blocks.LAPIS_ORE,
			Blocks.DEEPSLATE_LAPIS_ORE,
			Blocks.DIAMOND_ORE,
			Blocks.DEEPSLATE_DIAMOND_ORE,
			Blocks.EMERALD_ORE,
			Blocks.DEEPSLATE_EMERALD_ORE,
			Blocks.NETHER_QUARTZ_ORE,
			Blocks.NETHER_GOLD_ORE,

			// Plants
			Blocks.WHEAT,
			Blocks.CARROTS,
			Blocks.POTATOES,
			Blocks.BEETROOTS,
			Blocks.MELON,
			Blocks.PUMPKIN,
			Blocks.SWEET_BERRY_BUSH,
			Blocks.NETHER_WART,

			// Miscellaneous Blocks
			Blocks.GLOWSTONE,
			Blocks.SEA_LANTERN,
			Blocks.GRAVEL,
			Blocks.GRASS,
			Blocks.SNOW,
			Blocks.CLAY,
			Blocks.AMETHYST_CLUSTER,

			// Leaves
			Blocks.OAK_LEAVES,
			Blocks.DARK_OAK_LEAVES,
			Blocks.BIRCH_LEAVES,
			Blocks.SPRUCE_LEAVES,
			Blocks.ACACIA_LEAVES,
			Blocks.JUNGLE_LEAVES,
			Blocks.MANGROVE_LEAVES,
			Blocks.CHERRY_LEAVES
	);

	@Override
	public void onInitialize() {
		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, entity) -> {
			if (world == null || player == null || pos == null || state == null || FORTUNE_COMPATIBLE_BLOCKS.contains(state.getBlock())) {
				return true;
			}
			return this.onBlockBreak(world, player, pos, state);
		});

		net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (!world.isClient && !FORTUNE_COMPATIBLE_BLOCKS.contains(world.getBlockState(hitResult.getBlockPos().offset(hitResult.getSide())).getBlock())) {
				BlockPos pos = hitResult.getBlockPos().offset(hitResult.getSide());
				PlayerBlockTracker.markBlockAsPlayerPlaced(world, pos);
			}
			return net.minecraft.util.ActionResult.PASS;
		});
	}

	private boolean onBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state) {
		if (!(world instanceof ServerWorld serverWorld)) return true;

		if (PlayerBlockTracker.isPlayerPlacedBlock(serverWorld, pos)) {
			PlayerBlockTracker.removeBlock(serverWorld, pos);
			return true;
		}

		ItemStack tool = player.getMainHandStack();
		int fortuneLevel = EnchantmentHelper.getLevel(Enchantments.FORTUNE, tool);

		List<ItemStack> drops;

		drops = Block.getDroppedStacks(state, serverWorld, pos, null, player, tool);
		Random random = serverWorld.random;
		for (ItemStack stack : drops) {
			int originalCount = stack.getCount();
			int newCount = originalCount;

			if(fortuneLevel > 0) {
				newCount = calculateDrops(fortuneLevel, random, originalCount);
			}

			stack.setCount(newCount);
		}


		serverWorld.breakBlock(pos, false);
		drops.forEach(stack -> Block.dropStack(serverWorld, pos, stack));

		return false;
	}

	private int calculateDrops(int fortuneLevel, Random random, int originalCount) {
		switch (fortuneLevel) {
			case 1:
				return originalCount * (random.nextFloat() < 0.33f ? 2 : 1);
			case 2:
				return originalCount * (random.nextFloat() < 0.67f ? 2 : 1);
			case 3:
				float chance = random.nextFloat();
				if (chance < 0.5f) {
					return originalCount * 3;
				} else if (chance < 0.8f) {
					return originalCount * 2;
				} else {
					return originalCount;
				}
			default:
				return originalCount;
		}
	}
}