package hmusgen.melody;

import java.util.ArrayList;
import java.util.List;

import hmusgen.GenMain;
import hmusgen.melody.Note.Length;
import hmusgen.melody.Note.Length.LengthType;

public class Melody {
	private List<Length> noteLengths = new ArrayList<>(LengthType.values.length);
	private List<List<Bar>> partList = new ArrayList<>();
	private int bpm;

	public Melody(int bpm, LengthType basePulse) {
		setBPM(bpm, basePulse);
	}

	public void setBPM(int bpm, LengthType basePulse) {
		this.bpm = bpm;
		LengthType lengths[] = LengthType.values();
		int baseLength = MIN_MSEC / bpm;

		int newLen = 0;
		for (int i = 0; i < lengths.length; ++i) {
			newLen = (int) (baseLength * basePulse.relationTo(lengths[i]));
			GenMain.print(lengths[i].name + ": " + newLen + " ms");
			noteLengths.add(new Length(lengths[i], newLen));
		}
		GenMain.print("");
	}

	public void addBar(int partIdx, Bar bar) {
		if (partIdx >= partList.size())
			partList.add(new ArrayList<>());

		partList.get(partIdx).add(bar);
	}

	public List<Length> getNoteLengths() {
		return noteLengths;
	}

	public List<List<Bar>> getParts() {
		return partList;
	}

	public int getBpm() {
		return bpm;
	}

	static int MIN_MSEC = 60000;
}
