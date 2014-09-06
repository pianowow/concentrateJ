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
		String letters = "UCLDYNRARSNGHCDNUNRAYJHEG";
		String colors = "BBbrR BBbbr bbrrb rrRRr b-rRR";
		colors = colors.replace(" ", "");
		int move = 1;
		Position init = new Position(colors);
		System.out.println(init.toColors());
		ArrayList<Play> z = new ArrayList<Play>();
		z = x.decide(letters,colors,"","",move);
//		x.playWord(letters, "DACHSHUND");
//		z = x.decide(letters,colors,"","",move);

		Collections.sort(z, new ComparePlay(move));
//		System.out.println(z);
//		System.out.println(z.size());
		int e = (int) Math.pow(2, 25);
		System.out.println(e);
		System.out.println(Integer.toBinaryString(e));
		for (Play x0 : z) {
			if (x0.word.equals("HANDJARS")) {
				int i = x.endgameCheck(letters, x0.position, move);
				String uncl = Integer.toBinaryString(x0.position.getUnclaimed() % e);
				String bl = Integer.toBinaryString(x0.position.blue);
				String re = Integer.toBinaryString(x0.position.red);
				String pos = x0.position.toColors();
				System.out.println(x0+" "+bl + " " + re + " " + " " + uncl + " " + pos + " " + i);
			}
		}
//
		//		System.out.println(p.position.toColors());


		Play p = x.turn(letters,colors,move);
		System.out.println(p);
		System.out.println(p.position.toColors());
	}
}
