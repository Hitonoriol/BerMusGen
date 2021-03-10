package hmusgen.melody;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Bar {
	private Note.Length basePulse = Note.Length.QUARTER;
	private float curLen, maxLen;
	private boolean endless = false;

	private List<Note> notes = new ArrayList<>();

	public Bar(int length, Note.Length basePulse) {
		this.maxLen = length;
		this.basePulse = basePulse;
	}

	public Bar() {
		endless = true;
	}

	public boolean addNote(Note note) {
		float newBarLen = curLen + basePulse.relationTo(note.length);
		if (!endless && newBarLen > maxLen)
			return false;

		notes.add(note);
		curLen = newBarLen;
		return true;
	}

	public float getFreeSpace() {
		return maxLen - curLen;
	}

	public boolean isFull() {
		return curLen == maxLen;
	}

	public boolean isEmpty() {
		return notes.isEmpty();
	}

	public Note getNote(int note) {
		if (note >= notes.size())
			return null;

		return notes.get(note);
	}

	public List<Note> getNotes() {
		return notes;
	}

	public int getNoteCount() {
		return notes.size();
	}

	@Override
	public String toString() {
		String noteList = notes.stream()
				.map(note -> note.toString() + " ")
				.collect(Collectors.joining());
		return maxLen + "*" + basePulse.getName() + " | "
				+ noteList;
	}
}
