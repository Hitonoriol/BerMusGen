package hmusgen.melody;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;

import hmusgen.MidiPlayer;

public class Note {
	public int value, velocity;

	public Length length;
	public int lengthTimes = 1; // >1 for irregular lengths

	public Note(int value, Length length, int velocity) {
		setValue(value);
		setLength(length);
		setVelocity(velocity);
	}

	public Note(Note note) {
		set(note);
	}

	public Note() {
		setValue(REST);
	}

	public void set(Note note) {
		this.value = note.value;
		this.length = note.length;
		this.velocity = note.velocity;
		this.lengthTimes = note.lengthTimes;
	}

	public Note setVelocity(int velocity) {
		this.velocity = velocity;
		return this;
	}

	public Note setValue(int value) {
		this.value = value;
		return this;
	}

	public Note setLength(Length length) {
		this.length = length;
		return this;
	}

	public Note setLength(Length length, int times) {
		lengthTimes = times;
		return setLength(length);
	}

	public int getPPQLength() {
		return lengthTimes * length.toPPQ();
	}

	public Length getLength() {
		if (this.length == Length.MIDIDefault && lengthTimes > 1)
			return Length.fromPPQ(lengthTimes);

		return this.length;
	}

	public boolean isRest() {
		return value == REST;
	}

	public int applyInterval(int semitones) {
		int value = this.value;
		value += semitones;

		while (value > MIDI_PITCH_MAX)
			value -= OCTAVE;

		while (value < 0)
			value += OCTAVE;
		return value;
	}

	static final int MIDI_PITCH_MAX = 127, OCTAVE = 12;
	static final String NOTE_NAMES[] = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };

	public String getName(boolean withOctave) {
		if (isRest())
			return "-";

		int octv = value / OCTAVE - 1;
		String name = NOTE_NAMES[Math.abs(value) % OCTAVE];
		return withOctave ? name + octv : name;
	}

	public String getName() {
		return getName(true);
	}

	public boolean isEmpty() {
		return equals(emptyNote);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (!(obj instanceof Note))
			return false;

		Note rhs = (Note) obj;
		return new EqualsBuilder()
				.append(value, rhs.value)
				.append(length, rhs.length)
				.append(velocity, rhs.velocity)
				.isEquals();
	}

	@Override
	public String toString() {
		return getLength().getName() + getName() + "(" + value + ")";
	}

	public static final Note emptyNote = new Note().setValue(0xDEAD);

	public static final int REST = -666;

	public static enum Length {
		FULL("1"),
		HALF("1/2"),
		QUARTER("1/4"),
		EIGHTH("1/8"),
		SIXTEENTH("1/16"),
		THIRTY_SECOND("1/32");

		private final String name;
		public static final Length values[] = values();
		public final static Length MIDIDefault;
		static Map<Integer, Length> PPQMap = new HashMap<>();

		static {
			for (Length len : values)
				PPQMap.put(len.toPPQ(), len);
			MIDIDefault = fromPPQ(1);
		}

		Length(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		int toPPQ() {
			return (int) ((float) MidiPlayer.PPQ * QUARTER.relationTo(this));
		}

		float relationTo(Length length) {
			if (name.equals(length.name))
				return 1;

			float relation = ordinal() - length.ordinal();
			relation = (float) (relation > 0 ? relation * 2f : 1f / Math.pow(2f, -relation));
			return relation;
		}

		public static Length fromPPQ(int ticks) {
			return PPQMap.get(ticks);
		}
	}
}
