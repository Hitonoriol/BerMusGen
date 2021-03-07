package consoleapp;

public class Arguments {
	private String args[];
	private int currentArg;

	public Arguments(String[] args) {
		this.args = args;
		reset();
	}

	public Arguments() {
		this(null);
	}

	public boolean isEmpty() {
		return args == null || args.length == 0;
	}

	public boolean isPresent() {
		return !isEmpty();
	}

	public int getInt(int arg) {
		return Integer.parseInt(args[arg]);
	}

	public String getString(int arg) {
		return args[arg];
	}

	private int nextArgIdx() {
		if (currentArg + 1 <= args.length)
			return currentArg++;

		return currentArg - 1;
	}

	public int nextInt() {
		return getInt(nextArgIdx());
	}

	public String nextString() {
		return getString(nextArgIdx());
	}

	public Arguments reset() {
		currentArg = 0;
		return this;
	}

	public String[] asArray() {
		return args;
	}

	public String asString() {
		return String.join(" ", args);
	}

	public static Arguments empty() {
		return emptyArgs;
	}

	private static final Arguments emptyArgs = new Arguments();
}
