package com.greenmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

/** Merges partial stacks and sorts the 27 main inventory slots. */
public final class InventorySorter {
	private static final int FIRST = 9;   // first main-inventory slot in the player menu
	private static final int LAST = 35;   // last main-inventory slot

	private InventorySorter() {}

	private static final Comparator<ItemStack> ORDER = (a, b) -> {
		if (a.isEmpty() && b.isEmpty()) return 0;
		if (a.isEmpty()) return 1;
		if (b.isEmpty()) return -1;
		int c = key(a).compareTo(key(b));
		return c != 0 ? c : Integer.compare(b.getCount(), a.getCount());
	};

	private static String key(ItemStack s) {
		return BuiltInRegistries.ITEM.getKey(s.getItem()).toString();
	}

	public static void sort() {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		MultiPlayerGameMode gm = mc.gameMode;
		if (player == null || gm == null) return;
		if (!player.containerMenu.getCarried().isEmpty()) return; // don't mess with a held item

		int id = player.containerMenu.containerId;

		// 1) merge partial stacks of the same item
		for (int i = FIRST; i <= LAST; i++) {
			for (int j = i + 1; j <= LAST; j++) {
				ItemStack a = slot(player, i);
				ItemStack b = slot(player, j);
				if (a.isEmpty() || b.isEmpty()) continue;
				if (!ItemStack.isSameItemSameComponents(a, b)) continue;
				if (a.getCount() >= a.getMaxStackSize()) break;
				click(gm, id, j, player);
				click(gm, id, i, player);
				if (!player.containerMenu.getCarried().isEmpty()) click(gm, id, j, player);
			}
		}

		// 2) selection sort using 3-click swaps
		for (int i = FIRST; i <= LAST; i++) {
			int best = i;
			for (int j = i + 1; j <= LAST; j++) {
				if (ORDER.compare(slot(player, j), slot(player, best)) < 0) best = j;
			}
			if (best != i && ORDER.compare(slot(player, best), slot(player, i)) != 0) {
				click(gm, id, best, player);
				click(gm, id, i, player);
				if (!player.containerMenu.getCarried().isEmpty()) click(gm, id, best, player);
			}
		}
	}

	private static ItemStack slot(LocalPlayer p, int index) {
		return p.containerMenu.slots.get(index).getItem();
	}

	private static void click(MultiPlayerGameMode gm, int containerId, int slot, LocalPlayer p) {
		gm.handleInventoryMouseClick(containerId, slot, 0, ClickType.PICKUP, p);
	}
}
