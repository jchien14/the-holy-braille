package com.alec.heif.braille;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/* Parsing follows Grade 2 English Braille 
 * as defined here: http://en.wikipedia.org/wiki/English_Braille#System 
 * Singleton, which Java doesn't support so e'erthin' be static.
*/ 
public class BrailleUtils {
	
	public class AreYouBlindException extends RuntimeException {}
	
	// These special characters modify next character
	static final String NUMBER = "NUMBER"; 
	static final String UPPERCASE = "UPPER";
	static final String ACCENT = "ACCENT"; // not supported right now
	static final String LETTER_ONLY = "LETTER"; // not supported right now
	static final String ABBREVIATION = "ABBREVIATION"; // not supported right now

	// Punctuation with ambiguous behavior
	public static final String OPEN_QUOTE_OR_QUESTION = "?";
	public static final String CLOSE_QUOTE = "\"";
	public static final String PAREN = "(";

	public static final String UNKNOWN_OR_UNSUPPORTED = "*";
	
	public static final Set<String> UNSUPPORTED_MODIFIERS = new HashSet<String>(Arrays.asList(ABBREVIATION, 
			LETTER_ONLY, ACCENT));

	public static final Map<String, String> ALTERNATE_TRANSCRIPTIONS = new HashMap<String, String>(){{ 
		put("a", "1"); put("b", "2"); put("c", "3"); put("d", "4"); put("e", "5"); put("f", "6"); // LOL anonymous
		put("g", "7"); put("h", "8"); put("i", "9"); put("j", "0"); put(NUMBER, "ble");   // inner classes fuck Java.
		put(",", "ea"); put(";", "bb"); put(":", "cc"); put(".", "dd"); put("!", "ff"); put(PAREN, "gg"); }}; //Sucks lotsa thick dick.

	public static final String[] BRAILLE_ALPHABET = new String[] { " ", "a", ACCENT, "c", ",", // 4
	                        "b", "i", "f", ABBREVIATION, "e", ABBREVIATION, "d", ":", "h", "j", "g", "'", // 16
	                        "k", "/", "m", ";", "l", "s", "p", "in", // 24
	                        "o", "ar", "n", "!", "r", "t", "q", UPPERCASE, // 32
	                        "ch", /*decimal*/ ".", "sh", "en", "gh", "ow", "ed", LETTER_ONLY, // 40
	                        "wh", ABBREVIATION, "th", /*period*/ ".", "ou", "w", "er", "-", // 48
	                        "u", "ing", "x", OPEN_QUOTE_OR_QUESTION, "v", "the", "and", CLOSE_QUOTE, // 56
	                        "z", NUMBER, "y", PAREN, "of", "with", "for" }; // 63
	
	public static String parseBraille(int[][] brailleArray) {
		return parseBrailleSecondPass(parseBrailleFirstPass(brailleArray)).trim();
	}
	
	/**
	 * One to one mapping of Braille into a String
	 * @VisibleForTesting
	 */
	static String parseBrailleFirstPass(int[][] brailleArray) {
		int maxLen = getMaxLength(brailleArray);
		int numRowsToPad = 0;
		if (brailleArray.length % 3 != 0) {
			System.out.println("INFO: Num rows is NOT a multiple of 3. Will fill in 0s");
			numRowsToPad = 3 - (brailleArray.length % 3);
		}
		if (maxLen % 2 != 0) {
			System.out.println("INFO: Num cols is odd. Will fill in 0s.");
			maxLen++;
		}
		for (int i = 0; i < brailleArray.length; i++) {
			brailleArray[i] = Arrays.copyOf(brailleArray[i], maxLen);
		}
		
		int numLettersInRow = maxLen / 2; 
		int numRowsOfLetters = brailleArray.length / 3; // integer division is fine as we pad later

		// TODO: Clean ugly repeated code up by setting variables like midLeft or botRight 0 when padding is needed
		String output = "";
		for (int rowCount = 0; rowCount < numRowsOfLetters; rowCount++) {
			for (int rowLet = 0; rowLet < numLettersInRow; rowLet++) {
				output += brailleToLetter(brailleArray[3*rowCount][2*rowLet], brailleArray[3*rowCount][2*rowLet + 1],
						brailleArray[3*rowCount + 1][2*rowLet], brailleArray[3*rowCount + 1][2*rowLet + 1],
						brailleArray[3*rowCount + 2][2*rowLet], brailleArray[3*rowCount + 2][2*rowLet + 1]);
			}
		}
		if (numRowsToPad == 2) { 
			for (int rowLet = 0; rowLet < numLettersInRow; rowLet++) {
				output += brailleToLetter(brailleArray[3*numRowsOfLetters][2*rowLet], 
						brailleArray[3*numRowsOfLetters][2*rowLet + 1], 0, 0, 0, 0);
			}

		} else if (numRowsToPad == 1) { 
			for (int rowLet = 0; rowLet < numLettersInRow; rowLet++) {
				output += brailleToLetter(brailleArray[3*numRowsOfLetters][2*rowLet], 
						brailleArray[3*numRowsOfLetters][2*rowLet + 1], 
						brailleArray[3*numRowsOfLetters + 1][2*rowLet], 
						brailleArray[3*numRowsOfLetters + 1][2*rowLet + 1], 0, 0);
			}
		}
		
		return output;
	}
	
	private static int getMaxLength(int[][] brailleArray) {
		int maxLen = 0;
		for (int i = 0; i < brailleArray.length; i++) {
			if (brailleArray[i].length > maxLen) {
				maxLen = brailleArray[i].length;
			}
		}
		return maxLen;
	}
	
	/**
	 * Mutate the shit out of the input String to get the final String
	 * @VisibleForTesting
	 */
	static String parseBrailleSecondPass(String firstPassOutput) {
		while (firstPassOutput.contains(NUMBER)) {
			System.out.println(firstPassOutput);
			int indOfReplace = firstPassOutput.indexOf(NUMBER) + NUMBER.length();
			String replaceFrom = Character.toString(firstPassOutput.charAt(indOfReplace));
			String replaceTo = ALTERNATE_TRANSCRIPTIONS.get(replaceFrom);
			firstPassOutput = firstPassOutput.replace(NUMBER + replaceFrom, replaceTo);
		}
		while (firstPassOutput.contains(UPPERCASE)) {
			int indOfReplace = firstPassOutput.indexOf(UPPERCASE) + UPPERCASE.length();
			String replaceFrom = Character.toString(firstPassOutput.charAt(indOfReplace));
			String replaceTo = replaceFrom.toUpperCase();
			firstPassOutput = firstPassOutput.replace(UPPERCASE + replaceFrom, replaceTo);
		}
		for (String unsupportedModifier : UNSUPPORTED_MODIFIERS) {
			while (firstPassOutput.contains(unsupportedModifier)) {
				int indOfReplace = firstPassOutput.indexOf(unsupportedModifier) + unsupportedModifier.length();
				String replaceFrom = Character.toString(firstPassOutput.charAt(indOfReplace));
				String replaceTo = UNKNOWN_OR_UNSUPPORTED;
				firstPassOutput = firstPassOutput.replace(unsupportedModifier + replaceFrom, replaceTo);
			}
		}
		
		return firstPassOutput;
	}

	public static String brailleToLetter(int topLeft, int topRight,   // the arguments look
										 int midLeft, int midRight,   // like you're passing braille into 
										 int botLeft, int botRight) { // the function! haiku.
		return BRAILLE_ALPHABET[topLeft + 2*topRight + 4*midLeft + 8*midRight + 16*botLeft + 32*botRight];
	}
}