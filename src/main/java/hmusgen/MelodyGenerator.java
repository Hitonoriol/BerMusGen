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
import hmusgen.melody.Note.Length;

public class MelodyGenerator {
	private int bpm;
	private int minOct = 1, maxOct = 6;
	private int parts = 2;
	private double restProb = 15;
	private Set<String> allowedNotes = new HashSet<>();
	private Method generationMethod = Method.Random;

	private MidiPlayer player;
	private Melody melody;
	private MarkovChain intervalChain, rhythmChain;

	public MelodyGenerator(MidiPlayer player) {
		this.player = player;

		minOct = getRandomOctave();
		maxOct = getRandomOctave();
	}

	private Bar markovBar(Bar bar) {
		int length, newLength;
		bar.addNote(new Note().setLength(Length.values[length = rhythmChain.pickNonNullState()]));
		while (!bar.isFull()) {
			bar.addNote(new Note().setLength(Length.values[newLength = rhythmChain.changeState(length)]));
			length = newLength;
		}
		return bar;
	}

	private void generateMarkovIntervals() {
		MutableInt totalNotes = new MutableInt(0);
		List<Note> firstNotes = new ArrayList<>(2);
		melody.forEachBar(bar -> {
			totalNotes.add(bar.getNoteCount());
			bar.getNotes().forEach(note -> {
				if (firstNotes.size() >= 2)
					return;

				firstNotes.add(note);
			});
		});

		int initialInterval = intervalChain.pickNonNullState();
		Note fNote = firstNotes.get(0);
		fNote.setValue(genNote(minOct, maxOct));
		firstNotes.get(1).setValue(fNote.value + initialInterval);

		GenMain.print("\nCreated first 2 notes " + initialInterval + " semitones apart");

		int intervalsToGenerate = totalNotes.intValue() - 2;
		GenMain.print("Generating " + intervalsToGenerate + " intervals using current transition matrix...");

		List<Integer> intervals = new ArrayList<>();
		intervalChain.changeNStates(initialInterval, intervalsToGenerate,
				newInterval -> intervals.add(newInterval));

		MutableInt curInterval = new MutableInt(0);
		Note prevNote = new Note();
		melody.forEachNotePair((noteA, noteB) -> {
			//GenMain.print("A: " + noteA.toString() + " | B: " + noteB.toString());
			if (!noteB.isRest())
				return;

			if (GenMain.percentRand(restProb))
				return;

			int dir = (GenMain.random().nextBoolean() ? 1 : -1);

			if (curInterval.intValue() >= intervals.size())
				return;

			if (!noteA.isRest())
				prevNote.set(noteA);

			noteB.setValue((noteA.isRest() ? prevNote : noteA)
					.applyInterval(dir * intervals.get(curInterval.getAndIncrement())));

			if (noteA.isRest())
				prevNote.set(noteB);
		});
		GenMain.print("Done!");
	}

	public Melody generate(int maxBars) {
		if (bpm == 0)
			randomizeBpm();

		melody = new Melody(bpm);

		// Generate rhythm
		Bar bar = null;
		for (int part = 0; part < parts; ++part)
			for (int i = 0; i < maxBars; ++i) {
				bar = new Bar(4, Note.Length.QUARTER);

				if (generationMethod == Method.Random)
					bar = randomBar(bar);
				else if (generationMethod == Method.Markov)
					bar = markovBar(bar);

				melody.addBar(part, bar);
			}

		// Generate notes
		switch (generationMethod) {
		case Markov:
			generateMarkovIntervals();
			break;
		case Random:
			melody.forEachNote(note -> note.value = getRandomNote());
			break;
		default:
			break;
		}

		melody.forEachNote(note -> note.setVelocity(getRandomVelocity()));

		GenMain.print("Generated new melody!");
		GenMain.print("Generation method: " + generationMethod.name());
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

	public void setRhythmChain(MarkovChain chain) {
		rhythmChain = chain;
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

	private Bar randomBar(Bar bar) {
		Note note;
		while (!bar.isFull()) {
			note = new Note()
					.setLength(getRandomNoteLength());

			while (!bar.addNote(note))
				note.setLength(getRandomNoteLength());
		}
		return bar;
	}

	private int getRandomVelocity() {
		return GenMain.rand(20, 127);
	}

	public static enum Method {
		Random, Markov
	}
}
