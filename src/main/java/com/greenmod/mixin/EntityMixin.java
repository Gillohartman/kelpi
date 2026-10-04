package com.greenmod.mixin;

import com.greenmod.GreenMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

	/** Neon green nametags + kelp badge for other mod users. */
	@Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true, require = 0)
	private void greenmod$nametag(CallbackInfoReturnable<Component> cir) {
		Entity self = (Entity) (Object) this;
		if (!(self.level() instanceof ClientLevel)) return;

		MutableComponent name = Component.literal(cir.getReturnValue().getString())
				.withStyle(style -> style.withColor(GreenMod.NEON));

		if (self instanceof AbstractClientPlayer && GreenMod.isModUser(self)) {
			cir.setReturnValue(Component.empty().append(GreenMod.kelpIcon()).append(" ").append(name));
		} else {
			cir.setReturnValue(name);
		}
	}

	/** Low fire: you never see yourself burning. */
	@Inject(method = "isOnFire", at = @At("HEAD"), cancellable = true, require = 0)
	private void greenmod$noFire(CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this == Minecraft.getInstance().player) {
			cir.setReturnValue(false);
		}
	}
}
