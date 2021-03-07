package hmusgen.melody;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableInt;

import hmusgen.GenMain;
import hmusgen.MarkovChain;

public class Melody {
	private List<List<Bar>> partList = new ArrayList<>();
	private int bpm;

	public Melody(int bpm) {
		setBPM(bpm);
	}

	public void setBPM(int bpm) {
		this.bpm = bpm;
	}

	public void addBar(int partIdx, Bar bar) {
		if (partIdx >= partList.size())
			partList.add(new ArrayList<>());

		partList.get(partIdx).add(bar);
	}

	public MarkovChain extractIntervalChain() {
		MarkovChain chain = new MarkovChain(13); // From unison to octave
		MutableInt prevInterval = new MutableInt(-1);
		forEachNotePair((noteA, noteB) -> {
			int interval = Math.abs(noteA.value - noteB.value);

			if (prevInterval.intValue() != -1)
				chain.addTransition(prevInterval.intValue(), interval);

			prevInterval.setValue(interval);
		});
		GenMain.print("Melody interval transition matrix:");
		chain.dump();
		return chain;
	}

	public void forEachNotePair(BiConsumer<Note, Note> notesConsumer) {
		Note prevNote = new Note(Note.emptyNote);
		forEachNote(note -> {
			if (prevNote.isEmpty()) {
				prevNote.set(note);
				return;
			}

			notesConsumer.accept(prevNote, note);
		});
	}

	public void forEachNote(Consumer<Note> noteConsumer) {
		partList
				.forEach(part -> part
						.forEach(bar -> bar.getNotes()
								.forEach(note -> noteConsumer.accept(note))));
	}

	public void forEachBar(Consumer<Bar> barConsumer) {
		partList
				.forEach(bars -> bars
						.forEach(bar -> barConsumer.accept(bar)));
	}

	public void dump() {
		for (int p = 0; p < partList.size(); ++p)
			for (Bar bar : partList.get(p))
				GenMain.print("P" + p + " | " + bar.toString());
	}

	public List<Bar> getPart(int part) {
		if (part >= partList.size())
			return null;

		return partList.get(part);
	}

	public List<List<Bar>> getParts() {
		return partList;
	}

	public int getBpm() {
		return bpm;
	}

	static int MIN_MSEC = 60000;
}
