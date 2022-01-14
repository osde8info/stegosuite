package org.stegosuite;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.stegosuite.ui.cli.CliParser;
import org.stegosuite.ui.gui.Gui;

public class Stegosuite {

	public static void main(String[] args) {
		Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.INFO);
		if (args.length == 0) {
			new Gui(null);
		} else if (args[0].startsWith("-")) {
			new CliParser(args).parse();
		} else {
			new Gui(args[0]);
		}
	}
}
