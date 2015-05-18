package main.java;

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import main.java.utility.*;

/**
 * This is holds all the intelligence of Concentrate: the search, evaluation, etc.
 * @author Chris Irwin
 */

public class Player {

	protected String name;
	
	// from file
	private ArrayList<String> wordList; 
	
	// default difficulty settings
	private char dict;
	private int maxWordLen;
	private boolean random;
	
	// evolution weights
	private double dw;
	private double uw;
	private double dpw;
	private double upw;
	private double mw;
	
	private HashMap<String,CacheEntry> cache;
	private HashMap<String, HashMap<Position, Double>> hashTable;
		
	/**
	 * This constructor contains all settings, only used by evolution
	 * @param dict Difficulty setting, 'A' for all words, 'R' for reduced list of words
	 * @param maxWordLen Difficulty setting, used to limit the length of words searched for
	 * @param random Difficulty setting, whether or not to sort the words when choosing one to play
	 * @param dw Evolved parameter, weight given to a defended tile
	 * @param uw Evolved parameter, weight given to a claimed and undefended tile
	 * @param dpw Evolved parameter, weight given to the popularity of a defended tile
	 * @param upw Evolved parameter, weight given to the popularity of a claimed and undefended tile and its neighbors
	 * @param mw Evolved parameter, weight given to the difference between red and blue's center of mass from the unclaimed tiles
	 */
	public Player(char dict, int maxWordLen, boolean random, double dw, double uw, double dpw, double upw, double mw) {
		name = "Stable";
		this.dict = dict;
		this.maxWordLen = maxWordLen;
		this.random = random;
		this.dw = dw;
		this.uw = uw;
		this.dpw = dpw;
		this.upw = upw;
		this.mw = mw;
		this.wordList = new ArrayList<String>();
		this.cache = new HashMap<String,CacheEntry>();
		this.hashTable = new HashMap<String, HashMap<Position, Double>>();
		//load the wordList from disk
		try {
			BufferedReader wordFile;
			if (this.dict == 'A') {
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
	
	/**
	 * This constructor contains only difficulty settings, used by GUI
	 * @param dict Difficulty setting, 'A' for all words, 'R' for reduced list of words
	 * @param maxWordLen Difficulty setting, used to limit the length of words searched for
	 * @param random Difficulty setting, whether or not to sort the words when choosing one to play
	 */
	public Player(char dict, int maxWordLen, boolean random) {
		this(dict,maxWordLen,random,3.1,1.0,1.28,2.29,7.78);
	}

	/**
	 * This constructor creates the player at maximum difficulty
	 */
	public Player() {
		this('A',25,false,3.1,1.0,1.28,2.29,7.78);
	}
	
	
	/**
	 * @param letters are the letters on the board
	 * @return a list of words that can be made on this board
	 * purpose is to find words playable on a letterpress board
	 * Also computes values used by evaluatePos in determining the worth of a letter
	 */
	public ArrayList<String> possible(String letters) {
		if (cache.containsKey(letters)) {
			ArrayList<String> found = new ArrayList<String>(cache.get(letters).found);
			int i;
			for (String word: cache.get(letters).played) {
				i = found.indexOf(word);
				if (i>=0) 
					found.remove(i);
			}
			return found;
		} else {
			ArrayList<String> found = new ArrayList<String>();
			letters = letters.toUpperCase();
	        for (String word: wordList) {
	        	if (word.length() <= maxWordLen) {
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
	        }
	        
	        // calculate the popularity of each letter
	        HashMap<Character, Integer> letterCount = new HashMap<Character, Integer>();
	        for (String word: found) {
	            for (int i=0; i < word.length(); i++) {
	                char letter = word.charAt(i);
	                if (letterCount.containsKey(letter)) {
	                    letterCount.put(letter, letterCount.get(letter) + 1);
	                }
	                else {
	                	letterCount.put(letter, 1);
	                }
	            }
	        }
	        int max = -1;
	        int x;
	        for (Entry<Character, Integer> pair: letterCount.entrySet()) {
	        	x = pair.getValue();
	        	if (x>max) {
	        		max = x;
	        	}
	        }
	        HashMap<Character, Double> letterdict = new HashMap<Character, Double>();
	        for (Entry<Character, Integer> pair: letterCount.entrySet()) {
	        	x = pair.getValue();
	        	letterdict.put(pair.getKey(),Math.round((x/(double)max)*100)/100.);
	        }
	        for (int i=0; i<25; i++) {
	        	char l = letters.charAt(i);
	        	if (!letterdict.containsKey(l)) {
	        		letterdict.put(l, 0.);
	        	}
	        }
	
	        // save defended scores
	        double[] d = new double[25];
	        for (int i=0; i<25; i++) {
	        	d[i] = Math.round((dw + dpw*(1-letterdict.get(letters.charAt(i))))*100)/100.;
	        }
	        
	        //save undefended scores
	        double[] u = new double[25];
	        int[] range5 = {0,1,2,3,4};
	        for (int row: range5) {
	        	for (int col: range5) {
	        		ArrayList<Double> neighborList = new ArrayList<Double>();
	        		if (row-1 >= 0) {
	        			neighborList.add(letterdict.get(letters.charAt((row-1)*5+col)));
	        		}
	        		if (col+1 < 5) {
	        			neighborList.add(letterdict.get(letters.charAt(row*5+col+1)));
	        		}
	        		if (row+1 < 5) {
	        			neighborList.add(letterdict.get(letters.charAt((row+1)*5+col)));
	        		}
	        		if (col-1 >= 0) {
	        			neighborList.add(letterdict.get(letters.charAt(row*5+col-1)));
	        		}
	        		int size = neighborList.size();
	        		for (int i=0; i<size; i++) {
	        			neighborList.add(1-letterdict.get(letters.charAt(row*5+col)));
	        		}
	                double sum = 0.;
	                for (double n: neighborList) {
	                	sum += n;
	                }
	                size = neighborList.size();
	                u[row*5+col] = Math.round((uw + upw * (sum / size))*100)/100.;
	        	}
	        }
	        cache.put(letters, new CacheEntry(found,new ArrayList<String>(), d, u));
	        hashTable.put(letters, new HashMap<Position,Double>());
			return found;
		}
	}
	
	
	/**
	 * @param allLetters letters on the board
	 * @param needLetters letters required to use (all of these)
	 * @param notLetters do not use these letters 
	 * @param anyLetters use at least one of these letters
	 * @return list of words that match the input criteria, filtered from possible(allLetters)
	 */
	private ArrayList<String> concentrate(String allLetters, String needLetters, String notLetters, String anyLetters) {
		allLetters = allLetters.toUpperCase();
		needLetters = needLetters.toUpperCase();
		notLetters = notLetters.toUpperCase();
		anyLetters = anyLetters.toUpperCase();
		ArrayList<String> found = possible(allLetters);
		// remove words that use all the needLetters
		ArrayList<String> needLetterList = new ArrayList<String>();
		if (!needLetters.equals("")) {
			for (String word: found) {
				boolean good = true;
				for (int i=0; i<needLetters.length(); i++) {
					char l = needLetters.charAt(i);
					int wordcnt = 0;
					for (int wi=0; wi < word.length(); wi++) {
						if (l == word.charAt(wi)) {
							wordcnt++;
						}
					}
					int needcnt = 0;
					for (int ni=0; ni<needLetters.length(); ni++) {
						if (l == needLetters.charAt(ni)) {
							needcnt++;
						}
					}
					if (wordcnt < needcnt) {
						good = false;
						break;
					}
				}
				if (good) {
					needLetterList.add(word);
				}
				
			}
		}
		else {
			needLetterList = found;
		}
		// remove words that use notLetters
		ArrayList<String> notLetterList = new ArrayList<String>();
		if (!notLetters.equals("")) {
			for (String word: needLetterList) {
				boolean good = true;
				for (int i=0; i<notLetters.length(); i++) {
					char l = notLetters.charAt(i);
					int wordcnt = 0;
					for (int wi=0; wi < word.length(); wi++) {
						if (l == word.charAt(wi)) {
							wordcnt++;
						}
					}
					int notcnt = 0;
					for (int ni=0; ni<notLetters.length(); ni++) {
						if (l == notLetters.charAt(ni)) {
							notcnt++;
						}
					}
					int allcnt = 0;
					for (int ai=0; ai<allLetters.length(); ai++) {
						if (l == allLetters.charAt(ai)) {
							allcnt++;
						}
					}
					if (wordcnt > allcnt - notcnt) {
						good = false;
						break;
					}
				}
				if (good) {
					notLetterList.add(word);
				}
				
			}
		}
		else {
			notLetterList = needLetterList;
		}
		// remove words that don't use anyLetters
		ArrayList<String> anyLetterList = new ArrayList<String>();
		if (!anyLetters.equals("")) {
			for (String word: notLetterList) {
				boolean good = false;
				for (int i=0; i<anyLetters.length(); i++) {
					char l = anyLetters.charAt(i);
					if (word.indexOf(l) != -1) {
						good = true;
						break;
					}
				}
				if (good) {
					anyLetterList.add(word);
				}
			
			}
		}
		else {
			anyLetterList = notLetterList;
		}
		// remove words that are prefixes of already-played words
        ArrayList<String> goodList = new ArrayList<String>();
		for (String word1: anyLetterList) {
			boolean good = true;
			int len1 = word1.length();
			for (String word2: cache.get(allLetters).played) {
				if (word2.length() > len1) {
					if (word2.substring(0,len1).equals(word1)) {
						good = false;
						break;
					}
				}
			}
			if (good) {
				goodList.add(word1);
			}
		}
		return goodList;
	}
	

	
	private int computeGoal(String letters, Position p, int move) {
		ArrayList<Integer> goodGoals = new ArrayList<Integer>();
		ArrayList<Integer> unoccupied = new ArrayList<Integer>();
		int unclaimed = p.getUnclaimed();
		for (int x=0; x<25; x++) {
			if ( ((1<<x) & unclaimed) == (1<<x) ) {
				unoccupied.add(x);
			}
		}
		ArrayList<String> lst = possible(letters);
		for (int r=2; r<unoccupied.size(); r++) {
			ArrayList<ArrayList<Integer>> combos = combinations(unoccupied, r);
			for (ArrayList<Integer> combo : combos) {
				String goalString = "";
				for (int i : combo) {
					goalString += letters.charAt(i);
				}
				boolean goodGoal = true;
				for (String word : lst) {
					boolean goodWord = true;
					for (int i=0; i<goalString.length(); i++) {
						char l = goalString.charAt(i);
						int wordCount = 0;
						for (int x=0; x<word.length(); x++) {
							if (word.charAt(x) == l) {
								wordCount++;
							}
						}
						int goalStringCount = 0;
						for (int x=0; x<goalString.length(); x++) {
							if (goalString.charAt(x) == l) {
								goalStringCount++;
							}
						}
						if (wordCount < goalStringCount) {
							goodWord = false;
							break;
						}
					}
					if (goodWord) {
						goodGoal = false;
						break;
					}
					
				}
				if (goodGoal) {
					int goalMap = 0;
					for (int i: combo) {
						goalMap |= (1<<i);
					}
					goodGoals.add(goalMap);
				}
			}
			if (goodGoals.size() > 0) {
				break;
			}
		}
		if (goodGoals.size() == 0) {
			return 0;
		} else {
			double maxGoalValue = 0;
			double val;
			int best = 0;
			for (int goal: goodGoals) {
				val = goalValue(goal, p, move);
				if (val > maxGoalValue) {
					maxGoalValue = val;
					best = goal;
				}
			}
			return best;
		}
	}
	
	private double goalValue(int goal, Position p, int move) {
		Coordinate c1;
		if (move == 1) {
			c1 = Position.centroid(p.blue);
		} else {
			c1 = Position.centroid(p.red);
		}
		Coordinate c2 = Position.centroid(goal);
		return c1.distanceBetween(c2);
	}
	
	private HashMap<String, ArrayList<String>> groupWords(ArrayList<String> words, String anyl) {
		HashMap<String, ArrayList<String>> wordgroups = new HashMap<String, ArrayList<String>>();
		for (String word: words) {
			//group = ''.join(sorted([l for l in word if l in anyl]))
			ArrayList<Character> group = new ArrayList<Character>();
			for (int i=0; i<word.length(); i++) {
				char l = word.charAt(i);
				if (anyl.indexOf(l) != -1) {
					group.add(l);
				}
			}
			Collections.sort(group);
			String groupStr = "";
			for (int i=0; i<group.size(); i++) {
				groupStr += group.get(i);
			}
			if (wordgroups.containsKey(groupStr)) {
				wordgroups.get(groupStr).add(word);
			} else {
				ArrayList<String> newList = new ArrayList<String>();
				newList.add(word);
				wordgroups.put(groupStr, newList);
			}
		}
		return wordgroups;
	}
	
	public double evaluatePosition(String letters, Position p) {
		if (hashTable.get(letters).containsKey(p)) {
			return hashTable.get(letters).get(p);
		}
		
		String x = Integer.toBinaryString(p.red|p.blue);
		boolean ending = true;
		if (x.length() == 25) {
			
			for (int i=0; i<x.length(); i++) {
				if (x.charAt(i) == '0') {
					ending = false;
					break;
				}
			}
		} else {
			ending = false;
		}
		double total;
		if (!ending) {
			double [] d = cache.get(letters).d;
			double [] u = cache.get(letters).u;
			double blueScore = 0;
			double redScore = 0;
			int blueDef = p.getBlueDef();
			int redDef = p.getRedDef();
			for (int i=0; i<25; i++) {
				if ((blueDef & (1<<i)) > 0) {
					blueScore += d[i];
				} else if ((p.blue & (1<<i)) > 0) {
					blueScore += u[i];
				} else if ((redDef & (1<<i)) > 0) {
					redScore += d[i];
				} else if ((p.red & (1<<i)) > 0) {
					redScore += u[i];
				}
			}
            Coordinate bluecenter = Position.centroid(p.blue);
            Coordinate redcenter = Position.centroid(p.red);
            Coordinate zerocenter = Position.centroid(p.getUnclaimed());
            double bluediff = bluecenter.distanceBetween(zerocenter);
            double reddiff = redcenter.distanceBetween(zerocenter);
            total = blueScore - redScore + mw*(bluediff - reddiff);
		} else {
			int blueCount = 0;
			String blueStr = Integer.toBinaryString(p.blue);
			for (int i=0; i<blueStr.length(); i++) {
				if (blueStr.charAt(i) == '1')
					blueCount++;
			}
			int redCount = 0;
			String redStr = Integer.toBinaryString(p.red);
			for (int i=0; i<redStr.length(); i++) {
				if (redStr.charAt(i) == '1')
					redCount++;
			}
			total = (blueCount - redCount) * 1000;
		}
		hashTable.get(letters).put(p, total);
		return total;
	}
	
	
	private HashMap<Position,Double> arrange(String letters, String group, Position initP, ArrayList<Integer> used, int move ) {
		HashMap<Position,Double> scores = new HashMap<Position,Double>();
		int origBlueDef = initP.getBlueDef();
		int origRedDef = initP.getRedDef();
		// for each unique letter, get a list of tuples for the indexes it can be played in
		HashMap<Character, Integer> wordHist = new HashMap<Character, Integer>();
		for (int i=0; i<group.length(); i++) {
			char l = group.charAt(i);
            if (wordHist.containsKey(l)) {
            	wordHist.put(l, wordHist.get(l)+1);
            } else {
            	wordHist.put(l, 1);
            }
		}
		ArrayList<ArrayList<ArrayList<Integer>>> letterOptions = new ArrayList<ArrayList<ArrayList<Integer>>>();
		for (char l:wordHist.keySet()) {
			ArrayList<Integer> ilist = new ArrayList<Integer>();
			for (int i=0; i<25; i++) {
				if (letters.charAt(i) == l && used.indexOf(i) == -1) {
					ilist.add(i);
				}
			}
			int r = wordHist.get(l);
			ArrayList<ArrayList<Integer>> combos = combinations(ilist,r); 
			letterOptions.add(combos);
		}
		
		// create a new list with enough elements to hold all the options above (multiply the length of all the lists)
		int lenWordPlays=1;
		int lenOtherDim=group.length();
		for (ArrayList<ArrayList<Integer>> lst : letterOptions) {
			int s = lst.size();
			lenWordPlays *= s;
			if (s==1) {
				lenOtherDim -= lst.get(0).size();
			}
		}
		int [][] wordPlays = new int [lenWordPlays][lenOtherDim];
		
		// write the options to wordplays to get all the ways to play this word
		// [[(1,),(2)],[(3,4),(3,5),(4,5)]] becomes [[1,3,4],[1,3,5],[1,4,5],[2,3,4],[2,3,5],[2,4,5]]
		Position onlyP = initP.clone();
				
        int divisor = 1;
        int option = 0;
        for (ArrayList<ArrayList<Integer>> letterPlays : letterOptions) {
            divisor *= letterPlays.size();
            int cutoff = lenWordPlays / divisor;
            if (letterPlays.size() > 1) {
                for (int playIndex=0; playIndex<lenWordPlays; playIndex++) {
                    int lookup = (playIndex/cutoff) % letterPlays.size();
                    int tempOption = option;
                    for (int index : letterPlays.get(lookup)) {
                        wordPlays[playIndex][tempOption] = index;
                        tempOption++;
                    }
                }
                option+=letterPlays.get(0).size();
            } else {  // if there's only one place for a letter (or group of letters), just modify the board for it, don't create another loop iteration
                for (int i : letterPlays.get(0)) {
                    if (move == 1 && (1<<i & origRedDef) == 0) {
                    	onlyP.blue |= (1<<i); //set 1 to position i
                    	onlyP.red &= ~(1<<i); //set 0 to position i
                    } else if (move == -1 && (1<<i & origBlueDef) == 0) {
                    	onlyP.blue &= ~(1<<i); //set 0 to position i
                    	onlyP.red |= (1<<i); //set 1 to position i
                    }
                }
            }
        }
        
		
        // for each play create new positions using the play indexes, and evaluate each position
        Position newP = onlyP.clone();
        double score = 0;
        for (int [] play : wordPlays) {
            for (int i : play ) {
                if (move == 1 && (1<<i & origRedDef) == 0) {
                	newP.blue |= (1<<i); //set 1 to position i
                	newP.red &= ~(1<<i); //set 0 to position i
                } else if (move == -1 && (1<<i & origBlueDef) == 0) {
                	newP.blue &= ~(1<<i); //set 0 to position i
                	newP.red |= (1<<i); //set 1 to position i
                }
            }
            if (!scores.containsKey(newP))  {
                score = evaluatePosition(letters,newP);
                scores.put(newP,score);
            }
            newP = onlyP.clone();
        }
		return scores;
	}
	
	/**
	 * @param pool group to choose from
	 * @param r number of elements to choose
	 * @return list of combinations (lists of integers) from pool of size r
	 * translated from https://docs.python.org/3.4/library/itertools.html#itertools.combinations
	 */
	private ArrayList<ArrayList<Integer>> combinations(ArrayList<Integer> pool, int r) {
		ArrayList<ArrayList<Integer>> combos = new ArrayList<ArrayList<Integer>>();
	    int n = pool.size();
	    ArrayList<Integer> indices = new ArrayList<Integer>();
	    ArrayList<Integer> revRangeR = new ArrayList<Integer>();
	    for (int x=0;x<r;x++) {
	    	revRangeR.add(x);
	    	indices.add(x);
	    }
	    Collections.reverse(revRangeR);
	    ArrayList<Integer> combo = new ArrayList<Integer>();
	    for (int i=0; i<indices.size(); i++) {
	    	combo.add(pool.get(i));
	    }
	    combos.add(combo);
	    while (true) {
	    	combo = new ArrayList<Integer>();
	    	int i = 0;
	    	boolean foundIt = false;
	    	for (int I: revRangeR) {
	            if (indices.get(I) != I + n - r) {
	            	i = I;
	            	foundIt = true;
	                break;
	            }
	    	}
	    	if (!foundIt) {
	    		break;
	    	}
	        indices.set(i, indices.get(i) + 1);
	        for (int j=i+1; j<r; j++) {
	            indices.set(j, indices.get(j-1) + 1);
	        }
		    for (int k=0; k<indices.size(); k++) {
		    	combo.add(pool.get(indices.get(k)));
		    }
	        combos.add(combo);
	    }
	    return combos;
	}

	private class lengthAscending implements Comparator<String> {
		@Override
		public int compare(String o1, String o2) {
			return Integer.compare(o1.length(), o2.length());
		}
	};
	
	
	private boolean playIsSafe(ArrayList<String> group, String play ) {
		ArrayList<String> newGroup = new ArrayList<String>();
		for (String word: group) {
			int l = word.length();
			if (l > play.length()) {
				l = play.length();
			}
			if (!play.substring(0,l).equals(word)) {
				newGroup.add(word);
			}
		}
		group = newGroup;
		Collections.sort(group,new lengthAscending());
		HashMap<String,Integer> category = new HashMap<String,Integer>();
		HashSet<String> children = new HashSet<String>();
		for (int i=0; i<group.size(); i++) {
			String word1 = group.get(i);
			ArrayList<String> myChildren = new ArrayList<String>();
			if (!children.contains(word1)) {
				int l = word1.length();
				for (int j=i+1; j<group.size(); j++) {
					String word2 = group.get(j);
					if (word2.substring(0,l).equals(word1)) {
						myChildren.add(word2);
						children.add(word2);
					}
				}
				if (myChildren.size() > 0) {
					boolean dubble = true;
					Collections.sort(myChildren,new lengthAscending());
					for (int m=0; m<myChildren.size()-1; m++) {
						String child1 = myChildren.get(m);
						int cl = child1.length();
						for (int n=m+1; n<myChildren.size(); n++) {
							String child2 = myChildren.get(n);
							if (child2.substring(0,cl).equals(child1)) {
								if (category.containsKey("big")) {
									category.put("big", category.get("big") + 1);
								} else {
									category.put("big", 1);
								}
								dubble = false;
							}
						}
					}
					if (dubble) {
						if (category.containsKey("double")) {
							category.put("double", category.get("double") + 1);
						} else {
							category.put("double", 1);
						}
					}
					
				} else {
					if (category.containsKey("single")) {
						category.put("single", category.get("single") + 1);
					} else {
						category.put("single", 1);
					}
				}
			}
		}
		if (category.containsKey("big")) {
			return false;
		} else {
			if (!category.containsKey("single")) {
				category.put("single", 0);
			}
			if (!category.containsKey("double")) {
				category.put("double", 0);
			}
			if (category.get("single")%2==0 && category.get("double")%2==0) {
				return true;
			} else {
				return false;
			}	
		}		
	}
	
	
	public ArrayList<Play> decide(String letters, String colors, String needLetters, String notLetters, int move) {
		Position pOrig = new Position(colors);
		// letters to focus on are undefended opponent and unclaimed
		int targets;
		if (move == 1) {
			targets = (pOrig.red & ~pOrig.getRedDef()) | (~pOrig.blue & ~pOrig.red);
		} else {
			targets = (pOrig.blue & ~pOrig.getBlueDef()) | (~pOrig.blue & ~pOrig.red);
		}
		String anyl = "";
		ArrayList<Integer> dontUse = new ArrayList<Integer>();
		int goal = 0;
		// find goal for notLetters if none given already, define anyl from targets
		if ((needLetters == null || needLetters.isEmpty()) && (notLetters == null || notLetters.isEmpty())) {
			int maxsize = 0;
			for (String word: possible(letters)) {
				if (word.length() > maxsize) {
					maxsize = word.length();
				}
			}
			if (maxsize < 13) {
				goal = computeGoal(letters, new Position(pOrig.blue,pOrig.red), move);
			}
			for (int i=0; i<25; i++) {
				char l = letters.charAt(i);
				if (((1<<i) & targets) > 0) {
					anyl += l;
				}
				if (((1<<i) & goal) > 0) {
					dontUse.add(i);
					notLetters += l;
				}
			}
		} else {
			for (int i=0; i<25; i++) {
				char l = letters.charAt(i);
				if (((1<<i) & targets) > 0) {
					anyl += l;
				}
			}
		}
//		if (goal > 0) {
//			System.out.println("goal: "+notLetters + ' ' + Integer.toBinaryString(goal));
//		}
		//get the list of words and group them
		ArrayList<String> words = concentrate(letters, needLetters, notLetters, anyl);
		HashMap<String, ArrayList<String>> wordGroups = groupWords(words,anyl);		
		//arrange
		ArrayList<Play> plays = new ArrayList<Play>();
		for (String group: wordGroups.keySet()) {
			Position newP = pOrig.clone();
			HashMap<Position,Double> scores = arrange(letters,group,newP,dontUse,move);
			for (Position p : scores.keySet()) {
				double score = Math.round(scores.get(p) * 1000) / 1000.;
				for (String word : wordGroups.get(group)) {
					Play thisplay = new Play(score, word, p, move);
					plays.add(thisplay);
				}
			}
		}
		//parity 
		int size = plays.size();
		double bestScore;
		double inc = 0;
		HashMap<Integer,Play> bestWords = new HashMap<Integer,Play>();
		ArrayList<String> group = new ArrayList<String>();
		if (size > 0 && move == 1) {
			inc = .0005;
			bestScore = -25000;	
			for (int i=0; i<plays.size(); i++) {
				Play p = plays.get(i);
				if (p.score > bestScore) {
					bestScore = p.score;
					bestWords = new HashMap<Integer,Play>();
					group = new ArrayList<String>();
					bestWords.put(i, p);
					group.add(p.word);
				} else if (p.score == bestScore) {
					bestWords.put(i, p);
					group.add(p.word);
				}				
			}
		} else if (size > 0 && move == -1) {
			inc = -.0005;
			bestScore = 25000;	
			for (int i=0; i<plays.size(); i++) {
				Play p = plays.get(i);
				if (p.score < bestScore) {
					bestScore = p.score;
					bestWords = new HashMap<Integer,Play>();
					group = new ArrayList<String>();
					bestWords.put(i, p);
					group.add(p.word);
				} else if (p.score == bestScore) {
					bestWords.put(i, p);
					group.add(p.word);
				}				
			}
		} else {
			return plays;
		}
		if (Math.abs(bestScore) < 1000) {
			for (int i : bestWords.keySet()) {
				Play p = bestWords.get(i);
				if (playIsSafe(group, p.word) ) {
					p.score = Math.round( (p.score + inc) * 10000) / 10000.;
					plays.set(i, p);
				}
			}
		}
		return plays;
	}
	
    public double ply2(String letters, Position p, int move) {
        String colors = p.toColors().replace(" ","");
        ArrayList<Play> plays = decide(letters, colors, "", "", -move);
        double newScore;
        if (move == 1) {
            double min = 25000;
            for (Play play: plays) {
            	if (play.score < min) {
            		min = play.score;
            	}
            }
            newScore = min;
        } else {
            double max = -25000;
            for (Play play: plays) {
            	if (play.score > max) {
            		max = play.score;
            	}
            }
            newScore = max;
        }
        return newScore;
    }
	/**
	 * 
	 * @param letters
	 * @param p
	 * @param move
	 * @return -1 for losing, 1 for ending soon, 0 for not ending soon
	 * 
	 */
	public int endgameCheck(String letters, Position p, int move) {
		String unclaimedLetters = "";
		int unclaimed = p.getUnclaimed();
		String anyl = "";
		int targets;
		if (move == 1) {  // reversed here for opponent's reply
			targets = (p.blue & ~p.getBlueDef()) | unclaimed;
		} else {
			targets = (p.red & ~p.getRedDef()) | unclaimed;
		}
		for (int i=0; i<25; i++) {
			if ( ((1<<i) & targets) > 0) {
				anyl += letters.charAt(i);
			}
			if ( ((1<<i) & unclaimed) > 0) {
				unclaimedLetters += letters.charAt(i);
			}
		}
		ArrayList<String> gameEndingWords = new ArrayList<String>();
		if (unclaimed != 0) {
			if (cache.containsKey(letters+unclaimedLetters)) {
				gameEndingWords = cache.get(letters+unclaimedLetters).found;
			} else {
				gameEndingWords = concentrate(letters,unclaimedLetters,"","");
				cache.put(letters+unclaimedLetters, new CacheEntry(gameEndingWords));
			}
			Set<String> endingGroups = groupWords(gameEndingWords,anyl).keySet();
			for (String group: endingGroups) {
				ArrayList<Integer> none = new ArrayList<Integer>();
				HashMap<Position, Double> scores = arrange(letters, group, p, none, -move);
				if (move == 1) {
					for (double score : scores.values()) {
						if (score <= -1000) {
							return -1;
						}
					}
				} else {
					for (double score : scores.values()) {
						if (score >= 1000) {
							return -1;
						}
					}
				}
			}
			if (gameEndingWords.size() > 0) {
				return 1;
			} else {
				return 0;
			}
		} else {
			return 1;
		}
	}
	
    public void playWord(String letters, String word) {
        cache.get(letters).played.add(word);
    }

    public void resetPlayed(String letters, ArrayList<String> words) {
    	if (! cache.containsKey(letters)) {
    		possible(letters);
    	}
        cache.get(letters).played = words;
    }

    public void unplayWord(String letters, String word) {
        if (cache.get(letters).played.indexOf(word) != -1) {
        	cache.get(letters).played.remove(word);
        }
    }
	
	public Play turn(String letters, String colors, int move) {
		letters = letters.toUpperCase();
		colors = colors.toUpperCase();
		colors = colors.replace(" ", "");
		ArrayList<Play> plays = decide(letters, colors, "", "", move);
		int choice = 0;
		if (random) {
			Random r = new Random();
			choice = r.nextInt(plays.size());
		} else {
			Collections.sort(plays, new ComparePlay(move));
			int result;
			int max = (plays.size() > 1000) ? 1000 : plays.size();
			for (int i=0; i<max; i++) {
				Play play = plays.get(i);
				result = endgameCheck(letters, play.position, move);
				if (result >= 0) {
					if (move == 1) {
						if (play.score > -1000) {
							choice = i;
							break;
						} else {
							break;
						}
					} else {
						if (play.score < 1000) {
							choice = i;
							break;
						} else {
							break;
						}						
					}
				}
			}
		}
		if (plays.size() > 0) {
			playWord(letters,plays.get(choice).word);
			return plays.get(choice);
		} else {
			Position p = new Position(colors);
			double val = evaluatePosition(letters,p);
			return new Play(val, "", p, move);
		}
	}
}
