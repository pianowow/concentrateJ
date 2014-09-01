package main.java.utility;

public class Coordinate {
	double x;
	double y;
	Coordinate(double x, double y) {
		this.x = x;
		this.y = y;
	}
	public String toString() {
		return Double.toString(x) + "," + y;
	}
    public double distanceBetween(Coordinate c) {
        return Math.sqrt((Math.pow(c.x-x, 2) + Math.pow(c.y-y, 2)));
    }
}