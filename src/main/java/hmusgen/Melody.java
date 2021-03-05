package hmusgen;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

public class Melody {
	private BidiMap<NoteLength, Integer> realLength = new DualHashBidiMap<>();
	private List<Note> noteList = new ArrayList<>();
	private int bpm;

	public Melody(int bpm, NoteLength basePulse) {
		setBPM(bpm, basePulse);
	}

	private void setBPM(int bpm, NoteLength basePulse) {
		this.bpm = bpm;
		NoteLength lengths[] = NoteLength.values();
		int baseLength = MIN_MSEC / bpm;
		int baseIdx = basePulse.ordinal();
		realLength.put(basePulse, baseLength);

		int newLen = 0;
		for (int i = 0, k; i < lengths.length; ++i) {
			if (i == baseIdx)
				continue;

			k = baseIdx - i;
			newLen = k < 0 ? baseLength / (-2 * k) : baseLength * 2 * k;

			realLength.put(lengths[i], newLen);
		}
	}

	public int getNoteLength(NoteLength noteLen) {
		return realLength.get(noteLen);
	}

	public NoteLength getLengthName(int length) {
		return realLength.getKey(length);
	}

	public void addNote(Note note) {
		noteList.add(note);
	}

	public List<Note> getNotes() {
		return noteList;
	}
	
	public int getBpm() {
		return bpm;
	}

	static class Note {
		int value, length, velocity;

		public Note(int value, int length, int velocity) {
			this.value = value;
			this.length = length;
			this.velocity = velocity;
		}
		
		static final String NOTE_NAMES = "C C#D D#E F F#G G#A A#B ";

		public String getName() {
			int octv = value / 12 - 1;
			return (NOTE_NAMES.substring((value % 12) * 2, (value % 12) * 2 + 2)).trim() + octv;
		}
	}

	static int MIN_MSEC = 60000;

	static enum NoteLength {
		FULL("1"),
		HALF("1/2"),
		QUARTER("1/4"),
		EIGHTH("1/8"),
		SIXTEENTH("1/16"),
		THIRTY_SECOND("1/32");

		final String name;

		NoteLength(String name) {
			this.name = name;
		}

		static final NoteLength values[] = values();
	}
}
