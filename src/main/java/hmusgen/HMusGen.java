package hmusgen;

import consoleapp.ConsoleApplication;

public class HMusGen extends ConsoleApplication {

	MidiPlayer player = new MidiPlayer();
	MelodyGenerator generator = new MelodyGenerator(player);

	public HMusGen() {
		addCommand("gen [bars]",
				"Generate specified quantity (or 10 - 30 if absent) of 4/4 bars using current configuration",
				args -> {
					int maxBars = args.isPresent() ? args.nextInt() : GenMain.rand(10, 30);
					player.setMelody(generator.generate(maxBars));
				});

		addCommand("instr_rand",
				"Randomize instruments for all parts of current melody",
				() -> generator.randomizeInstruments());

		addCommand("parts <value>",
				"Set number of parts to generate",
				args -> generator.setParts(args.nextInt()));

		addCommand("instr_set <channel> <id>",
				"Change instrument of specified channel",
				args -> player.changeInstrument(args.nextInt(), args.nextInt()));

		addCommand("oct_range <min> <max>",
				"Set min and max octaves for melody generator",
				args -> generator.setOctaveRange(args.nextInt(), args.nextInt()));

		addCommand("allow_notes <note1> <note2> ...",
				"Specify the list of notes that can be generated",
				args -> generator.addAllowedNotes(args.asArray()));

		addCommand("bpm <value>",
				"Set bpm of current melody",
				args -> generator.setBPM(args.nextInt()));

		addCommand("play",
				"Play current melody from the beginning",
				() -> player.play());

		addCommand("stop",
				"Stop playing current melody",
				() -> player.stop());

		addCommand("save <filename>",
				"Save current melody to .midi file.",
				args -> player.saveSequence(args.nextString() + ".midi"));

		addCommand("load <filename>",
				"Load .midi file to memory",
				args -> {
					GenMain.print("Loading " + args.getString(0));
					player.loadSequence(args.getString(0));
				});

		addCommand("markov_intervals",
				"Build interval transition matrix based on current melody",
				() -> generator.setIntervalChain(player.extractMelody().extractIntervalChain()));

		addCommand("gen_mode <Random | MarkovIntervals>",
				"Set note pitch generation mode",
				args -> generator.setGenerationMethod(MelodyGenerator.Method.valueOf(args.nextString())));

		addCommand("instruments",
				"List all instruments supported by current soundbank",
				() -> player.listInstruments());
	}

	@Override
	public void exit() {
		player.close();
		super.exit();
	}
}
