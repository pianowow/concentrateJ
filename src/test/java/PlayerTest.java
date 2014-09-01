package test.java;

import java.util.ArrayList;
import java.util.Collections;

import main.java.Player;
import main.java.utility.ComparePlay;
import main.java.utility.Play;
import main.java.utility.Position;

public class PlayerTest {
	public static void main(String[] args) {
		Player x = new Player();
		String letters = "BNXOFQOQXNVQQSDFJQLPISQQV";
		String colors = "B5WB4RRB3R3WBR3BB";
		int move = -1;
		Position init = new Position(colors);
		System.out.println(init.toColors());
		ArrayList<Play> z = new ArrayList<Play>();
		z = x.decide(letters,colors,"","",move);
		Collections.sort(z, new ComparePlay(move));
		System.out.println(z);
		System.out.println(z.size());

		Play p = x.turn(letters,colors,move);
		System.out.println(p);
		System.out.println(p.position.toColors());
	}
}
