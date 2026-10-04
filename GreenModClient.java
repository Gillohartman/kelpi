package com.greenmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class GreenModClient implements ClientModInitializer {
	public static final String MOD_ID = "greenmod";

	@Override
	public void onInitializeClient() {
		// View bobbing: always off
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.options.bobView().get()) {
				client.options.bobView().set(false);
			}
		});

		// 2D green hitboxes
		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "hitboxes"), HitboxHud::render);

		// Replace vanilla crosshair with a clean green one
		HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, original -> (graphics, delta) -> {
			Minecraft mc = Minecraft.getInstance();
			if (!mc.options.getCameraType().isFirstPerson()) return;
			int cx = mc.getWindow().getGuiScaledWidth() / 2;
			int cy = mc.getWindow().getGuiScaledHeight() / 2;
			int c = GreenMod.NEON_ARGB;
			int gap = 2, len = 4;
			graphics.fill(cx - gap - len, cy, cx - gap, cy + 1, c); // left
			graphics.fill(cx + gap + 1, cy, cx + gap + 1 + len, cy + 1, c); // right
			graphics.fill(cx, cy - gap - len, cx + 1, cy - gap, c); // top
			graphics.fill(cx, cy + gap + 1, cx + 1, cy + gap + 1 + len, c); // bottom
		});

		// Inventory tweak: Ctrl+R in the inventory merges stacks and sorts
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof InventoryScreen) {
				ScreenKeyboardEvents.afterKeyPress(screen).register((s, keyEvent) -> {
					boolean ctrl = (keyEvent.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0;
					if (ctrl && keyEvent.key() == GLFW.GLFW_KEY_R) {
						InventorySorter.sort();
					}
				});
			}
		});
	}
}
