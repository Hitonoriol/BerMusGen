package hmusgen.melody;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.lang3.mutable.MutableInt;

import hmusgen.GenMain;
import hmusgen.MarkovChain;
import hmusgen.melody.Note.Length;

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

	private MarkovChain extractNoteChain(int chainSize, BiFunction<Note, Note, Integer> noteStateTransition) {
		MarkovChain chain = new MarkovChain(chainSize);
		MutableInt prevState = new MutableInt(-1);
		forEachNotePair((noteA, noteB) -> {
			int state = noteStateTransition.apply(noteA, noteB);
			if (prevState.intValue() != -1 && state != -1)
				chain.addTransition(prevState.intValue(), state);

			prevState.setValue(state);
		});
		return chain;
	}

	private MarkovChain extractNoteChain(int chainSize, Function<Note, Integer> noteStateTransition) {
		return extractNoteChain(chainSize, (noteA, noteB) -> noteStateTransition.apply(noteA));
	}

	public MarkovChain extractIntervalChain() {
		// From unison to double octave
		MarkovChain chain = extractNoteChain(25, (noteA, noteB) -> Math.abs(noteA.value - noteB.value));
		GenMain.print("Melody interval transition matrix:");
		chain.dump();
		return chain;
	}

	public MarkovChain extractRhythmChain() {
		MarkovChain chain = extractNoteChain(6, note -> {
			Length len = note.getLength();
			if (len == null)
				return -1;

			return len.ordinal();
		});

		GenMain.print("Note duration (1 - 1/32) transition matrix:");
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
			
			prevNote.set(note);
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
