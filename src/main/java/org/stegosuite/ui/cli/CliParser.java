package org.stegosuite.ui.cli;

import ch.qos.logback.classic.Level;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CliParser {

	private String[] args = null;
	private static final Logger LOG = LoggerFactory.getLogger(CliParser.class);

	public CliParser(String[] s) {
		args = s;
	}

	public void parse() {
		CommandLineParser parser = new DefaultParser();

		Options options = new Options();
		options.addOption("d", "debug", false, "show debug information");
		options.addOption("e", "embed", false, "embed data into image");
		options.addOption("x", "extract", false, "extract data from image");
		options.addOption("m", "message", true, "message to embed");
		options.addOption("k", "key", true, "secret key used for encrytion and hiding");
		options.addOption("c", "capacity", false, "shows the maximum amount of data which can be embededded");
		options.addOption("h", "help", false, "displays this help message");

		Option files = Option.builder("f").hasArgs().longOpt("files").desc("files to embed").build();

		options.addOption(files);

//		Option stegokey = Option.builder().hasArg().longOpt("stegokey")
//				.desc("the secret stego key used for hiding the content").build();
//		Option cryptokey = Option.builder().hasArg().longOpt("encryptionkey")
//				.desc("the secret key used for encryption of the content").build();
		Option noNoise = Option.builder().longOpt("disable-noise-detection")
				.desc("disables the automatic avoidance of homogeneous areas").build();
//		Option extractPath = Option.builder().longOpt("extraction-path").desc("the folder to store extracted files")
//				.build();

//		options.addOption(stegokey); // TODO
//		options.addOption(cryptokey); // TODO
		options.addOption(noNoise);
//		options.addOption(extractPath); // TODO

		try {
			CommandLine line = parser.parse(options, args);
			Cli cli = new Cli();

			if (line.hasOption("d")) { 
				ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory
						.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
				root.setLevel(Level.DEBUG);
			}

			if (line.hasOption("e")) {
				cli.embed(line);
			} else if (line.hasOption("x")) {
				cli.extract(line);
			} else if (line.hasOption("c")) {
				cli.capacity(line);
			} else if (line.hasOption("h")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("stegosuite", options);
			}
		} catch (ParseException exp) {
			LOG.error("Unexpected exception:" + exp.getMessage());
		}
	}
}
