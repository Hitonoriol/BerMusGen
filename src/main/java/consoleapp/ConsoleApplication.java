package consoleapp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

public abstract class ConsoleApplication {
	private Map<String, CommandConsumer> commands = new HashMap<>();
	private Map<String, String> help = new HashMap<>();
	protected String cmdDelim = " ", multiCmdDelim = ";";
	protected String helpCmd = "help", exitCmd = "exit";
	protected String promptStr = "> ";
	protected String helpMenuMsg = "Command list:";
	protected boolean shutdownOnException = true;

	private Scanner scanner;

	public ConsoleApplication() {
		addCommand(helpCmd, () -> printHelpMenu());
		addCommand(exitCmd, () -> exit());
	}

	public ConsoleApplication addCommand(String name, String description, CommandConsumer cmdAction) {
		String helpName = name;
		if (name.contains(cmdDelim)) {
			helpName = name;
			name = name.split(cmdDelim, 2)[0];
		}

		commands.put(name, cmdAction);

		if (description != null)
			help.put(helpName, description);

		return this;
	}

	public ConsoleApplication addCommand(String name, CommandConsumer cmdAction) {
		return addCommand(name, null, cmdAction);
	}

	public ConsoleApplication addCommand(String name, String description, Runnable noArgCmd) {
		return addCommand(name, description, args -> noArgCmd.run());
	}

	public ConsoleApplication addCommand(String name, Runnable noArgCmd) {
		return addCommand(name, null, noArgCmd);
	}

	public void executeCommand(String in) {
		String commandList[] = in.trim().split(multiCmdDelim, 2);
		String tokens[] = commandList[0].split(cmdDelim);
		String cmd = tokens[0].strip();

		if (!commands.containsKey(cmd))
			return;

		Arguments args = new Arguments(Arrays.copyOfRange(tokens, 1, tokens.length));
		commands.get(cmd).accept(args);

		if (commandList.length > 1)
			executeCommand(commandList[1]);
	}

	public void executeConsoleArgs(String[] args) {
		if (args.length > 1)
			executeCommand(String.join(cmdDelim, args));
	}

	public void listenForCommands() {
		scanner = new Scanner(System.in);
		String in = "";
		while (true) {
			System.out.print(promptStr);
			in = scanner.nextLine();
			try {
				executeCommand(in);
			} catch (Exception e) {
				e.printStackTrace();

				if (shutdownOnException)
					exit();
			}
		}
	}

	public void exit() {
		if (scanner != null)
			scanner.close();
		System.exit(0);
	}

	public void printHelpMenu() {
		System.out.println(helpMenuMsg);
		help
				.forEach((cmd, description) -> System.out.println(cmd + " - " + description));
	}

	public static interface CommandConsumer extends Consumer<Arguments> {
	}
}
