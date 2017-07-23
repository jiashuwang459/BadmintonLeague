package main;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;


/**
 * @author SmashCity
 *
 */
public class Main {

	//Change the file name, depending on which league is playing.
	private static String fileName = "Saturday";
	public static boolean careful = true;

	private static ArrayList<Player> players = new ArrayList<Player>(); //all players playing today
	private static ArrayList<Player> pool = new ArrayList<Player>(); //all players who are not sitting out
	private static ArrayList<Player> out = new ArrayList<Player>(); //all players who haven't been out
	private static ArrayList<Player> extras = new ArrayList<Player>(); //all players who are out that current round
	
	private static ArrayList<Match> matches = new ArrayList<Match>(); //array of matches
	private static ArrayList<Pair> pairs = new ArrayList<Pair>(); //array of pairs (teams) so nobody gets same team again
	private static ArrayList<String> master = new ArrayList<String>(); //array of strings of every line in Master (everyone in the league)
	
	private static Scanner input = Utility.input;

	private static boolean single;

	private static int numGames;
	private static int numExtra;
	
	/**
	 * Reads from "Playing" and create players.
	 */
	private static void begin() {
		
		ArrayList<String> playing = Utility.read("Playing");
		master = Utility.read(fileName);
		
		for(String name: playing){
			String[] split = name.split("\t");
			String first = split[0];
			String last = split[1];
			
			find(first, last);
		}
	}
	
	/**
	 * General distribution of players for sitting out or playing.
	 */
	private static void round(){

		//Get total number of players
		int numPlayers = Player.getCount();
		
		numGames  = numPlayers / 4;
		numExtra = numPlayers % 4;
		
		//Populates the pool of players
		populate(Type.POOL);
		
		//Distributes players into sitting out or playing
		extras();
		matches();
		display();
	}
	
	/**
	 * Determines the players who will sit out or play singles.
	 */
	private static void extras(){
		
		//If there will be a singles match
		if (numExtra >= 2){
			single = true;
		}

		//Choose the people who are sitting out or playing singles.
		for (int extra = 0; extra < numExtra; extra++) {
			Player player = pick(Type.OUT);
			extras.add(player);
			pool.remove(player);
			
			//Once everyone sat out, repopulate the Out array.
			if (out.isEmpty()) {
				populate(Type.OUT);
				for (Player side : extras){
					out.remove(side);
				}
			}
		}
	}
	
	
	/**
	 * Distributes the players into matches.
	 */
	private static void matches(){
		for (int game = 1; game < numGames + 1; game++) {

			boolean unique = false;
			int errors = 0;
			do{
				//Clears array of pairs if too many repeats
				if(errors > 100){
					pairs.clear();
				}

				unique = true;

				//Pick four people
				Player first = pick(Type.POOL);
				Player second = pick(Type.POOL);
				Player third = pick(Type.POOL);
				Player fourth = pick(Type.POOL);

				//Create Pairs
				Pair one = new Pair(first, second);
				Pair two = new Pair(third, fourth);

				//Checks if it's a unique pair
				for(Pair pair:pairs){
					if ((one.equals(pair) || two.equals(pair)) && unique){
						unique = false;
						pool.add(first);
						pool.add(second);
						pool.add(third);
						pool.add(fourth);
						errors++;
					}
				}
				
				//Adds the unique pairs into the array so it doesn't repeat
				if(unique){
					pairs.add(one);
					pairs.add(two);
					Match match = new Match(one,two);
					matches.add(match);
				}

			}while(!unique);
			
			//Singles game
			if (single)	singles();
		}
	}
	
	/**
	 * Creates a singles game.
	 */
	private static void singles(){

		//Pick players from extra
		Player first = pick(Type.EXTRA);
		Player second = pick(Type.EXTRA);
	
		//Creates match
		Match match = new Match(first,second);
		matches.add(match);

	}

	/**
	 * Randomly chooses a Player from an array.
	 * @param type The type of array to choose the Player from.
	 * @return The Player picked from the array.
	 */
	private static Player pick(Type type) {

		Random rand = new Random();

		int num;
		Player picked = null;
		if (type == Type.POOL) {
			num = rand.nextInt(pool.size());
			picked = pool.get(num);
			pool.remove(picked);
		} else if (type == Type.OUT) {
			num = rand.nextInt(out.size());
			picked = out.get(num);
			out.remove(picked);
		} else if (type == Type.EXTRA) {
			num = rand.nextInt(extras.size());
			picked = extras.get(num);
			extras.remove(picked);
		}

		return picked;
	}

	/**
	 * Populates the arrays with all of the players playing.
	 * @param type The type of array to populate
	 */
	private static void populate(Type type) {
		for (Player player : players) {
			if (type == Type.POOL)
				pool.add(player);
			else if (type == Type.OUT)
				out.add(player);
		}
	}

	/**
	 * Starts the next round if all games are finished.
	 */
	private static void next() {
		boolean finished = true;
		
		for(Match match: matches)
			if (!match.finished())
				finished = false;
		
		if(finished){
			if(!matches.isEmpty())
				matches.clear();
			round();
		}
		else{

			System.out.println("The following matches have not been completed:");
			System.out.println();
			System.out.println("-------------------------------------");
			for(Match match: matches){
				if (!match.finished()){
					System.out.println("Match " + (matches.indexOf(match)+1) + ":");
					System.out.println();
					match.print();
				}
			}
		}
	}
	
	
	/**
	 * Displays all of the players currently playing.
	 */
	private static void view() {
		System.out.println();
		System.out.println("Number of Players: " + Player.getCount());
		for(Player player: players){
			player.print();
		}
		System.out.println();
	}
	
	
	/**
	 * Add a player.
	 */
	private static void add() {
		
		System.out.print("First Name:");
		String first = input.next();
		
		System.out.print("Last Name:");
		String last = input.next();
		
		boolean play = false;
		for(Player player: players)
			if(player.is(first, last)){
				System.out.println(player + " is already playing.");
				play = true;
			}
		
		if(!play){
			find(first,last);
			
		}
	}
	
	/**
	 * Finds  Player in the "Master" file and adds it.
	 * @param first The first name of the Player.
	 * @param last The last name of the Player.
	 */
	private static void find(String first, String last){

		boolean found = false;
		for(String line: master){
			String[] split1 = line.split("\t");
			String first1 = split1[0];
			String last1 = split1[1];				
			
			if (first.equals(first1) && last.equals(last1)){
				found = true;
				int wins = Integer.parseInt(split1[2]);
				int losses = Integer.parseInt(split1[3]);
				int ties = Integer.parseInt(split1[4]);
				players.add(new Player(first, last, wins, losses, ties));
			}
			
			
		}
		if(!found){
			add(first,last);
			players.add(new Player(first, last));
		}
	}
	
	
	/**
	 * Adds the player to the master
	 * @param first The first name of the Player.
	 * @param last The last name of the Player.
	 */
	private static void add(String first, String last) {
		String line = first + "\t" + last + "\t0\t0";
		master.add(line);
	}
	

	private static void remove() {
		
		System.out.print("First Name:");
		String first = input.next();
		
		System.out.print("Last Name:");
		String last = input.next();
		
		Player delete = null;
		for(Player player:players)
			if(player.is(first, last)){
				replace(player);
				delete = player;
			}
		delete.delete();
		players.remove(delete);
	}

	private static void replace(Player player) {
		
		for(String line: master){
			String[] split = line.split("\t");
			String first = split[0];
			String last = split[1];
			if (player.is(first, last)){
				int wins = player.getWins();
				int losses = player.getLosses();
				int ties = player.getTies();
				String newLine = first + "\t" + last + "\t" + wins + "\t" + losses + "\t" + ties;
				master.set(master.indexOf(line), newLine);
			}
		}
	}
	
	private static void display() {

		//Display Extra People
		if (!extras.isEmpty()) {
			System.out.println("-------------------------------------");
			System.out.println("The following people sit out:");
			System.out.println();
			for (Player player: extras) {
				System.out.println(player);
			}
			System.out.println();
		}
		
		System.out.println("-------------------------------------");

				
		for(Match match:matches){
			int num = matches.indexOf(match)+1;
			System.out.println("Match " + num + ":");
			System.out.println();
			match.print();
		}
	}
	
	private static void swap() {
			
		System.out.println("First person to switch out:");
		System.out.println();
		System.out.print("First Name:");
		String first1 = input.next();
		
		System.out.print("Last Name:");
		String last1 = input.next();
		
		System.out.println("Person to switch with?");
		System.out.println();
		System.out.print("First Name:");
		String first2 = input.next();
		
		System.out.print("Last Name:");
		String last2 = input.next();
		
		Player player1 = null;
		Player player2 = null;
		
		for(Player player: players){
			if(player.is(first1, last1))
				player1 = player;
			if(player.is(first2, last2))
				player2 = player;
		}
		
		if(player1 == null || player2 == null){
			System.out.println("One or more of the players you entered does not exist.");
			swap();
		}
		else{
			for(Match match: matches){
				match.swap(player1, player2);
			}
			for(Player player: extras){
				if(player.equals(player1)){
					player = player2;
				}
				else if(player.equals(player2)){
					player = player1;
				}
			}
		}
	}
	
	private static void score() {
		
		if(matches.size() == 0)
			System.out.println("All matches are complete.");
		else{
			System.out.println("What was your match number? ('0' to go back.)");

			int matchNum = Utility.validInt(0, matches.size());

			if(matchNum != 0){
				Match match = matches.get(matchNum-1);
				if(match.finished()){
					System.out.println("This match is already complete");
					score();
				}
				else{
					System.out.println("-------------------------------------");
					System.out.println();
					match.print();

					if(careful){
						System.out.println("Is this your match?");
						System.out.println();

						if(Utility.confirm())
							match.complete();
						else
							score();
					}
					else{
						match.complete();
					}
				}
			}
			else{
				//go back to main menu
			}
		}
		end();
	}
	
	/*
	private static void addPair(){
		
		System.out.println("First person to switch out:");
		System.out.println();
		System.out.print("First Name:");
		String first1 = input.next();
		
		System.out.print("Last Name:");
		String last1 = input.next();
		
		System.out.println("Person to switch with?");
		System.out.println();
		System.out.print("First Name:");
		String first2 = input.next();
		
		System.out.print("Last Name:");
		String last2 = input.next();
		
		Player player1 = null;
		Player player2 = null;
		
		for(Player player: players){
			if(player.is(first1, last1))
				player1 = player;
			if(player.is(first2, last2))
				player2 = player;
		}
		
		if(player1 == null){
			System.out.println(first1 + " " + last1 + " isn't a player. Would you like to add him?");
			if(Utility.confirm()){
				find(first1,last1);
			}
		}
		
	}
	
	
	private static void removePair(){
		
	}
	*/
	
	
	private static void end() {
		for(Player player: players){
			replace(player);
		}
		
		Utility.write("", fileName , false);
		for(String line: master){
			//System.out.println(line);
			Utility.write(line + "\n", fileName , true);
		}
	}
	
	private static void menu(){
		System.out.println("What would you like to do?");
		System.out.println();
		System.out.println("0. Start next round");
		System.out.println("1. View players");
		System.out.println("2. Add a player");
		System.out.println("3. Remove a player");
		System.out.println("4. Swap two players");
		//System.out.println("5. Create Pairs");
		//System.out.println("6. Remove Pairs");
		if(!matches.isEmpty()){
			System.out.println("5. Submit a score");
			System.out.println("6. View matches");
		}
		System.out.println("9. Save to " + fileName);
	}
	
	public static void main(String[] args) {

		begin();
		populate(Type.OUT);
		input = new Scanner(System.in);
		
		while(true){

			menu();

			int selection = Utility.validInt(0,9);

			switch(selection){
				case 0:	next();			break;
				case 1:	view();			break;
				case 2: add();			break;
				case 3: remove();		break;
				case 4: swap();			break;
				//case 5: addPair();		break;
				//case 6: removePair();	break;
				case 5: score();		break;
				case 6: display();		break;
				case 9: end();			break;
			}
			System.out.println("\n\n");
		}
	}
	
	
}
