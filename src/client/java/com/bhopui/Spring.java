package com.bhopui;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Damped spring (mass 1), stepped with real time every frame, never per tick. */
public final class Spring {
	public static final float FAST_K = 2500, FAST_C = 72;   // ~4% overshoot, rests in ~130 ms
	public static final float SOFT_K = 332, SOFT_C = 23.4f; // ~7% overshoot, peak 225 ms, rests ~340 ms

	/** Animation speed multiplier from config/bhopui.properties; 0 turns animations off. */
	public static final float SPEED = loadSpeed();

	public float pos, vel, target;
	private long last = System.nanoTime();

	public Spring(float value) { pos = target = value; }

	/** Advances to now and returns the current position. Keeps velocity when the target changes mid-flight. */
	public float update(float k, float c) {
		long now = System.nanoTime();
		float dt = Math.min((now - last) / 1e9f, 0.05f) * SPEED; // clamp so a lag spike does not fling things
		last = now;
		if (SPEED <= 0) return snap(target);
		for (; dt > 0; dt -= 0.001f) { // 1 ms substeps keep the stiff spring stable
			float h = Math.min(dt, 0.001f);
			vel += (k * (target - pos) - c * vel) * h;
			pos += vel * h;
		}
		if (Math.abs(target - pos) < 1e-4f && Math.abs(vel) < 1e-3f) snap(target);
		return pos;
	}

	public float snap(float value) { pos = target = value; vel = 0; last = System.nanoTime(); return pos; }

	public boolean settled() { return pos == target && vel == 0; }

	private static float loadSpeed() {
		Path file = Path.of("config", "bhopui.properties");
		Properties p = new Properties();
		try {
			if (Files.exists(file)) {
				try (Reader r = Files.newBufferedReader(file)) { p.load(r); }
			} else {
				Files.createDirectories(file.getParent());
				try (Writer w = Files.newBufferedWriter(file)) {
					w.write("# Animation speed: 1 = normal, 2 = twice as fast, 0.5 = slow motion, 0 = off\nspeed=1\n");
				}
			}
			return Math.max(0, Float.parseFloat(p.getProperty("speed", "1").trim()));
		} catch (IOException | NumberFormatException e) {
			return 1;
		}
	}

	/** Self-check of the measured overshoots: java src/client/java/com/bhopui/Spring.java */
	public static void main(String[] args) {
		check(FAST_K, FAST_C, 0.04f);
		check(SOFT_K, SOFT_C, 0.07f);
		System.out.println("springs ok");
	}

	private static void check(float k, float c, float expected) {
		float pos = 0, vel = 0, max = 0;
		for (int i = 0; i < 2000; i++) { vel += (k * (1 - pos) - c * vel) * 0.001f; pos += vel * 0.001f; max = Math.max(max, pos); }
		if (Math.abs((max - 1) - expected) > 0.01f) throw new AssertionError("overshoot " + (max - 1) + ", expected " + expected);
	}
}
