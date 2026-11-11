package com.bhopui.mixin;

import com.bhopui.Spring;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Knob springs to new values (clicks, arrow keys, resets); follows the mouse directly while dragged. */
@Mixin(AbstractSliderButton.class)
public abstract class SliderMixin {
	@Shadow protected double value;
	@Shadow private boolean dragging;

	@Unique private Spring bhopui$knob;
	@Unique private double bhopui$real;

	@Inject(method = "extractWidgetRenderState", at = @At("HEAD"))
	private void bhopui$swapIn(GuiGraphicsExtractor g, int mx, int my, float pt, CallbackInfo ci) {
		bhopui$real = value;
		if (bhopui$knob == null) bhopui$knob = new Spring((float) value);
		if (dragging) bhopui$knob.snap((float) value);
		bhopui$knob.target = (float) value;
		value = bhopui$knob.update(Spring.SOFT_K, Spring.SOFT_C); // drawn position only, restored below
	}

	@Inject(method = "extractWidgetRenderState", at = @At("RETURN"))
	private void bhopui$swapOut(GuiGraphicsExtractor g, int mx, int my, float pt, CallbackInfo ci) {
		value = bhopui$real;
	}
}
