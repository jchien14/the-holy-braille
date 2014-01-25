package com.alec.heif.braille;

import static org.junit.Assert.*;
import org.junit.Test;
import com.alec.heif.braille.BrailleUtils;

/**
 * LOL WHO THE FUCK WRITES TEST CASES FOR A HACKATHON?
 * @author jchien
 * Oh, that's who.
 */
public class BrailleUtilsTest {
	@Test
	public void brailleToLetter_simple() {
		assertEquals("n", BrailleUtils.brailleToLetter(1, 1, 0, 1, 1, 0));
	}
	
	@Test
	public void parseBraille_simple() {
		int[][] squareDown = {{1, 1, 1, 0}, {0, 1, 0, 1}, {0, 0, 1, 0}, {0, 1, 1, 1}, {1, 1, 0, 1}, {0, 1, 1, 0}};
		assertEquals("down", BrailleUtils.parseBraille(squareDown));
	}
	
	@Test
	public void parseBraille_numb3rsCaps() {
		int[][] A1 = {{0, 0, 1, 0}, {0, 0, 0, 0}, {0, 1, 0, 0}, {0, 1, 1, 0}, {0, 1, 0, 0}, {1, 1, 0, 0}};
		assertEquals("A1", BrailleUtils.parseBraille(A1));
	}
	
	@Test
	public void parseBraille_missingZeros() {
		int[][] squareDown = {{1, 1, 1}, {0, 1, 0, 1}, {0, 0, 1}, {0, 1, 1, 1}, {1, 1, 0, 1}, {0, 1, 1}};
		assertEquals("down", BrailleUtils.parseBraille(squareDown));
	}

	@Test
	public void parseBraille_needPadding() {
		int[][] squareDown = {{1, 1, 1}, {0, 1, 0, 1}, {0, 0, 1}, {0, 1, 1, 1}, {1, 1, 0, 1}, {0, 1, 1}, {1}};
		assertEquals("downa", BrailleUtils.parseBraille(squareDown));
	}
}
