package com.greenmod.mixin;

import com.greenmod.GreenMod;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Options.class)
public abstract class OptionsMixin {

	/**
	 * The client tells the server which skin parts are visible (7 bits used).
	 * The server syncs that byte to every other player, so we use the free 8th bit
	 * as a "this player runs the mod" marker. Works on servers without the mod.
	 */
	@ModifyVariable(method = "buildPlayerInformation", at = @At("STORE"), ordinal = 0, require = 0)
	private int greenmod$markUser(int mask) {
		return mask | GreenMod.USER_FLAG;
	}
}
