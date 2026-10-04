package com.greenmod;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Field;

/** Shared helpers. */
public final class GreenMod {
	/** Light neon green. */
	public static final int NEON = 0x39FF14;
	/** Same colour as opaque ARGB. */
	public static final int NEON_ARGB = 0xFF39FF14;
	/** Spare bit in the synced "skin parts" byte that we use as a "uses this mod" flag. */
	public static final int USER_FLAG = 0x80;

	private static Component kelp;
	private static EntityDataAccessor<Byte> skinPartsAccessor;
	private static boolean accessorSearched;

	private GreenMod() {}

	/** Dried kelp block texture as an inline sprite (falls back to plain text if it can't be parsed). */
	public static Component kelpIcon() {
		if (kelp == null) {
			try {
				var json = JsonParser.parseString("{\"object\":\"atlas\",\"sprite\":\"minecraft:block/dried_kelp_side\"}");
				kelp = ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, json).result().orElse(Component.literal("[K]"));
			} catch (Throwable t) {
				kelp = Component.literal("[K]");
			}
		}
		return kelp;
	}

	@SuppressWarnings("unchecked")
	private static EntityDataAccessor<Byte> accessor() {
		if (!accessorSearched) {
			accessorSearched = true;
			for (Class<?> c = Player.class; c != null; c = c.getSuperclass()) {
				try {
					Field f = c.getDeclaredField("DATA_PLAYER_MODE_CUSTOMISATION");
					f.setAccessible(true);
					skinPartsAccessor = (EntityDataAccessor<Byte>) f.get(null);
					break;
				} catch (Throwable ignored) {
				}
			}
		}
		return skinPartsAccessor;
	}

	/** True for yourself and for any player whose client sends our flag bit. */
	public static boolean isModUser(Entity e) {
		if (e instanceof LocalPlayer) return true;
		if (!(e instanceof Player p)) return false;
		EntityDataAccessor<Byte> acc = accessor();
		if (acc == null) return false;
		try {
			return (p.getEntityData().get(acc) & USER_FLAG) != 0;
		} catch (Throwable t) {
			return false;
		}
	}
}
