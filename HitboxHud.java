package com.greenmod;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Draws a flat (2D) neon green rectangle around every visible player. First person only. */
public final class HitboxHud {
	private HitboxHud() {}

	public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer me = mc.player;
		ClientLevel level = mc.level;
		if (me == null || level == null) return;
		if (!mc.options.getCameraType().isFirstPerson()) return;

		float pt = delta.getGameTimeDeltaPartialTick(false);
		Vec3 eye = me.getEyePosition(pt);
		double yaw = Math.toRadians(me.getViewYRot(pt));
		double pitch = Math.toRadians(me.getViewXRot(pt));

		// camera basis (Minecraft: yaw 0 = +Z, pitch positive = looking down)
		double fx = -Math.sin(yaw) * Math.cos(pitch), fy = -Math.sin(pitch), fz = Math.cos(yaw) * Math.cos(pitch);
		double rx = -Math.cos(yaw), ry = 0, rz = -Math.sin(yaw);
		double ux = ry * fz - rz * fy, uy = rz * fx - rx * fz, uz = rx * fy - ry * fx;

		int w = mc.getWindow().getGuiScaledWidth();
		int h = mc.getWindow().getGuiScaledHeight();
		double tanHalf = Math.tan(Math.toRadians(mc.options.fov().get()) / 2.0);
		double aspect = w / (double) h;

		for (AbstractClientPlayer p : level.players()) {
			if (p == me || p.isSpectator()) continue;
			if (me.distanceToSqr(p) > 96 * 96) continue;
			if (!me.hasLineOfSight(p)) continue;

			double ox = Mth.lerp(pt, p.xOld, p.getX()) - p.getX();
			double oy = Mth.lerp(pt, p.yOld, p.getY()) - p.getY();
			double oz = Mth.lerp(pt, p.zOld, p.getZ()) - p.getZ();
			AABB bb = p.getBoundingBox();

			double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
			boolean visible = true;
			for (int i = 0; i < 8 && visible; i++) {
				double cx = ((i & 1) == 0 ? bb.minX : bb.maxX) + ox;
				double cy = ((i & 2) == 0 ? bb.minY : bb.maxY) + oy;
				double cz = ((i & 4) == 0 ? bb.minZ : bb.maxZ) + oz;
				double dx = cx - eye.x, dy = cy - eye.y, dz = cz - eye.z;
				double z = dx * fx + dy * fy + dz * fz;
				if (z < 0.1) { visible = false; break; }
				double x = dx * rx + dy * ry + dz * rz;
				double y = dx * ux + dy * uy + dz * uz;
				double sx = w / 2.0 + (x / (z * tanHalf * aspect)) * (w / 2.0);
				double sy = h / 2.0 - (y / (z * tanHalf)) * (h / 2.0);
				minX = Math.min(minX, sx); maxX = Math.max(maxX, sx);
				minY = Math.min(minY, sy); maxY = Math.max(maxY, sy);
			}
			if (!visible) continue;

			int x1 = (int) Math.floor(minX), x2 = (int) Math.ceil(maxX);
			int y1 = (int) Math.floor(minY), y2 = (int) Math.ceil(maxY);
			if (x2 - x1 > w * 4 || y2 - y1 > h * 4) continue;

			int c = GreenMod.NEON_ARGB;
			g.fill(x1, y1, x2, y1 + 1, c);       // top
			g.fill(x1, y2 - 1, x2, y2, c);       // bottom
			g.fill(x1, y1, x1 + 1, y2, c);       // left
			g.fill(x2 - 1, y1, x2, y2, c);       // right
		}
	}
}
