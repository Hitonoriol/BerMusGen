package bermusgen;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Random;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;

class Loader {
	void load() throws Exception {
		Musgen.Synth.loadAllInstruments(
				MidiSystem.getSoundbank(new BufferedInputStream(new FileInputStream(new File("bank.sf2")))));
	}
}

public class Musgen {
	static int channel = 0;
	static boolean simul = false;
	static int note, vel, oct, intr; // note params
	static int interv[] = new int[6]; // note intervals
	static int minoct, maxoct; // min/max octave (inclusive)
	static Synthesizer Synth;
	static Random rnd;
	static int seed, instr, c, chans;
	static MidiChannel[] mc;

	static int rand(int min, int max) {
		return rnd.nextInt(max + 1 - min) + min;
	}

	public static void main(String[] args) throws Exception {

		// Usage args:
		// 0 <iterations (or 0 for endless)>
		// 1 <instrument (-1 for random; works only if channels == 1)>
		// 2 <seed (0 for random)>
		// 3 <octave range (e.g. 0>3 or 0>0 ; -1 for random)>
		// 4 <BPM (0 for random)>
		// 5 <channels (0 for random)>
		// e.g. "java -jar bermusgen.jar 0 -1 0 -1 0 0" - fully randomized melody

		String iterHolder = args[0], instrumentHolder = args[1], seedHolder = args[2], octaveHolder = args[3],
				BPMHolder = args[4], chansHolder = args[5];

		seed = new Random().nextInt();
		if (seedHolder.equals("0"))
			rnd = new Random(seed);
		else
			rnd = new Random(Integer.parseInt(seedHolder));

		if (chansHolder.equals("0"))
			chans = rand(1, 16);
		else
			chans = Integer.parseInt(chansHolder);

		int bpm;
		if (BPMHolder.equals("0"))
			bpm = rand(10, 320);
		else
			bpm = Integer.parseInt(BPMHolder);

		interv[2] = 60000 / bpm;
		interv[1] = interv[2] * 2;
		interv[0] = interv[1] * 2;
		interv[3] = interv[2] / 2;
		interv[4] = interv[2] / 4;
		interv[5] = interv[4] / 2;

		boolean fl = false;
		if (iterHolder.equals("0"))
			fl = true;

		Loader bankLoader = new Loader();
		Synth = MidiSystem.getSynthesizer();
		Synth.open();
		bankLoader.load();
		mc = Synth.getChannels();

		int i = Integer.parseInt(iterHolder);
		instr = Integer.parseInt(instrumentHolder);

		if (chans == 1) {
			if (instr == -1)
				mc[0].programChange(rand(0, 127));
			else
				mc[0].programChange(instr);
		} else {
			int ck = 0;
			while (ck < chans) {
				mc[ck].programChange(rand(0, 127));
				ck++;
			}
		}

		if (octaveHolder.equals("-1")) {
			minoct = rand(-1, 7);
			maxoct = rand(minoct, 7);
		} else {
			minoct = Integer.parseInt(octaveHolder.split("\\>")[0]);
			maxoct = Integer.parseInt(octaveHolder.split("\\>")[1]);
		}

		if (fl)
			c = -1; // infinite mode

		oct = rand(minoct, maxoct);
		intr = interv[rand(0, 5)]; // if they won't change for the first time

		System.out.println("Ready to play!\nSeed: " + seed + "\nChannels: " + chans + "\nBPM: " + bpm
				+ "\nIntervals: 1/2: " + interv[1] + " 1/4: " + interv[2] + " 1/8: " + interv[3] + " 1/16: " + interv[4]
				+ " 1/32: " + interv[5] + "\n---------------------------------------");

		int ck = 0;
		while (c < i) {
			simul = rnd.nextBoolean();
			while (ck < chans) {
				channel = ck;
				playNote();
				ck++;
			}
			if (simul) {
				Thread.sleep(intr);
				if (rnd.nextBoolean())
					mc[channel].allNotesOff();
				if (rnd.nextBoolean())
					mc[channel].noteOff(note);
			}
			ck = 0;
			c++;
			if (fl)
				i++;
		}
	}

	static void playNote() throws Exception {
		randomNote();
		System.out.println("Note #" + c + " on channel: " + channel + " octave: " + oct + " note id: " + note
				+ " velocity: " + vel + " interval: " + intr + " ms " + "instrument: " + mc[channel].getProgram());
		mc[channel].noteOn(note, vel);
		if (!simul) {
			Thread.sleep(intr);
			if (rnd.nextBoolean())
				mc[channel].allNotesOff();
			if (rnd.nextBoolean())
				mc[channel].noteOff(note);
		}
	}

	static void randomNote() {
		if (instr == -1 && rnd.nextInt(100) == rnd.nextInt(100))
			mc[channel].programChange(rand(0, 127));
		if (rnd.nextBoolean())
			oct = rand(minoct, maxoct);
		if (rnd.nextBoolean())
			intr = interv[rand(0, 5)];
		note = rand((oct * 12) + 12, (oct * 12) + 23);
		vel = rand(50, 700);
	}

}
