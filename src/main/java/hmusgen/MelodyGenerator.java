package hmusgen;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import hmusgen.melody.Bar;
import hmusgen.melody.Melody;
import hmusgen.melody.Note;
import hmusgen.melody.Note.Length.LengthType;

public class MelodyGenerator {
	int bpm;
	int minOct = 0, maxOct = 7;
	int parts = 2;
	Set<String> allowedNotes = new HashSet<>();

	private MidiPlayer player;
	private Melody melody;

	public MelodyGenerator(MidiPlayer player) {
		this.player = player;

		minOct = getRandomOctave();
		maxOct = getRandomOctave();
	}

	public Melody generate(int maxBars) {
		if (bpm == 0)
			randomizeBpm();

		melody = new Melody(bpm, LengthType.QUARTER);

		for (int part = 0; part < parts; ++part)
			for (int i = 0; i < maxBars; ++i) {
				GenMain.out("P" + part + " | ");
				melody.addBar(part, randomBar(new Bar(4, melody.getNoteLengths().get(2))));
			}

		GenMain.print("Generated melody!");
		GenMain.print("BPM: " + melody.getBpm());
		GenMain.print("Min octave: " + minOct + " | Max octave: " + maxOct);
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

	public MelodyGenerator setBPM(int bpm) {
		this.bpm = bpm;
		if (melody != null)
			melody.setBPM(bpm, Note.Length.LengthType.QUARTER);

		return this;
	}

	public MelodyGenerator setOctaveRange(int min, int max) {
		minOct = min;
		maxOct = max;
		return this;
	}

	public MelodyGenerator addAllowedNotes(String notes[]) {
		GenMain.out("Allowed notes: ");
		GenMain.print(Arrays.toString(notes));
		allowedNotes.addAll(Arrays.asList(notes));
		return this;
	}

	public Melody getMelody() {
		return melody;
	}

	public int getRandomOctave() {
		return GenMain.rand(minOct, maxOct);
	}

	public Note.Length getRandomNoteLength() {
		return GenMain.pick(melody.getNoteLengths());
	}

	double restProb = 25;

	private Note testNote = new Note();

	public int getRandomNote() {
		testNote.setValue(Note.REST);

		if (GenMain.percentRand(restProb))
			return testNote.value;

		int octave = GenMain.rand(minOct, maxOct);
		while (!allowedNotes.contains(testNote.getName(false))) {
			testNote.setValue(GenMain.rand((octave * 12) + 12, (octave * 12) + 23));

			if (allowedNotes.isEmpty()) {
				GenMain.print("poop");
				break;
			}
		}

		return testNote.value;
	}

	public Bar randomBar(Bar bar) {
		Note note;
		while (!bar.isFull()) {
			note = new Note(getRandomNote(), getRandomNoteLength(), getRandomVelocity());

			while (!bar.addNote(note))
				note.setLength(getRandomNoteLength());
		}
		GenMain.print(bar.toString());
		return bar;
	}

	public int getRandomVelocity() {
		return GenMain.rand(20, 127);
	}
}
