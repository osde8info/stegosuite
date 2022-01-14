package org.stegosuite.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {

	/**
	 * Returns the substring after the last dot in fileName. If there is no dot in fileName, returns
	 * an empty {@link String}
	 * 
	 * @param fileName foo.bar
	 * @return bar
	 */
	public static String getFileExtension(String fileName) {
		int lastDotPos = fileName.lastIndexOf('.');
		return lastDotPos > -1 ? fileName.substring(lastDotPos + 1) : "";
	}

	/**
	 * Appends a suffix to a filename
	 * 
	 * @param fileName D:\folder\foo.bar
	 * @param suffix _horse
	 * @return D:\folder\foo_horse.bar
	 */
	public static String addFileNameSuffix(String fileName, String suffix) {
		String fileExtension = getFileExtension(fileName);
		if (fileExtension.isEmpty()) {
			return fileName + suffix;
		}
		return fileName.substring(0, fileName.length() - fileExtension.length() - 1) + suffix + "." + fileExtension;
	}

	/**
	 * Changes the file name while leaving the path unmodified
	 * 
	 * @param oldFileName D:\folder\foo.bar
	 * @param newFileName file.dat
	 * @return D:\folder\file.dat
	 */
	public static String changeFileName(String oldFileName, String newFileName) {
		Path path = Paths.get(oldFileName);
		return path.getParent() + File.separator + newFileName;
	}

	/**
	 * @param filePath D:\folder\foo.bar
	 * @return foo.bar
	 */
	public static String getFileName(String filePath) {
		return Paths.get(filePath).getFileName().toString();
	}

	public static long getFileSize(String filePath) {
		return new File(filePath).length();
	}
}
