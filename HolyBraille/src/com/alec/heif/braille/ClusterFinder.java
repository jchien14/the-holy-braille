package com.alec.heif.braille;

import java.io.*;
import java.util.*;

public class ClusterFinder {

	public static int[][] correct = new int[][] {
			{0,0,1,1,1,0,1,0,0,0,0,1,1,0,0,1,0,1,1,0,0,0,0,1,1,0,0,1,1,1},
			{0,0,0,1,0,1,0,0,0,0,1,1,1,1,1,0,1,1,0,1,0,0,0,1,0,0,0,1,1,0},
			{0,1,1,1,1,0,1,1,0,0,0,1,1,0,0,0,1,0,0,0,0,0,1,1,0,0,1,1,0,0},
			{0,0,1,1,1,0,0,1,0,1,0,0,1,0,0,0,0,1,1,0,0,1,0,0,0,1,0,1,1,0},
			{0,0,0,1,0,1,1,1,1,0,1,0,0,1,0,0,1,1,0,0,1,1,0,0,1,1,1,0,1,0},
			{0,0,0,0,1,0,1,0,1,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,1,0,0,1,0},

			{1,0,0,0,1,1,1,0,1,0,0,0,0,1,1,0,1,0,0,0,0,0,0,0,1,0,0,0,0,1},
			{1,0,0,0,0,1,0,1,0,0,0,0,1,0,0,1,0,1,1,0,0,0,0,0,0,0,0,0,0,1},
			{1,0,0,0,1,1,1,0,1,1,0,0,1,0,0,0,0,0,1,1,0,0,0,1,0,0,0,0,1,1},
			{1,1,0,0,1,0,1,0,0,1,0,1,1,0,1,0,0,0,0,1,1,0,1,0,1,1,0,0,0,1},
			{1,0,0,0,1,0,0,1,1,1,1,1,0,1,1,1,0,0,1,1,0,1,1,1,0,1,0,0,1,1},
			{0,0,0,0,1,0,0,0,1,0,1,0,0,0,1,0,0,0,0,1,1,0,1,0,0,0,0,0,0,1},
			{1,0,1,0,0,1,1,0,0,0,1,1,1,0,1,0,1,1,0,1,1,1,1,1,0,0,0,1,0,1},
			{1,1,0,1,1,0,0,1,0,0,0,0,0,1,0,0,0,1,1,0,0,1,1,1,0,0,1,1,1,0},
			{0,0,1,0,1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,1,0,0},
			{1,0,1,0,0,0,1,0,1,0,0,0,0,1,0,1,1,0,0,1,1,0,1,0,1,1,0,1,0,0},
			{1,0,1,0,0,0,1,0,0,1,0,0,1,0,1,0,1,0,1,0,1,0,0,0,0,1,1,1,0,0},
			{1,0,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,1,0,1,0,0,0},

			{0,1,1,0,1,0,1,1,1,1,0,1,0,0,0,0,0,1,1,0,0,1,0,1,0,1,1,0,1,1},
			{1,0,0,1,0,0,0,1,0,1,1,0,1,0,0,0,1,1,1,1,1,0,1,1,1,1,0,1,0,1},
			{1,0,1,0,1,1,1,0,0,0,1,0,0,0,0,0,0,1,1,0,0,0,1,0,1,0,0,0,1,0},
			{0,0,0,1,1,1,0,0,1,0,0,0,1,1,1,0,1,1,1,0,0,0,0,1,1,0,1,0,0,1},
			{0,0,1,0,0,1,0,0,0,0,0,0,0,0,0,1,0,1,0,1,0,0,1,1,1,1,0,0,1,1},
			{0,0,0,0,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0},

			{0,0,0,1,0,1,0,0,1,1,1,0,0,1,0,0,0,1,1,0,1,0,0,0,0,1,1,0,1,1},
			{0,0,1,0,1,0,0,0,0,1,0,1,1,1,0,0,1,1,1,1,0,1,0,0,1,0,0,0,0,0},
			{0,0,0,0,1,0,0,0,1,0,1,0,1,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,1,0},
			{1,0,0,0,1,0,0,1,0,0,0,1,1,0,0,1,0,1,0,0,0,0,1,1,0,0,1,0,0,0},
			{0,1,0,0,0,0,1,0,0,0,1,1,1,1,1,0,1,0,0,0,0,0,0,0,0,0,0,1,1,1},
			{0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,1,0,0,0,0,1,1,0,0,1,1,0,0,1}
		};
	
	public static int[][] parse(int[][] bitMap) {

		int m = bitMap.length;
		int n = bitMap[0].length;
		boolean[][] grid = new boolean[m][n];
		
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				if (bitMap[i][j] == 0) {
					grid[i][j] = true; //flip 0s and 1s
				} else {
					grid[i][j] = false;
				}
			}
		}
		
		
		boolean[][] newGrid = new boolean[m][n];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				if (grid[i][j] && !newGrid[i][j]) {
					boolean[][] seen = new boolean[m][n];
					if (isCluster(grid, i, j, seen)) {
						fillCluster(grid, i, j, newGrid);
					}
				}
			}
		}
		
		boolean[][] rowGrid = new boolean[m][n];
		int m2 = compressRows(newGrid, rowGrid, m, n);
		
		boolean[][] colGrid = new boolean[m][n];
		int n2 = compressCols(rowGrid, colGrid, m2, n);

		int[][] result = new int[m2][n2];
		
//		System.out.println(m2 + "," + n2);
		for (int i = 0; i < m2; i++) {
//			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < n2; j++) {
				if (colGrid[m2][n2]) {
					result[m2][n2] = 1;
				} else {
					result[m2][n2] = 0;
				}
				
				// uncomment for visual check of all bits
//				if ((colGrid[i][j] && correct[i][j]==1) || (!colGrid[i][j] && correct[i][j] == 0)) {
//					sb.append('1');
//				} else {
//					sb.append('0');
//				}
			}
//			out.println(sb.toString());
		}
		return result;
	}
	
	public static boolean isBlock(boolean[][] grid, int row, int col) {
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				if (offGrid(grid, row, col) || !grid[row+i][col+j]) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static boolean offGrid(boolean[][] grid, int row, int col) {
		// if row < grid.length and row >= 0, then grid[0] exists
		return row < 0 || col < 0 || row >= grid.length || col >= grid[0].length;
	}
	
	public static boolean isCluster(boolean[][] grid, int row, int col, boolean[][] seen) {
		// off grid, wrong bit, or already seen
		if (offGrid(grid, row, col) || !grid[row][col] || seen[row][col]) {
			return false;
		}
		
		if (isBlock(grid, row, col)) {
			return true;
		}
		
		seen[row][col] = true;
		if (isCluster(grid, row+1, col, seen)) {
			return true;
		}

		if (isCluster(grid, row, col+1, seen)) {
			return true;
		}

		if (isCluster(grid, row-1, col, seen)) {
			return true;
		}

		if (isCluster(grid, row, col-1, seen)) {
			return true;
		}
		
		return false;
	}

	public static void fillCluster(boolean[][] grid, int row, int col, boolean[][] newGrid) {
		// off grid, shouldn't fill, or already filled
		if (offGrid(grid, row, col) || !grid[row][col] || newGrid[row][col]) {
			return;
		}
		
		newGrid[row][col] = true;
		fillCluster(grid, row+1, col, newGrid);
		fillCluster(grid, row, col+1, newGrid);
		fillCluster(grid, row-1, col, newGrid);
		fillCluster(grid, row, col-1, newGrid);
	}
	

	public static int compressRows(boolean[][] grid, boolean[][] newGrid, int m, int n) {
		int row = 0;
		boolean wasEmpty = true;
		for (int i = 0; i < m; i++) {
			boolean empty = true;
			for (int j = 0; j < n; j++) {
				if (grid[i][j]) {
					newGrid[row][j] = true;
					empty = false;
				}
			}
			if (!empty) {
				wasEmpty = false;
			}
			if (empty && !wasEmpty) {
				row++;
				wasEmpty = true;
			}
		}
		return row;
	}
	
	public static int compressCols(boolean[][] grid, boolean[][] newGrid, int m, int n) {
		int col = 0;
		boolean wasEmpty = true;
		for (int j = 0; j < n; j++) {
			boolean empty = true;
			for (int i = 0; i < m; i++) {
				if (grid[i][j]) {
					newGrid[i][col] = true;
					empty = false;
				}
			}
			if (!empty) {
				wasEmpty = false;
			}
			if (empty && !wasEmpty) {
				col++;
				wasEmpty = true;
			}
		}
		return col;
	}
	
}
