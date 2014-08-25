package main.java;

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;


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
        
        
        
		return found;
	}
}
