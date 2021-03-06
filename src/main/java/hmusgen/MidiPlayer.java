package hmusgen;

import java.io.File;
import java.util.List;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;

import org.apache.commons.lang3.mutable.MutableInt;

import hmusgen.melody.Bar;
import hmusgen.melody.Melody;
import hmusgen.melody.Note;

public class MidiPlayer {

	private Synthesizer synth;
	private MidiChannel[] mc;
	private Sequencer sequencer;
	private int instruments;
	private Sequence sequence;

	public MidiPlayer() {
		initSynth();
		loadMidiBank();
	}

	private void loadMidiBank() {
		try {
			synth.loadAllInstruments(
					MidiSystem.getSoundbank(getClass().getResourceAsStream("/bank.sf2")));

			GenMain.print("Loaded soundbank:");

			Instrument instrument[] = synth.getLoadedInstruments();
			instruments = instrument.length;
			for (int i = 0; i < instruments; ++i)
				GenMain.print("#" + i + ": " + instrument[i].getName());
			GenMain.out("\n");

		} catch (Exception e) {
			GenMain.print("Failed to load sounbank.");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private void initSynth() {
		try {
			synth = MidiSystem.getSynthesizer();
			synth.open();
			mc = synth.getChannels();
			sequencer = MidiSystem.getSequencer();
			if (sequencer == null)
				throw (new Exception("Failed to initialize sequencer."));
			else {
				sequencer.getTransmitter().setReceiver(synth.getReceiver());
				sequencer.open();
			}
		} catch (Exception e) {
			GenMain.print("Failed to init MIDI synth.");
			e.printStackTrace();
		}
	}

	public MidiPlayer setMelody(Melody melody) {
		List<List<Bar>> parts = melody.getParts();
		try {
			GenMain.print("Parts: " + parts.size());
			sequence = new Sequence(Sequence.PPQ, 4, parts.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
		sequencer.setTempoInBPM(melody.getBpm());

		MutableInt tick = new MutableInt(0);
		Track midiTrack = sequence.createTrack();
		parts.forEach(part -> {
			int channel = parts.indexOf(part);
			tick.setValue(0);
			part.forEach(bar -> {
				bar.getNotes().forEach(note -> {
					try {
						if (note.isRest()) {
							tick.add(note.length.type.toPPQ());
							return;
						}
						ShortMessage noteOnMsg = new ShortMessage();
						noteOnMsg.setMessage(ShortMessage.NOTE_ON, channel, note.value, note.velocity);

						ShortMessage noteOffMsg = new ShortMessage();
						noteOffMsg.setMessage(ShortMessage.NOTE_OFF, channel, note.value, note.velocity);

						midiTrack.add(new MidiEvent(noteOnMsg, tick.intValue()));
						tick.add(note.length.type.toPPQ());
						midiTrack.add(new MidiEvent(noteOffMsg, tick.intValue()));
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			});
		});
		GenMain.print("Created " + midiTrack.size() + " MIDI events");

		try {
			sequencer.setSequence(sequence);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	public void play() {
		GenMain.print("Starting playback...");
		sequencer.setTickPosition(0);
		sequencer.start();
	}

	public void playNote(int channel, Note note) {
		GenMain.print(note.getName()
				+ " " + note.length.getName()
				+ " Vel: " + note.velocity);

		if (!note.isRest())
			mc[channel].noteOn(note.value, note.velocity);

		new Thread(() -> {
			try {
				Thread.sleep(note.length.value);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				mc[channel].noteOff(note.value);
				synchronized (note) {
					note.notify();
				}
			}

		}).start();
	}
	
	public void stop() {
		sequencer.stop();
	}

	void saveSequence(String filename) {
		try {
			MidiSystem.write(sequence, MidiSystem.getMidiFileTypes()[1], new File(filename));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void loadSequence(String filename) {
		try {
			sequencer.setSequence(MidiSystem.getSequence(new File(filename)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void changeInstrument(int chan, int instr) {
		GenMain.print("Set channel " + chan + " instrument to #" + instr);
		mc[chan].programChange(instr);
	}

	int getInstrumentCount() {
		return instruments;
	}
}
