package hmusgen;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class GenMain {
	private static Random random = new Random();

	MidiPlayer player = new MidiPlayer();
	MelodyGenerator generator = new MelodyGenerator(player);

	void exec(String in) {
		String commandList[] = in.trim().split(";", 2);
		String tokens[] = commandList[0].split(" ");
		String cmd = tokens[0].trim();
		boolean hasArgs = tokens.length > 1;

		if (cmd.equals("gen")) {
			int maxBars = hasArgs ? val(tokens[1]) : rand(10, 30);
			player.setMelody(generator.generate(maxBars));
		}

		if (cmd.equals("instr_rand"))
			generator.randomizeInstruments();

		if (cmd.equals("parts"))
			generator.setParts(val(tokens[1]));

		if (cmd.equals("instr_set"))
			player.changeInstrument(val(tokens[1]), val(tokens[2]));

		if (cmd.equals("oct_range"))
			generator.setOctaveRange(val(tokens[1]), val(tokens[2]));

		if (cmd.equals("allow_notes"))
			generator.addAllowedNotes(Arrays.copyOfRange(tokens, 1, tokens.length));

		if (cmd.equals("bpm"))
			generator.setBPM(val(tokens[1]));

		if (cmd.equals("play"))
			player.play();
		
		if (cmd.equals("stop"))
			player.stop();

		if (cmd.equals("save"))
			player.saveSequence(tokens[1] + ".midi");
		
		if (cmd.equals("load"))
			player.loadSequence(tokens[1]);

		if (commandList.length > 1)
			exec(commandList[1]);
	}

	public static void main(String[] args) {
		GenMain main = new GenMain();
		Scanner scan = new Scanner(System.in);
		String in = args.length > 1 ? String.join(" ", args) : "---";

		while (!in.equals("")) {
			main.exec(in);
			out("> ");
			in = scan.nextLine();
		}
		scan.close();
	}

	static int val(String numStr) {
		return Integer.parseInt(numStr);
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

}
