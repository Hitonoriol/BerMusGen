package hmusgen;

import hmusgen.Melody.Note;
import hmusgen.Melody.NoteLength;

public class MelodyGenerator {
	int minOct = 0, maxOct = 7;
	// TODO: list of allowed notes / intervals

	private MelodyPlayer player;
	private Melody melody;

	public MelodyGenerator(MelodyPlayer player) {
		this.player = player;

		minOct = getRandomOctave();
		maxOct = getRandomOctave();
	}

	public Melody generate(int maxNotes) {
		melody = new Melody(GenMain.rand(35, 175), NoteLength.QUARTER);
		int octave = GenMain.rand(minOct, maxOct);
		for (int i = 0; i < maxNotes; ++i)
			melody.addNote(new Note(getRandomNote(octave), getRandomNoteLength(), getRandomVelocity()));

		GenMain.out("Generated melody!");
		GenMain.out("BPM: " + melody.getBpm());
		GenMain.out("Min octave: " + minOct + " | Max octave: " + maxOct);
		return melody;
	}

	public void randomizeInstruments() {
		MidiPlayer midiPlr = player.getMidiPlayer();
		midiPlr.changeInstrument(0, GenMain.rand(0, midiPlr.getInstrumentCount() - 1));
	}

	public Melody getMelody() {
		return melody;
	}

	public int getRandomOctave() {
		return GenMain.rand(minOct, maxOct);
	}

	public int getRandomNoteLength() {
		return melody.getNoteLength(GenMain.pick(NoteLength.values));
	}

	public int getRandomNote(int octave) {
		return GenMain.rand((octave * 12) + 12, (octave * 12) + 23);
	}

	public int getRandomVelocity() {
		return GenMain.rand(50, 255);
	}
}
