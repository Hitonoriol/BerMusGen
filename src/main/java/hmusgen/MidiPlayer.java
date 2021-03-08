package hmusgen;

import java.io.File;
import java.util.List;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
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
import hmusgen.melody.Note.Length;

public class MidiPlayer {

	private Synthesizer synth;
	private MidiChannel[] mc;
	private Sequencer sequencer;
	private Instrument instrument[];
	private int instruments;
	private Sequence sequence;

	static final int channels = 16;
	public static final int PPQ = 8;

	public MidiPlayer() {
		initSynth();
		loadMidiBank();
	}

	private void loadMidiBank() {
		try {
			synth.loadAllInstruments(
					MidiSystem.getSoundbank(getClass().getResourceAsStream("/bank.sf2")));
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
			instrument = synth.getLoadedInstruments();
			instruments = instrument.length;
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
			sequence = new Sequence(Sequence.PPQ, PPQ, parts.size());
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
							tick.add(note.getPPQLength());
							return;
						}
						ShortMessage noteOnMsg = new ShortMessage();
						noteOnMsg.setMessage(ShortMessage.NOTE_ON, channel, note.value, note.velocity);

						ShortMessage noteOffMsg = new ShortMessage();
						noteOffMsg.setMessage(ShortMessage.NOTE_OFF, channel, note.value, note.velocity);

						midiTrack.add(new MidiEvent(noteOnMsg, tick.intValue()));
						tick.add(note.getPPQLength());
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

	public Melody extractMelody() {
		Melody melody = new Melody((int) sequencer.getTempoInBPM());
		Sequence sequence = sequencer.getSequence();
		Track tracks[] = sequence.getTracks();

		if (sequence.getDivisionType() != Sequence.PPQ)
			return null;

		int denom = sequence.getResolution() / PPQ;
		GenMain.print("Dividing note durations by " + denom);

		MidiEvent event;
		MidiMessage msg;

		Bar[] bar = new Bar[channels];
		for (int i = 0; i < bar.length; ++i)
			bar[i] = new Bar();

		Note[] curNote = new Note[channels];
		long[] noteStart = new long[channels],
				noteEnd = new long[channels],
				delta = new long[channels];

		for (Track track : tracks)
			for (int i = 0; i < track.size(); ++i) {
				event = track.get(i);
				msg = event.getMessage();
				if (msg instanceof ShortMessage) {
					ShortMessage sm = (ShortMessage) msg;
					int channel = sm.getChannel();
					int note = sm.getData1();
					int velocity = sm.getData2();

					if (sm.getCommand() == ShortMessage.NOTE_ON) {
						curNote[channel] = new Note().setValue(note).setVelocity(velocity);
						noteStart[channel] = event.getTick();

						if ((delta[channel] = (noteEnd[channel] - noteStart[channel]) / denom) > 0)
							bar[channel].addNote(new Note().setLength(Length.MIDIDefault, (int) delta[channel]));
					} else if (sm.getCommand() == ShortMessage.NOTE_OFF) {
						noteEnd[channel] = event.getTick();

						int dur = (int) (noteEnd[channel] - noteStart[channel]) / denom;
						Note.Length len = Note.Length.fromPPQ(dur);

						if (len == null)
							curNote[channel].setLength(Length.MIDIDefault, dur);
						else
							curNote[channel].setLength(len);

						bar[channel].addNote(curNote[channel]);
					}
				}
			}

		for (int i = 0; i < bar.length; ++i)
			if (!bar[i].isEmpty()) {
				melody.addBar(i, bar[i]);
				GenMain.print("Adding part #" + i + " to the melody");
			}

		return melody;
	}

	public void play() {
		GenMain.print("Starting playback...");
		sequencer.setTickPosition(0);
		sequencer.start();
	}

	public void stop() {
		sequencer.stop();
	}

	public void close() {
		sequencer.close();
		synth.close();
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

	public void listInstruments() {
		for (int i = 0; i < instruments; ++i)
			GenMain.print("#" + i + ": " + instrument[i].getName());
		GenMain.print("\n");
	}
}
