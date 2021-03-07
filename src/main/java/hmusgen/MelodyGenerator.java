package hmusgen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableInt;

import hmusgen.melody.Bar;
import hmusgen.melody.Melody;
import hmusgen.melody.Note;

public class MelodyGenerator {
	private int bpm;
	private int minOct = 0, maxOct = 7;
	private int parts = 2;
	private double restProb = 25;
	private Set<String> allowedNotes = new HashSet<>();
	private Method generationMethod = Method.Random;

	private MidiPlayer player;
	private Melody melody;
	private MarkovChain intervalChain;

	public MelodyGenerator(MidiPlayer player) {
		this.player = player;

		minOct = getRandomOctave();
		maxOct = getRandomOctave();
	}

	private void generateMarkovIntervals() {
		final int maxInterval = 12;
		MutableInt totalNotes = new MutableInt(0), initialInterval = new MutableInt(-1);
		List<Note> firstNotes = new ArrayList<>(2);
		melody.forEachBar(bar -> {
			totalNotes.add(bar.getNoteCount());
			if (initialInterval.intValue() == -1) {
				bar.getNotes().forEach(note -> {
					if (firstNotes.size() >= 2)
						return;

					firstNotes.add(note);
				});
				if (firstNotes.size() >= 2) {
					do
						initialInterval.setValue(
								Math.abs(firstNotes.get(0).setValue(genNote(minOct, minOct + 1)).value
										- firstNotes.get(1).setValue(genNote(minOct, minOct + 1)).value));
					while (initialInterval.intValue() == -1 || initialInterval.intValue() > maxInterval);
				}
			}
		});

		GenMain.out(firstNotes.get(0).getName() + " & " + firstNotes.get(1).getName());
		GenMain.print("Created first 2 notes with interval: " + initialInterval.intValue() + " semitones");

		int intervalsToGenerate = 1 + ((totalNotes.intValue() - 2) / 2);
		GenMain.print("Generating " + intervalsToGenerate + "intervals using current transition matrix...");

		List<Integer> intervals = new ArrayList<>();
		intervalChain.changeNStates(initialInterval.intValue(), intervalsToGenerate,
				newInterval -> intervals.add(newInterval));

		MutableInt curInterval = new MutableInt(0);
		melody.forEachNotePair((noteA, noteB) -> {
			if (!noteB.isRest())
				return;

			if (GenMain.percentRand(restProb))
				return;

			int dir = (GenMain.random().nextBoolean() ? 1 : -1);
			if (noteA.value >= 110)
				dir = -1;
			else if (noteA.value <= 10)
				dir = 1;

			if (curInterval.intValue() >= intervals.size())
				return;

			noteB.value = noteA.value + (dir * intervals.get(curInterval.getAndIncrement()));
		});
		GenMain.print("Done!");
	}

	public Melody generate(int maxBars) {
		if (bpm == 0)
			randomizeBpm();

		melody = new Melody(bpm);

		// Generate rhythm
		for (int part = 0; part < parts; ++part)
			for (int i = 0; i < maxBars; ++i)
				melody.addBar(part, randomBar(new Bar(4, Note.Length.QUARTER)));

		// Generate notes
		switch (generationMethod) {
		case MarkovIntervals:
			generateMarkovIntervals();
			break;
		case Random:
			melody.forEachNote(note -> note.value = getRandomNote());
			break;
		default:
			break;
		}

		GenMain.print("Generated melody!");
		GenMain.print("BPM: " + melody.getBpm());
		GenMain.print("Min octave: " + minOct + " | Max octave: " + maxOct);
		melody.dump();
		return melody;
	}

	public MelodyGenerator setParts(int instruments) {
		parts = instruments;
		return this;
	}

	public void randomizeInstruments() {
		for (int i = 0, parts = melody.getParts().size(); i < parts; ++i)
			player.changeInstrument(i, GenMain.rand(0, player.getInstrumentCount() - 1));
	}

	public void randomizeBpm() {
		setBPM(GenMain.rand(35, 175));
	}

	public void setBPM(int bpm) {
		this.bpm = bpm;
		if (melody != null)
			melody.setBPM(bpm);
	}

	public void setOctaveRange(int min, int max) {
		minOct = min;
		maxOct = max;
	}

	public void setIntervalChain(MarkovChain chain) {
		intervalChain = chain;
	}

	public MelodyGenerator addAllowedNotes(String notes[]) {
		GenMain.out("Allowed notes: ");
		GenMain.print(Arrays.toString(notes));
		allowedNotes.addAll(Arrays.asList(notes));
		return this;
	}

	public void setGenerationMethod(Method genMethod) {
		generationMethod = genMethod;
	}

	public Melody getMelody() {
		return melody;
	}

	private int getRandomOctave() {
		return GenMain.rand(minOct, maxOct);
	}

	private Note.Length getRandomNoteLength() {
		return GenMain.pick(Note.Length.values);
	}

	private Note testNote = new Note();

	private int getNote(Consumer<Note> noteGen) {
		testNote.setValue(Note.REST);

		if (GenMain.percentRand(restProb))
			return testNote.value;

		do
			noteGen.accept(testNote);
		while (!allowedNotes.isEmpty() && !allowedNotes.contains(testNote.getName(false)));

		return testNote.value;
	}

	private int getRandomNote(int minOct, int maxOct) {
		return getNote(note -> {
			note.setValue(genNote(minOct, maxOct));
		});
	}

	private int getRandomNote() {
		return getRandomNote(minOct, maxOct);
	}

	private int genNote(int minOct, int maxOct) {
		int octave = GenMain.rand(minOct, maxOct);
		return GenMain.rand((octave * 12) + 12, (octave * 12) + 23);
	}

	MutableInt interval = new MutableInt();

	private int markovGenNote(MutableInt currentInterval) {
		return getNote(note -> intervalChain.changeState(currentInterval));
	}

	private Bar randomBar(Bar bar) {
		Note note;
		while (!bar.isFull()) {
			note = new Note()
					.setLength(getRandomNoteLength())
					.setVelocity(getRandomVelocity());

			while (!bar.addNote(note))
				note.setLength(getRandomNoteLength());
		}
		return bar;
	}

	private int getRandomVelocity() {
		return GenMain.rand(20, 127);
	}

	public static enum Method {
		Random, MarkovIntervals
	}
}
