package main.java;

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;


public class player {
	// from file
	ArrayList<String> wordList; 
	
	// default difficulty settings
	char dict;
	int maxWordLen;
	boolean random;
	
	// evolution weights
	double dw;
	double uw;
	double dpw;
	double upw;
	double mw;
	
	HashMap<String,CacheEntry> cache;
	
	public static void main(String[] args) {
		player x = new player();
		System.out.println(x.possible("ASDFGASDFGASDFGASDFGASDFG").size()  );
	}
	
	public player(char dict, int maxWordLen, boolean random, double dw, double uw, double dpw, double upw, double mw) {
		this.dict = dict;
		this.maxWordLen = maxWordLen;
		this.random = random;
		this.dw = dw;
		this.uw = uw;
		this.dpw = dpw;
		this.upw = upw;
		this.mw = mw;
		this.cache = new HashMap<String,CacheEntry>();
		try {
			BufferedReader wordFile;
			if (dict == 'A') {
			    wordFile = new BufferedReader(new FileReader("src/main/resources/en15.txt"));
			}
			else {
				wordFile = new BufferedReader(new FileReader("src/main/resources/reduced.txt"));
			}
			String line;
		
			while ((line = wordFile.readLine()) != null) 
			{
			    wordList.add(line.toUpperCase());
			}
			wordFile.close();
		}
		catch (Exception e) {
			System.out.println("Exception in player(): "+e.getMessage());
		}		
	}
	
	public player() {
		this('A',25,false,3.1,1.0,1.28,2.29,7.78);
	}
	
	public ArrayList<String> possible(String letters) {
		ArrayList<String> found = new ArrayList<String>();
		letters = letters.toUpperCase();
        for (String word: wordList) {
            boolean good = true;
            for (int i = 0; i < word.length(); i++) {
            	char l = word.charAt(i);
            	int lcount = 0;
            	for (int li = 0; li < letters.length(); li++) {
            		if (letters.charAt(li) == l)
            			lcount++;
            	}
            	int wcount = 0;
            	for (int wi = 0; wi < word.length(); wi++) {
            		if (word.charAt(wi) == l)
            			wcount++;
            	}            	
                if (lcount < wcount) {
                    good = false;
                    break;
                }
            }
            if (good)
                found.add(word);
        }
        // calculate the popularity of each letter
        HashMap<Character, Integer> letterdict = new HashMap<Character, Integer>();
        for (String word: found) {
            for (int i=0; i < word.length(); i++) {
                char letter = word.charAt(i);
                if (letterdict.containsKey(letter)) {
                    letterdict.put(letter, letterdict.get(letter) + 1);
                }
                else {
                	letterdict.put(letter, 1);
                }
            }
        }
        
        int max = -1;
        int x;
        for (Entry<Character, Integer> pair: letterdict.entrySet()) {
        	x = pair.getValue();
        	if (x>max) {
        		max = x;
        	}
        }
        HashMap<Character, Double> letterdict2 = new HashMap<Character, Double>();
        for (Entry<Character, Integer> pair: letterdict.entrySet()) {
        	x = pair.getValue();
        	letterdict2.put(pair.getKey(), x/(double)max);
        }
        for (int i=0; i<25; i++) {
        	char l = letters.charAt(i);
        	if (!letterdict2.containsKey(l)) {
        		letterdict2.put(l, 0.);
        	}
        }
        
        Double[] d = new Double[25];
        
        
        for (int i=0; i<25; i++) {
        	
        }
        
		return found;
	}
}

class CacheEntry {
	public ArrayList<String> found;
	public ArrayList<String> played;
	public Double[] d;
	public Double[] u;
	public CacheEntry(ArrayList<String> found, ArrayList<String> played, Double[] d,Double[] u) {
		this.found = found;
		this.played = played;
		this.d = d;
		this.u = u;
	}
}

