package bermusgen;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.sound.midi.MidiSystem;

public class MidiBankLoader {
	void load() throws Exception {
		Musgen.Synth.loadAllInstruments(
				MidiSystem.getSoundbank(new BufferedInputStream(new FileInputStream(new File("bank.sf2")))));
	}
}
