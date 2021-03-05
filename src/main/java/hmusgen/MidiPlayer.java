package hmusgen;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;

import hmusgen.Melody.Note;

public class MidiPlayer {

	private Synthesizer synth;
	private MidiChannel[] mc;
	private MelodyPlayer melodyPlayer;
	private int instruments;

	public MidiPlayer(MelodyPlayer parent) {
		this.melodyPlayer = parent;
		initSynth();
		loadMidiBank();
	}

	private void loadMidiBank() {
		try {
			synth.loadAllInstruments(
					MidiSystem.getSoundbank(getClass().getResourceAsStream("/bank.sf2")));

			GenMain.out("Loaded soundbank:");

			Instrument instrument[] = synth.getLoadedInstruments();
			instruments = instrument.length;
			for (int i = 0; i < instruments; ++i)
				GenMain.out("#" + i + ": " + instrument[i].getName());
			GenMain.out("\n");

		} catch (Exception e) {
			GenMain.out("Failed to load sounbank.");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private void initSynth() {
		try {
			synth = MidiSystem.getSynthesizer();
			synth.open();
			mc = synth.getChannels();
		} catch (Exception e) {
			GenMain.out("Failed to init MIDI synth.");
			e.printStackTrace();
		}
	}

	public void playNote(int channel, Note note) {
		GenMain.out(note.getName()
				+ " " + melodyPlayer.getMelody().getLengthName(note.length).name
				+ " Vel: " + note.velocity);

		mc[channel].noteOn(note.value, note.velocity);
		new Thread(() -> {
			try {
				Thread.sleep(note.length);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				mc[channel].noteOff(note.value);
				synchronized (melodyPlayer) {
					melodyPlayer.notify();
				}
			}

		}).start();
	}

	void changeInstrument(int chan, int instr) {
		GenMain.out("Set channel " + chan + " instrument to #" + instr);
		mc[chan].programChange(instr);
	}

	int getInstrumentCount() {
		return instruments;
	}
}
