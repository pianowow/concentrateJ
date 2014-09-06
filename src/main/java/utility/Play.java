package main.java.utility;

public class Play {
	public double score;
	public String word;
	public Position position;
	public int move;
	public Play(double score, String word, Position p, int move) {
		this.score = score;
		this.word = word;
		this.position = p;
		this.move = move;
	}
	public Play() {
		this(0,"",new Position(), 0);
	}
	public String toString() {
		return Double.toString(score) + "," + word + "," + position;
	}
	
}