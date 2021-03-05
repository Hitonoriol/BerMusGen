package hmusgen;

import java.util.Random;

public class GenMain {
	private static Random random = new Random();

	public static void main(String[] args) {
		MelodyPlayer player = new MelodyPlayer();
		MelodyGenerator generator = new MelodyGenerator(player);
		player.setMelody(generator.generate(127));
		generator.randomizeInstruments();
		player.play();

	}

	static int rand(int min, int max) {
		if (min >= max)
			return min;

		return random.nextInt(max + 1 - min) + min;
	}

	static <T> T pick(T[] arr) {
		return arr[random.nextInt(arr.length)];
	}

	static void out(String str) {
		System.out.println(str);
	}

}
