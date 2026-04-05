package dev.yhdgms1.torchok.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.yhdgms1.torchok.PlaceTorchC2SPayload;
import dev.yhdgms1.torchok.SupportedTorchesS2CPayload;
import dev.yhdgms1.torchok.TorchOK;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class TorchOKClient implements ClientModInitializer {
	private final static List<ItemStack> SUPPORTED_TORCHES = new ArrayList<>();

	private static KeyMapping keyBinding;
	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(TorchOK.MOD_ID, "key-bind-category"));

	@Override
	public void onInitializeClient() {
		MidnightConfig.init(TorchOK.MOD_ID, ClientConfig.class);

		keyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.torchok.toggle-autoplacement", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_CONTROL, CATEGORY));

		ClientPlayNetworking.registerGlobalReceiver(
				SupportedTorchesS2CPayload.TYPE,
				(payload, context) -> {
					context.client().execute(() -> {
						payload.torches().forEach((torch) -> {
                            BuiltInRegistries.ITEM.getOptional(Identifier.parse(torch)).ifPresent(item -> SUPPORTED_TORCHES.add(new ItemStack(item)));
                        });
					});
				}
		);

		ClientTickEvents.END_CLIENT_TICK.register((client) -> {
			while (keyBinding.consumeClick()) {
				ClientConfig.enabled = !ClientConfig.enabled;
				MidnightConfig.write(TorchOK.MOD_ID);
			}

			if (!ClientConfig.enabled) {
				return;
			}

			if (client.player == null || client.level == null || client.player.isSpectator() || !client.player.onGround()) {
				return;
			}

			BlockPos placePos = client.player.blockPosition();
			BlockState placeState = client.level.getBlockState(placePos);

			BlockPos below = placePos.below();
			BlockState stateBelow = client.level.getBlockState(below);

			if (!stateBelow.isSolid() && !stateBelow.isFaceSturdy(client.level, below, Direction.UP)) {
				return;
			}

			if (!placeState.getFluidState().isEmpty()) {
				return;
			}

			if (client.level.getMaxLocalRawBrightness(placePos) > ClientConfig.threshold) {
				return;
			}

			if (!placeState.canBeReplaced()) {
				return;
			}

			if (SUPPORTED_TORCHES.contains(placeState.getBlock().asItem())) {
				return;
			}

			Inventory inventory = client.player.getInventory();

			if (SUPPORTED_TORCHES.stream().noneMatch(inventory::contains)) {
				return;
			}

			ClientPlayNetworking.send(new PlaceTorchC2SPayload(placePos));
		});
	}
}