package test.java;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.java.Player;
import main.java.PlayerBeta;
import main.java.utility.Play;

public class Arena {

	private char dict = 'A';
	private int maxWordLen = 25;
	private boolean random = false;
	
	public static void main(String[] args) {
		
	}
	public static final String vowels = "AEIOU";
	public static final ArrayList<Character> letterList;
	private static Random r = new Random();
	private Logger logger;
    static {
    	letterList = new ArrayList<Character>();
		ArrayList<Character> fileList = new ArrayList<Character>(); 
		try {
			BufferedReader wordFile = new BufferedReader(new FileReader("src/main/resources/en15.txt"));
			String line;
			while ((line = wordFile.readLine()) != null) 
			{
				line = line.toUpperCase();
				for (int i=0; i<line.length(); i++) {
					fileList.add(line.charAt(i));
				}
			}
			wordFile.close();		
		} catch (Exception e) {
			System.out.println("Exception in Arena(): "+e.getMessage());
			e.printStackTrace();
		}
		HashMap<Character, Integer> letterHist = new HashMap<Character, Integer>();
		
		for (int i=0; i<fileList.size(); i++) {
			char l = fileList.get(i);
			if (vowels.indexOf(l) == -1) {
				if (letterHist.containsKey(l)) {
					letterHist.put(l, letterHist.get(l)+1);
				} else {
					letterHist.put(l, 1);
				}
			}
		}
		double tot = letterHist.size();
		double minimum = .25 * letterHist.get('N') / tot;
		for ( char l : letterHist.keySet()) {
			double newVal;
			if (letterHist.get(l)/tot > minimum) {
				newVal = letterHist.get(l) / tot / minimum * 100;
			} else {
				newVal = 100;
			}
			for (int i=0; i<newVal; i++) {
				letterList.add(l);
			}
		}
    }
    
    public static String generateLetters() {
    	ArrayList<Character> pickedLetters;
    	while (true) {
    		pickedLetters = new ArrayList<Character>();
	    	int vowelCount = r.nextInt(6) + 3;
	    	int tot = 25 - vowelCount;
	    	ArrayList<Character> pickedVowels = new ArrayList<Character>();
	    	for (int i=0; i<vowelCount; i++) {
	    		int whichVowel = r.nextInt(vowels.length());
	    		pickedVowels.add(vowels.charAt(whichVowel));
	    	}
	    	for (int i=0; i<tot; i++) {
	    		int whichConsonant = r.nextInt(letterList.size());
	    		pickedLetters.add(letterList.get(whichConsonant));
	    	}
	    	if (pickedLetters.indexOf('Q') != -1) {
	    		if (pickedVowels.indexOf('I') == -1) {
	    			continue;
	    		}
	    	}
	    	pickedLetters.addAll(pickedVowels);
	    	Collections.shuffle(pickedLetters);
	    	break;
    	}
    	String letters = "";
    	for (char c : pickedLetters) {
    		letters += c;
    	}
    	return letters;
    }

    public Arena() {
    	logger = Logger.getLogger("test.java.arena");
    	Formatter f = new SingleLineFormatter();
    	logger.setUseParentHandlers(false);
    	Handler ch = new MyConsoleHandler();
    	ch.setFormatter(f);
    	ch.setLevel(Level.ALL);
    	logger.setLevel(Level.ALL);
    	logger.addHandler(ch);

    	try {
    	Handler fh = new FileHandler("src/test/logs/arena.log", true);
    	fh.setLevel(Level.INFO);
    	fh.setFormatter(f);
    	logger.addHandler(fh);
    	} catch (Exception e) {
    		System.out.println("Exception in Arena()");
    		e.printStackTrace();
    	}
    }

    class GameResult {
    	public String winner;
    	public int numWordsPlayed;
    	public double bTime;
    	public double rTime;
    	public String fnWithPath;
    	public GameResult(String winner, int numWordsPlayed, double bTime, double rTime, String fn) {
    		this.winner = winner;
    		this.numWordsPlayed = numWordsPlayed;
    		this.bTime = bTime;
    		this.rTime = rTime;
    		this.fnWithPath = fn;
    	}
    	
    }
    
    
    
    public GameResult game(String letters, boolean stableFirst) {
    	if (letters == null || letters.equals("")) {
    		letters = Arena.generateLetters();
    		logger.info(letters);
    	}
    	letters = letters.toUpperCase();
    	String fileName = "";
    	Player b;
    	Player r;
    	SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
    	String date = df.format(new Date()); 
    	if (stableFirst) {
    		b = new Player(dict, maxWordLen, random);
    		logger.fine("Stable player goes first, plays blue");
    		r = new PlayerBeta(dict, maxWordLen, random);
    		logger.fine("Beta player goes second, plays red");
    		fileName = date + "_bsrb.cgd";
    	} else {
    		b = new PlayerBeta(dict, maxWordLen, random);
    		logger.fine("Beta player goes first, plays blue");
    		r = new Player(dict, maxWordLen, random);
    		logger.fine("Stable player goes second, plays red");
    		fileName = date + "_bbrs.cgd";
    	}
    	String fnWithPath = "src/test/games/" + fileName;
    	logger.info(fnWithPath);
    	ArrayList<String> saveList = new ArrayList<String>();
    	b.possible(letters);
    	r.possible(letters);
    	int turn = 1;
    	String colors = "----- ----- ----- ----- -----";
    	saveList.add(letters);
    	saveList.add("[Initial Position],0," + colors.replace(" ", ""));
    	ArrayList<String> playedWords = new ArrayList<String>();
    	boolean bluePassed = false;
    	boolean redPassed = false;
    	double btime, rtime;
    	btime = rtime = 0;
    	double score = 0;
    	double oldscore = 0;
    	boolean early = false;
    	Play p = new Play();
    	while (colors.indexOf('-') != -1) {
    		if (turn == 1) {
    			oldscore = score;
    			Date start = new Date();
    			p = b.turn(letters, colors, turn);
    			Date end = new Date();
    			double timeTaken = (end.getTime() - start.getTime())/1000.;
    			colors = p.position.toColors();
    			score = p.score;
    			playedWords.add(p.word);
    			r.playWord(letters, p.word);
    			btime += timeTaken;
    			logger.fine("blue plays "+String.format("%1$25s", p.word)+" "+colors+" "+playedWords.size()+" "+p.position.numScore()+" "+timeTaken+" seconds "+" "+p.score);
    			bluePassed = p.word.equals("");
    			saveList.add(p.word+","+p.score+","+colors.replace(" ", "")+",blue");
    			turn = -1;    			
    		} else {
    			oldscore = score;
    			Date start = new Date();
    			p = r.turn(letters, colors, turn);
    			Date end = new Date();
    			double timeTaken = (end.getTime() - start.getTime())/1000.;
    			colors = p.position.toColors();
    			playedWords.add(p.word);
    			b.playWord(letters, p.word);
    			rtime += timeTaken;
    			logger.fine(" red plays "+String.format("%1$25s", p.word)+" "+colors+" "+playedWords.size()+" "+p.position.numScore()+" "+timeTaken+" seconds "+" "+p.score);
    			redPassed = p.word.equals("");
    			saveList.add(p.word+","+p.score+","+colors.replace(" ", "")+",red");
    			turn = 1;
    		}
        	if ((playedWords.size() > 100) && ((score > 0 && oldscore > 0) || (score < 0 && oldscore < 0))) {
        		early = true;
        		break;
        	}
        	if (bluePassed && redPassed) {
        		break;
        	}
    	}
        BufferedWriter writer = null;
        try {
            File f = new File(fnWithPath);
            writer = new BufferedWriter(new FileWriter(f));
            for (String line: saveList) {
            	writer.write(line+'\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            }
        }
        GameResult result = new GameResult("",playedWords.size(), btime, rtime, fnWithPath);
    	int blueCount = p.position.blueCount();
    	int redCount = p.position.redCount();
        if (!early) {
        	if (blueCount > redCount) {
        		logger.fine("Blue wins!");
        		result.winner = "blue";
        		return result;
        	} else if (redCount > blueCount) {
        		logger.fine("Red wins!");
        		result.winner = "red";
        		return result;
        	} else {
        		logger.fine("Tie");
        		return result;
        	}
        } else {
        	if (p.score > 0) {
        		logger.fine("Blue wins!");
        		result.winner = "blue";
        		return result;
        	} else {
        		logger.fine("Red wins!");
        		result.winner = "red";
        		return result;
        	}
        }
    }
    
    class BothResult {
    	public int stableWinCount;
    	public int betaWinCount;
    	public String winner;
    	public double stableTime;
    	public double betaTime;
    	public BothResult(int sW, int bW, String w, double sT, double bT) {
    		stableWinCount = sW;
    		betaWinCount = bW;
    		winner = w;
    		stableTime = sT;
    		betaTime = bT;
    	}
    }
    
    public BothResult both(String letters) {
    	if (letters == null || letters.equals("")) {
    		letters = Arena.generateLetters();
    		logger.fine(letters);
    	}
    	GameResult result1 = game(letters, true);
    	logger.info("result: "+result1.winner+" "+result1.numWordsPlayed+" words played");
    	GameResult result2 = game(letters, false);
    	logger.info("result: "+result2.winner+" "+result2.numWordsPlayed+" words played");
    	
    	double stableTime = result1.bTime + result2.rTime;
    	double betaTime = result1.rTime + result2.bTime;
    	int stableWins = 0;
    	int betaWins = 0;
    	int stableLen = 0;
    	int betaLen = 0;
    	if (result1.winner.equals("blue")) {
    		stableWins++;
    		stableLen+=result1.numWordsPlayed;
    	} else if (result1.winner.equals("red")){
    		betaWins++;
    		betaLen+=result1.numWordsPlayed;
    	} else {
    		stableLen+=result1.numWordsPlayed;
    		betaLen+=result1.numWordsPlayed;
    	}
    	if (result2.winner.equals("blue")) {
    		betaWins++;
    		betaLen+=result2.numWordsPlayed;
    	} else if (result2.winner.equals("red")) {
    		stableWins++;
    		stableLen+=result2.numWordsPlayed;
    	} else {
    		stableLen+=result1.numWordsPlayed;
    		betaLen+=result1.numWordsPlayed;
    	}
    	if (stableWins > betaWins) {
    		logger.info("Stable won both games");
    		return new BothResult(stableWins,betaWins,"stable",stableTime, betaTime);
    	} else if (stableWins < betaWins) {
    		logger.info("Beta won both games");
    		return new BothResult(stableWins,betaWins,"beta",stableTime, betaTime);
    	} else {
    		String msg = "Players tied 1-1. ";
    		if (stableLen < betaLen) {
    			logger.info(msg + "However Stable won faster.");
    			return new BothResult(stableWins,betaWins,"stable",stableTime, betaTime);
    		} else if (stableLen > betaLen) {
    			logger.info(msg + "However Beta won faster.");
    			return new BothResult(stableWins,betaWins,"beta",stableTime, betaTime);
    		} else {
    			logger.info(msg + "They both played the same amount of moves to win.");
    			return new BothResult(stableWins,betaWins,"tie",stableTime, betaTime);
    		}
    	}
    }
    
    public void match(int count) {
    	int stableWins = 0;
    	int betaWins = 0;
    	int equal = 0;
    	int stableFaster = 0;
    	int betaFaster = 0;
    	double[] times = new double[count];
    	double stableTime = 0;
    	double betaTime = 0;
    	logger.info("");
    	logger.info("");
    	logger.info("MATCH BEGINS - Dictionary: " + dict + ", Max Word Length: "+ maxWordLen + ", Random: " + random);
    	logger.info("");
    	DecimalFormat four = new DecimalFormat("0000");
    	for (int gameNum=1; gameNum<=count; gameNum++) {
    		logger.info("");
    		logger.info("***********************   GAME "+four.format(gameNum)+ "   ***********************");
    		logger.info("Stable Wins: "+stableWins+" Beta Wins: " + betaWins+ " Stable Faster: " + stableFaster + " Beta Faster: "+betaFaster + " Equal: "+ equal);
    		logger.fine("");
    		Date start = new Date();
    		BothResult result = both("");
    		Date end = new Date();
    		times[gameNum-1] = (end.getTime() - start.getTime())/1000.;
    		stableTime += result.stableTime;
    		betaTime += result.betaTime;
    		if (result.stableWinCount > result.betaWinCount) {
    			stableWins++;
    		} else if (result.stableWinCount < result.betaWinCount) {
    			betaWins++;
    		} else {
    			if (result.winner.equals("beta")) {
    				betaFaster++;
    			} else if (result.winner.equals("stable")) {
    				stableFaster++;
    			} else {
    				equal++;
    			}
    		}
    	}
    	logger.info("");
    	logger.info("***********************     RESULT     ***********************");
		logger.info("Stable Wins: "+stableWins+" Beta Wins: " + betaWins+ " Stable Faster: " + stableFaster + " Beta Faster: "+betaFaster + " Equal: "+ equal);
		if (stableTime > betaTime) {
	        logger.info("Stable was "+ (stableTime-betaTime)/betaTime*100 +" percent slower");
		} else if (stableTime < betaTime) {
			logger.info("Beta was "+ (betaTime-stableTime)/stableTime*100 +" percent slower");
		}
		double sum = 0;
    	for (int i=0;i<count;i++) {
    		sum += times[i];
    	}
    	logger.info("average seconds per game: "+sum/(double)count);
    }
}
