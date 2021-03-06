package hmusgen.melody;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Bar {
	private Note.Length basePulse;
	private float curLen, maxLen;

	private List<Note> notes = new ArrayList<>();

	public Bar(int length, Note.Length basePulse) {
		this.maxLen = length;
		this.basePulse = basePulse;
	}

	public boolean addNote(Note note) {
		float newBarLen = curLen + basePulse.type.relationTo(note.length.type);
		if (newBarLen > maxLen)
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

	public List<Note> getNotes() {
		return notes;
	}

	@Override
	public String toString() {
		String noteList = notes.stream()
				.map(note -> note.length.getName() + note.getName() + " ")
				.collect(Collectors.joining());
		return maxLen + "*" + basePulse.getName() + " | "
				+ noteList;
	}
}
