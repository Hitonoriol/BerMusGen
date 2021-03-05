package hmusgen;

public class MelodyPlayer {
	private MidiPlayer player = new MidiPlayer(this);
	private Melody melody;

	public MelodyPlayer(Melody melody) {
		setMelody(melody);
	}
	
	public MelodyPlayer() {
		this(null);
	}
	
	public MelodyPlayer setMelody(Melody melody) {
		this.melody = melody;
		return this;
	}

	public void play() {
		melody.getNotes().stream().forEach(note -> {
			player.playNote(0, note);
			synchronized (this) {
				try {
					wait();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		GenMain.out("End of melody");
	}

	public Melody getMelody() {
		return melody;
	}
	
	public MidiPlayer getMidiPlayer() {
		return player;
	}
}
