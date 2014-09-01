package main.java.utility;

import java.util.Comparator;

public class ComparePlay implements Comparator<Play> {
	private int move;
	@Override
	public int compare(Play o1, Play o2) {
		if (move == 1) {
			int result = -1 * Double.compare(o1.score, o2.score);
			if (result != 0) {
				return result;
			}
		} else {
			int result = Double.compare(o1.score, o2.score);
			if (result != 0) {
				return result;
			}
		}
		return -1 * Integer.compare(o1.word.length(), o2.word.length());
	}
	public ComparePlay(int move) {
		this.move = move;
	}
	
}