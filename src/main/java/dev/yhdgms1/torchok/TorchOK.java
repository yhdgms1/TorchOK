package dev.yhdgms1.torchok;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;

public class TorchOK implements ModInitializer {
	public static final String MOD_ID = "torchok";
	public static final String SERVER_CONFIG_FILENAME = MOD_ID + ".server.json";

	private final static List<Item> SUPPORTED_TORCHES = new ArrayList<>();
	private final static Map<UUID, Instant> TIMEOUT_TABLE = new HashMap<>();

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ServerConfig config = ServerConfigReader.readConfig();

		config.torches.forEach((torch) -> {
            BuiltInRegistries.ITEM.getOptional(Identifier.parse(torch)).ifPresent(SUPPORTED_TORCHES::add);
        });

		PayloadTypeRegistry.clientboundPlay().register(SupportedTorchesS2CPayload.TYPE, SupportedTorchesS2CPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(PlaceTorchC2SPayload.TYPE, PlaceTorchC2SPayload.CODEC);

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			SupportedTorchesS2CPayload payload = new SupportedTorchesS2CPayload(config.torches);
			ServerPlayNetworking.send(handler.player, payload);
		});

		ServerPlayNetworking.registerGlobalReceiver(PlaceTorchC2SPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
            ServerLevel level = player.level();

            if (!player.onGround()) {
                return;
            }

            UUID playerUUID = player.getUUID();

            Instant lastTime = TIMEOUT_TABLE.get(playerUUID);

            if (lastTime != null && lastTime.plusMillis(config.placeTimeout).isAfter(Instant.now())) {
                return;
            }

            if (player.isSpectator()) {
                return;
            }

            BlockPos placePos = payload.pos();
            BlockState placeState = level.getBlockState(placePos);

            BlockPos below = placePos.below();
            BlockState stateBelow = level.getBlockState(below);

            if (!stateBelow.isSolid() && !stateBelow.isFaceSturdy(level, below, Direction.UP)) {
                return;
            }

            if (!placeState.getFluidState().isEmpty()) {
                return;
            }

            if (!placeState.canBeReplaced()) {
                return;
            }

            if (SUPPORTED_TORCHES.contains(placeState.getBlock().asItem())) {
                return;
            }

            Inventory inventory = player.getInventory();

            Optional<ItemStack> torch = Optional.empty();

            for (ItemStack stack : inventory) {
                if (SUPPORTED_TORCHES.contains(stack.getItem())) {
                    torch = Optional.of(stack);
                    break;
                }
            }

            if (torch.isEmpty()) {
                return;
            }

            ItemStack stack = torch.get();

            if (!(stack.getItem() instanceof BlockItem blockItem)) {
                return;
            }

            Block block = blockItem.getBlock();
            BlockState state = block.defaultBlockState();

            if (!player.gameMode.isCreative()) {
                stack.shrink(1);
            }

            level.setBlock(placePos, state, 3);

            TIMEOUT_TABLE.put(playerUUID, Instant.now());
		});
	}
}