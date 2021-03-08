package hmusgen;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class GenMain {
	private static Random random = new Random();

	private static NumberFormat numberFormatter = NumberFormat.getInstance(Locale.US);
	static {
		numberFormatter.setMinimumFractionDigits(0);
		numberFormatter.setRoundingMode(RoundingMode.HALF_UP);
	}

	public static void main(String[] args) {
		HMusGen app = new HMusGen();
		app.printHelpMenu();
		app.executeConsoleArgs(args);
		app.listenForCommands();
	}

	static int val(String numStr) {
		return Integer.parseInt(numStr);
	}

	public static Random random() {
		return random;
	}

	static boolean percentRand(double percent) {
		return (random.nextDouble() * 100d) < percent;
	}

	static int rand(int min, int max) {
		if (min >= max)
			return min;

		return random.nextInt(max + 1 - min) + min;
	}

	static <T> T pick(T[] arr) {
		return arr[random.nextInt(arr.length)];
	}

	static <T> T pick(List<T> list) {
		return list.get(random.nextInt(list.size()));
	}

	public static void print(String str) {
		System.out.println(str);
	}

	public static void out(String str) {
		System.out.print(str);
	}

	public static void out(String str, int width) {
		out(setWidth(str, width));
	}

	public static String round(double num) {
		return round(num, 3);
	}

	public static String round(double num, int n) {
		numberFormatter.setMaximumFractionDigits(n);
		return numberFormatter.format(num);
	}

	public static String setWidth(String string, int length) {
		return String.format("%1$" + length + "s", string);
	}
}
