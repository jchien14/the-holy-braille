package com.alec.heif.braille;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class BitDotMapper {
	
	public final int IMAGE_HEIGHT;
	public final int IMAGE_WIDTH;
	
	// Empirically tuned parameter that indicates the threshold at which if greater than
	// this fraction of pixels are dark OR we have more than NUM_CONSECUTIVE dark, 
	// we assume the row/col is background noise and white it out.
	public static final double NOISE_FRACTION_THRESHOLD = 115./230;
	public static final int NUM_CONSECUTIVE = 100;
	
	public int LEFT_OFFSET; // Approximate last left col index without Braille dot bits
	public int RIGHT_OFFSET; // Approximate first right col index without Braille dot bits
	public int TOP_OFFSET; // Approximate last top row index without Braille dot bits
	public int BOTTOM_OFFSET; // Approximate first bottom row index without Braille dot bits
	public double DOT_WIDTH;
	public double DOT_HEIGHT;
	
	private final int[][] bitMap;
	private int expectedDotRows = 30;
	private int expectedDotCols = 30;
	
	public BitDotMapper(int[][] imageBitMap) {
		this.bitMap = imageBitMap;
		this.IMAGE_HEIGHT = imageBitMap.length;
		this.IMAGE_WIDTH = imageBitMap[0].length;
	}
		

	public BitDotMapper(int[][] imageBitMap, int expectedDotRows, int expectedDotCols) {
		this.bitMap = imageBitMap;
		this.expectedDotRows = expectedDotRows;
		this.expectedDotCols = expectedDotCols;
		this.IMAGE_HEIGHT = imageBitMap.length;
		this.IMAGE_WIDTH = imageBitMap[0].length;
	}
	
	public boolean rowTooDark(int i) {
		double dark = 0.;
		int tot = 0;
		int lastNonDark = 0;
		for (int j = 0; j < IMAGE_WIDTH; j++) {
			if (bitMap[i][j] == 0) {
				dark++;
				tot++;
				if (j - lastNonDark > NUM_CONSECUTIVE) {
					return true;
				}
			} else if (bitMap[i][j] == 1) {
				tot++;
				lastNonDark = j;
			}
		}
		return dark/tot > NOISE_FRACTION_THRESHOLD;
	}

	public boolean colTooDark(int j) {
		double dark = 0.;
		int tot = 0;
		int lastNonDark = 0;
		for (int i = 0; i < IMAGE_HEIGHT; i++) {
			if (bitMap[i][j] == 0) {
				dark++;
				tot++;
				if (i - lastNonDark > NUM_CONSECUTIVE) {
					return true;
				}
			} else if (bitMap[i][j] == 1) {
				tot++;
				lastNonDark = i;
			}
		}
		return dark/tot > NOISE_FRACTION_THRESHOLD;
	}
	
	public void whiteOutDarkBackground() {
		for (int i = 0; i < IMAGE_HEIGHT; i++) {
			if (rowTooDark(i)) {
				for (int j = 0; j < IMAGE_WIDTH; j++) {
					bitMap[i][j] = -1; // represent "deleted" value
				}
			} 
		}
		for (int j = 0; j < IMAGE_WIDTH; j++) {
			if (colTooDark(j)) {
				for (int i = 0; i < IMAGE_HEIGHT; i++) {
					bitMap[i][j] = -1; // represent "to be deleted" value
				}
			} 
		}
		
		// go over twice just in case "deleting" makes other things eligible for "deleting" 
		for (int i = IMAGE_HEIGHT - 1; i >= 0; i--) {
			if (rowTooDark(i)) {
				for (int j = 0; j < IMAGE_WIDTH; j++) {
					bitMap[i][j] = -1; // represent "to be deleted" value
				}
			} 
		}
		for (int j = IMAGE_WIDTH - 1; j >= 0; j--) {
			if (colTooDark(j)) {
				for (int i = 0; i < IMAGE_HEIGHT; i++) {
					bitMap[i][j] = -1; // represent "to be deleted" value
				}
			}		
		}
	}
	
	// Assume there exists at least 2x2 dark square for all dots and is at least one dot in each col/row. 
	private void setTopOffset() {
		for (int i = 0; i < IMAGE_HEIGHT - 1; i++) {
			for (int j = 0; j < IMAGE_WIDTH - 1; j++) {
				if (bitMap[i][j] == 0 && bitMap[i+1][j] == 0 && bitMap[i][j+1] == 0 && bitMap[i+1][j+1] == 0) {
					TOP_OFFSET = i;
					return;
				}
			}
		}
	}
	
	private void setLeftOffset() {
		for (int j = 0; j < IMAGE_WIDTH - 1; j++) {
			for (int i = 0; i < IMAGE_HEIGHT - 1; i++) {
				if (bitMap[i][j] == 0 && bitMap[i+1][j] == 0 && bitMap[i][j+1] == 0 && bitMap[i+1][j+1] == 0) {
					LEFT_OFFSET = j;
					return;
				}
			}
		}
	}
	
	private void setBottomOffset() {
		for (int i = IMAGE_HEIGHT - 2; i >= 0; i--) {
			for (int j = 0; j < IMAGE_WIDTH - 1; j++) {
				if (bitMap[i][j] == 0 && bitMap[i+1][j] == 0 && bitMap[i][j+1] == 0 && bitMap[i+1][j+1] == 0) {
					BOTTOM_OFFSET = i;
					return;
				}
			}
		}
	}

	private void setRightOffset() {
		for (int j = IMAGE_WIDTH - 2; j >= 0; j--) {
			for (int i = 0; i < IMAGE_HEIGHT - 1; i++) {
				if (bitMap[i][j] == 0 && bitMap[i+1][j] == 0 && bitMap[i][j+1] == 0 && bitMap[i+1][j+1] == 0) {
					RIGHT_OFFSET = j;
					return;
				}
			}		
		}
	}
	
	public void setOffsets() {
		setTopOffset();
		setLeftOffset();
		setRightOffset();
		setBottomOffset();
		DOT_WIDTH = (RIGHT_OFFSET - LEFT_OFFSET - 2) / (expectedDotCols - 1.);
		DOT_HEIGHT = (BOTTOM_OFFSET - TOP_OFFSET - 2) / (expectedDotRows - 1.);
	}
	
	public int[][] parse() {
		whiteOutDarkBackground();
		setOffsets();
		int[][] brailleDots = new int[expectedDotRows][expectedDotCols];
		
		for (int i = 0; i < expectedDotRows; i++) {
			for (int j = 0; j < expectedDotCols; j++) {
				if (isBrailleDot(i, j)) {
					brailleDots[i][j] = 1;
				} else {
					brailleDots[i][j] = 0;
				}
			}
		}
		
		for (int i = 0; i < IMAGE_HEIGHT; i++) {
			for (int j = 0; j < IMAGE_WIDTH; j++) {
				if (bitMap[i][j] == -1) {
					bitMap[i][j] = 1; 
				}
				System.out.print(bitMap[i][j]);
			} 
			System.out.print("\n");
		}
		return brailleDots;
	}
	
	public boolean isBrailleDot(int dotI, int dotJ) {
		int topI;
		if (dotI < expectedDotRows / 2)
			topI = ((int) Math.round(TOP_OFFSET + 0.5 + DOT_HEIGHT * dotI));
		else 
			topI = ((int) Math.round(BOTTOM_OFFSET - 1 - DOT_HEIGHT * (expectedDotCols - 1 - dotI)));
		int leftJ;
		if (dotJ < expectedDotCols / 2) 
			leftJ = ((int) Math.floor(LEFT_OFFSET + 0.5 + DOT_WIDTH * dotJ));
		else 
			leftJ = ((int) Math.floor(RIGHT_OFFSET - 2.5 - DOT_WIDTH * (expectedDotCols - 1 - dotJ)));
		leftJ = Math.min(IMAGE_WIDTH, Math.max(leftJ, 0));
		topI = Math.min(IMAGE_HEIGHT, Math.max(topI, 0));
		//bitMap[topI][leftJ] = 2;
		return bitMap[topI][leftJ] + bitMap[topI+1][leftJ] + bitMap[topI][leftJ+1] + bitMap[topI+1][leftJ+1] + 
				bitMap[topI+2][leftJ] + bitMap[topI+2][leftJ+1] + bitMap[topI+2][leftJ+2] +
				bitMap[topI][leftJ+2] + bitMap[topI+1][leftJ+2] < 9;
	}
	
	static int[][] importTestBitMap(String fileName) throws FileNotFoundException {

		BufferedReader CSVFile = new BufferedReader(new FileReader(fileName));
		int i = 0;
		int[][] result = null;
		try {
			int lineCount = countLines(fileName);
			String dataRow = CSVFile.readLine();
			String[] tokens = dataRow.split(",");
			
			result = new int[lineCount][tokens.length];
			while (dataRow != null) {
				tokens = dataRow.split(",");
				int j = 0;
				for (String token : tokens) {
					result[i][j] = Integer.parseInt(token);
					j++;
				}
				i++;
				dataRow = CSVFile.readLine();
			}
			CSVFile.close();
			return result;
		} catch (IOException e) {
			return result;
		} 
	}

	// thanks http://stackoverflow.com/questions/453018/number-of-lines-in-a-file-in-java
	public static int countLines(String fileName) throws IOException {
        LineNumberReader reader  = new LineNumberReader(new FileReader(fileName));
	    try {
	        int cnt = 0;
	        while (reader.readLine() != null) {}
	        cnt = reader.getLineNumber(); 
	        reader.close();
	        return cnt;
	    } finally {
	        reader.close();
	    }
	}
}
	
