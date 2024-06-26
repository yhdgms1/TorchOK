package dev.yhdgms1;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import org.lwjgl.glfw.GLFW;

import java.util.Set;

public class TorchokClient implements ClientModInitializer {
	/*
	 * All torches except for redstone one
	 */
	protected static Set<Item> TORCHES = Set.of(Items.TORCH, Items.SOUL_TORCH);
	protected static KeyBinding switchAutoplacementKeyBinding = KeyBindingHelper.registerKeyBinding(
			new KeyBinding(
					"switch.torchok.autoplacement",
					InputUtil.Type.KEYSYM,
					GLFW.GLFW_KEY_RIGHT_ALT,
					"category.torchok.main"
			)
	);

	protected Boolean autoplacementEnabled = true;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(this::tick);
	}

	private void tick(MinecraftClient client) {
		if (client.player == null || client.world == null || client.interactionManager == null) {
			return;
		}

		if (switchAutoplacementKeyBinding.wasPressed()) {
			autoplacementEnabled = !autoplacementEnabled;

			var message = autoplacementEnabled ? Text.translatable("message.torchok.enabled") : Text.translatable("message.torchok.disabled");

			client.player.sendMessage(message, false);
		}

		if (!autoplacementEnabled) {
			return;
		}

		/*
		 * Using torch from second hand is much easier than using any from inventory
		 */
		if (!TORCHES.contains(client.player.getOffHandStack().getItem())) {
			return;
		}

		var playerBlockPosition = client.player.getBlockPos();

		if (client.world.getLightLevel(LightType.BLOCK, playerBlockPosition) > 4) {
			return;
		}

		if (!client.world.getBlockState(playerBlockPosition).getFluidState().isEmpty()) {
			return;
		}

		if (!Block.sideCoversSmallSquare(client.world, playerBlockPosition.down(), Direction.UP)) {
			return;
		}

		var hitVector = Vec3d.ofBottomCenter(playerBlockPosition);

		client.interactionManager.interactBlock(client.player, Hand.OFF_HAND, new BlockHitResult(hitVector, Direction.DOWN, playerBlockPosition, false));
		client.interactionManager.interactItem(client.player,Hand.OFF_HAND);
	}
}