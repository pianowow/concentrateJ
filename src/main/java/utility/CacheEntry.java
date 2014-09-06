package main.java.utility;

import java.util.ArrayList;
import java.util.Arrays;

public class CacheEntry {
	public ArrayList<String> found;
	public ArrayList<String> played;
	public double[] d;
	public double[] u;
	public CacheEntry(ArrayList<String> found, ArrayList<String> played, double[] d,double[] u) {
		this.found = found;
		this.played = played;
		this.d = d;
		this.u = u;
	}
	public CacheEntry(ArrayList<String> found) {
		this.found = found;
		this.played = new ArrayList<String>();
		this.d = new double[0];
		this.u = new double[0];
	}
	public String toString() {
		return Arrays.toString(d) + " " + Arrays.toString(u);
	}
}