package com.bhopui.mixin;

import com.bhopui.Spring;
import com.bhopui.SpringWidget;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Menu pops open (77% -> slight overshoot -> 100%) and its widgets are dealt in top to bottom. */
@Mixin(Screen.class)
public abstract class ScreenMixin {
	@Shadow public int width;
	@Shadow public int height;
	@Shadow public abstract List<? extends GuiEventListener> children();

	@Unique private final Spring bhopui$scale = new Spring(1);
	@Unique private boolean bhopui$justOpened;

	@Inject(method = "added", at = @At("HEAD"))
	private void bhopui$open(CallbackInfo ci) {
		bhopui$scale.snap(0.77f);
		bhopui$scale.target = 1;
		bhopui$justOpened = true;
	}

	// init(int,int) also runs on window resize; only cascade the first time after opening.
	@Inject(method = "init(II)V", at = @At("RETURN"))
	private void bhopui$cascade(int w, int h, CallbackInfo ci) {
		if (!bhopui$justOpened) return;
		bhopui$justOpened = false;
		if ((Object) this instanceof TitleScreen) return; // it already fades its own buttons in
		List<AbstractWidget> widgets = children().stream()
			.filter(AbstractWidget.class::isInstance).map(AbstractWidget.class::cast)
			.sorted(Comparator.comparingInt(AbstractWidget::getY).thenComparingInt(AbstractWidget::getX))
			.toList();
		long stepMs = widgets.size() > 1 ? Math.min(45, 400 / (widgets.size() - 1)) : 0; // whole cascade <= 400 ms
		long now = System.nanoTime();
		for (int i = 0; i < widgets.size(); i++)
			((SpringWidget) widgets.get(i)).bhopui$cascadeAt(now + (long) (i * stepMs * 1_000_000L / Math.max(Spring.SPEED, 0.01f)));
	}

	// Scale only the foreground; the background stays full-screen. Clicks still use the real layout.
	@Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/gui/screens/Screen;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"))
	private void bhopui$push(GuiGraphicsExtractor g, int mx, int my, float pt, CallbackInfo ci) {
		float s = bhopui$scale.update(Spring.FAST_K, Spring.FAST_C);
		g.pose().pushMatrix();
		g.pose().translate(width / 2f, height / 2f).scale(s).translate(-width / 2f, -height / 2f);
	}

	@Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
		target = "Lnet/minecraft/client/gui/screens/Screen;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"))
	private void bhopui$pop(GuiGraphicsExtractor g, int mx, int my, float pt, CallbackInfo ci) {
		g.pose().popMatrix();
	}
}
