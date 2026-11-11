package com.bhopui.mixin;

import com.bhopui.Spring;
import com.bhopui.SpringWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Per-widget springs: cascade entrance, button hover/press, text field focus pulse. Drawing only; hitboxes untouched. */
@Mixin(AbstractWidget.class)
public abstract class WidgetMixin implements SpringWidget {
	@Shadow public boolean active;
	@Shadow public abstract int getX();
	@Shadow public abstract int getY();
	@Shadow public abstract int getWidth();
	@Shadow public abstract int getHeight();
	@Shadow public abstract boolean isHovered();
	@Shadow public abstract boolean isFocused();
	@Shadow public abstract void setAlpha(float alpha);

	@Unique private final Spring bhopui$size = new Spring(1);
	@Unique private final Spring bhopui$appear = new Spring(1);
	@Unique private long bhopui$appearStart;
	@Unique private boolean bhopui$wasFocused;

	@Override
	public void bhopui$cascadeAt(long startNanos) {
		if (Spring.SPEED <= 0) return;
		bhopui$appear.snap(0);
		bhopui$appearStart = startNanos;
		setAlpha(0);
	}

	@Inject(method = "extractRenderState", at = @At("HEAD"))
	private void bhopui$push(GuiGraphicsExtractor g, int mx, int my, float pt, CallbackInfo ci) {
		Object self = this;
		if (self instanceof AbstractButton) {
			boolean hovered = active && isHovered();
			bhopui$size.target = hovered && Minecraft.getInstance().mouseHandler.isLeftPressed() ? 0.95f : hovered ? 1.04f : 1;
		} else if (self instanceof EditBox) {
			if (isFocused() && !bhopui$wasFocused) bhopui$size.vel += 2.2f; // kick: ~100% -> 102% -> 100%
			bhopui$wasFocused = isFocused();
		}
		// Hover uses the soft spring; press, release and focus the fast one.
		boolean soft = bhopui$size.target == 1.04f;
		float size = bhopui$size.update(soft ? Spring.SOFT_K : Spring.FAST_K, soft ? Spring.SOFT_C : Spring.FAST_C);

		float lift = 0;
		if (bhopui$appearStart != 0) { // cascading; a waiting widget is "settled" at 0, so track it explicitly
			if (System.nanoTime() >= bhopui$appearStart) bhopui$appear.target = 1;
			float p = bhopui$appear.update(Spring.SOFT_K, Spring.SOFT_C);
			if (bhopui$appear.settled() && p == 1) bhopui$appearStart = 0;
			setAlpha(Math.clamp(p, 0, 1));
			size *= 0.9f + 0.1f * p;
			lift = 8 * (1 - p);
		}

		float cx = getX() + getWidth() / 2f, cy = getY() + getHeight() / 2f;
		g.pose().pushMatrix();
		g.pose().translate(cx, cy + lift).scale(size).translate(-cx, -cy);
	}

	@Inject(method = "extractRenderState", at = @At("RETURN"))
	private void bhopui$pop(GuiGraphicsExtractor g, int mx, int my, float pt, CallbackInfo ci) {
		g.pose().popMatrix();
	}
}
