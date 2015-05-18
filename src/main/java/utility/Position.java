package main.java.utility;

import java.text.DecimalFormat;
import java.util.HashMap;

/**
 * holds a position for the board colors
 * @author Chris Irwin
 */
/**
 * @author CHRISTOPHER_IRWIN
 *
 */
public class Position {
	
	// stores the constants for the neighbors of any given square
	private static final HashMap<Integer,Integer> neighbors;
	static { 
		neighbors = new HashMap<Integer,Integer>();
		neighbors.put(0, 35);
		neighbors.put(1, 71);
		neighbors.put(2, 142);
		neighbors.put(3, 284);
		neighbors.put(4, 536);
		neighbors.put(5, 1121);
		neighbors.put(6, 2274);
		neighbors.put(7, 4548);
		neighbors.put(8, 9096);
		neighbors.put(9, 17168);
		neighbors.put(10, 35872);
		neighbors.put(11, 72768);
		neighbors.put(12, 145536);
		neighbors.put(13, 291072);
		neighbors.put(14, 549376);
		neighbors.put(15, 1147904);
		neighbors.put(16, 2328576);
		neighbors.put(17, 4657152);
		neighbors.put(18, 9314304);
		neighbors.put(19, 17580032);
		neighbors.put(20, 3178496);
		neighbors.put(21, 7405568);
		neighbors.put(22, 14811136);
		neighbors.put(23, 29622272);
		neighbors.put(24, 25690112);
	}
	public int blue;
	public int red;
	public int getUnclaimed()  {
		return ~(blue|red) % 33554432;  // this is mod 2**25, for some reason this was returning values greater than 2**25
	}
	public Position(int blue,int red) {
		this.blue = blue;
		this.red = red;
	}

	public Position(String colors) {
		int i = 0;
		String digits = "0123456789";
		char prevchar = 'W';
		colors = colors.toUpperCase();
		for (int n=0; n<colors.length(); n++) {
			char c = colors.charAt(n);
			if (c == 'B') {
				blue |= (1<<i);
				prevchar = c;
				i++;
			} else if (c == 'R') {
				red |= (1<<i);
				prevchar = c;
				i++;
			} else if (digits.indexOf(c) != -1) {
				int num = Character.getNumericValue(c);
				for (int m=0;m<num-1;m++) {
					if (prevchar == 'R') {
						red |= (1<<i);
					} else if (prevchar == 'B') {
						blue |= (1<<i);
					}
					i++;
				}
			} else {
				prevchar = 'W';
				i++;
			}
		}
	}
	
	public Position() {
		this(0,0);
	}
	
	public int getBlueDef() {
		int blueDef = 0;
        for (int n=0; n<25; n++) {
	        if ((blue & neighbors.get(n)) == neighbors.get(n)) {
	            blueDef |= (1 << n);
	        }
        }
        return blueDef;
	}
	
	public int getRedDef() {
		int redDef = 0;
        for (int n=0; n<25; n++) {
	        if ((red & neighbors.get(n)) == neighbors.get(n)) {
	            redDef |= (1 << n);
	        }
		}
        return redDef;
	}
	
	public static Coordinate centroid(int map) {
		if (map != 0) {
	        double cnt = 0;
	        int ysum = 0;
	        int xsum = 0;
	        for (int i=0; i<25; i++) {
	            if ((1<<i & map) > 0) {
	                ysum+=i/5;
	                xsum+=i%5;
	                cnt++;
	            }
	        }
	        return new Coordinate(xsum/cnt, ysum/cnt);
        } else {
            return new Coordinate(2,2);
        }
	}
	
	@Override
	public Position clone() {
		return new Position(blue, red); 
	}
	
	@Override
	public String toString() {
		//return Integer.toString(blue) + "," + Integer.toString(red); 
		return toColors();
	}
	
	public String toColors() {
        String s = "";
        for (int i=0; i<25; i++) {
            if ((blue & neighbors.get(i)) == neighbors.get(i)) {
                s += 'B';
            } else if ((red & neighbors.get(i)) == neighbors.get(i)) {
            	s += 'R';
            } else if ((blue & (1<<i)) > 0) {
                s += 'b';
            } else if ((red & (1<<i)) > 0) {
                s += 'r';
            } else {
                s += '-';
            }
            if (i % 5 == 4) {
                s += ' ';
            }
        }
        return s;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || o.getClass() != getClass()) {
			return false;
		} else {
			Position p = (Position) o;
			if ( blue == p.blue && red == p.red) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	
	/**
	 * @return String displaying the number of tiles occupied and defended for each color, used by Arena
	 */
	public String numScore() {
		String colors = toColors();
		int blueCount = 0;
		int redCount = 0;
		int blueDefCount = 0;
		int redDefCount = 0;
		for (int i=0; i<colors.length(); i++) {
			char l = colors.charAt(i);
			switch (l) {
				case 'B': 	blueDefCount++;
				case 'b':	blueCount++;
							break;
				case 'R':	redDefCount++;
				case 'r':	redCount++;
							break;
			}
		}
		DecimalFormat two = new DecimalFormat("00");
		return "blue: " + two.format(blueCount) + "(" + two.format(blueDefCount) + ") red: " + two.format(redCount) + "(" + two.format(redDefCount) + ")";
	}
	
	public int blueCount() {
		String blueStr = Integer.toBinaryString(blue);
		int blueCount = 0;
		for (int x=0; x<blueStr.length(); x++) {
			if (blueStr.charAt(x) == '1') {
				blueCount++;
			}
		}
		return blueCount;
	}

	public int redCount() {
		String redStr = Integer.toBinaryString(red);
		int redCount = 0;
		for (int x=0; x<redStr.length(); x++) {
			if (redStr.charAt(x) == '1') {
				redCount++;
			}
		}
		return redCount;
	}

}