package hmusgen.melody;

public class Note {
	public int value, velocity;
	public Length length;

	public Note(int value, Length length, int velocity) {
		setValue(value);
		setLength(length);
		setVelocity(velocity);
	}

	public Note() {
		setValue(REST);
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

	public boolean isRest() {
		return value == REST;
	}

	static final String NOTE_NAMES = "C C#D D#E F F#G G#A A#B ";

	public String getName(boolean withOctave) {
		if (isRest())
			return "-";

		int octv = value / 12 - 1;
		String name = (NOTE_NAMES.substring((value % 12) * 2, (value % 12) * 2 + 2)).trim();
		return withOctave ? name + octv : name;
	}
	
	public String getName() {
		return getName(true);
	}

	public static final int REST = -666;

	public static class Length {
		public LengthType type;
		public int value;

		public Length(LengthType type, int value) {
			this.type = type;
			this.value = value;
		}

		public String getName() {
			return type.name;
		}

		public static enum LengthType {
			FULL("1"),
			HALF("1/2"),
			QUARTER("1/4"),
			EIGHTH("1/8"),
			SIXTEENTH("1/16"),
			THIRTY_SECOND("1/32");

			public final String name;

			LengthType(String name) {
				this.name = name;
			}
			
			public int toPPQ() {
				return (int) (4f * QUARTER.relationTo(this));
			}

			float relationTo(LengthType length) {
				if (name.equals(length.name))
					return 1;

				float relation = ordinal() - length.ordinal();
				relation = (float) (relation > 0 ? relation * 2f : 1f / Math.pow(2f, -relation));
				return relation;
			}

			public static final LengthType values[] = values();
		}
	}
}
