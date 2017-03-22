package vote;

/**
 * This is the Voting Strategy implementation.
 * @author Jacques Breaux
 */


import java.util.ArrayList;
import java.util.Iterator;
import map.Strategy;
import map.Map;

public final class VoteStrategy implements Strategy {

	private int width;
	private int height;

	private ArrayList<Tile> fringe;
	private ArrayList<Tile> voteFringe;
	private Tile[][] board;

	void initialize(Map m) {
		width = m.columns();
		height = m.rows();
		fringe = new ArrayList<Tile>();
		voteFringe = new ArrayList<Tile>();
		board = new Tile[width][height];
		for (int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				board[i][j] = new Tile(i,j);
			}
		}
	}

	void ChooseRandom(Map m) {
		int x = m.pick(width);
		int y = m.pick(height);
		Reveal(x,y,m);
	}

	void RevealSurrounding(int x,int y, Map m) {
		int q;
		for(int i = x - 1; i <= x + 1 ; i++) {
			for(int j = y - 1; j <= y + 1 ; j++) {
				if(i == x && j == y) {
					continue;
				}
				q = m.look(i,j);
				if(q == Map.UNPROBED) {
					Reveal(i,j,m);
				}
			}
		}
	}

	void Reveal(int x, int y, Map m) {
		int q = m.probe(x,y);
		if(q == 0) {
			RevealSurrounding(x,y,m);
		} else {
			voteFringe.remove(board[x][y]);
			fringe.add(board[x][y]);
			for(int i = x - 1; i <= x + 1; i++) {
				for(int j = y - 1; j <= y+1; j++) {
					if(m.look(i,j) == Map.UNPROBED && !voteFringe.contains(board[i][j])) {
						voteFringe.add(board[i][j]);
					}
				}
			}
		}
	}

	void VotePhase(Map m) {
		for(Tile t: voteFringe) {
			t.Reset();//Reset for votes
		}
		Iterator<Tile> iFringe = fringe.iterator();
		while (iFringe.hasNext()) {
			Tile t = iFringe.next();
			int i = t.x - 1 < 0 ? 0 : t.x - 1;
			int j = t.y - 1 < 0 ? 0 : t.y - 1;
			int x = i;
			int y = j;
			int unprobed = 0;
			for(;i < t.x + 1 && i < width; i++) {
				for(;j < t.x + 1 && j < height; j++) {
					if(m.look(i,j) == Map.UNPROBED){
						unprobed++;//counting unprobed neighbors
					}
				}
			}
			if(unprobed == 0) { //remove from fringe, it's done
				iFringe.remove();
				continue;
			}
			double score = 1 - m.look(t.x,t.y) / unprobed;
			for(;x < t.x + 1 && x < width; x++) {
				for(;y < t.y + 1 && y < height; y++) {
					if(m.look(x,y) == Map.UNPROBED){
						System.out.println("Voting on x: " + x + " y: " + y );
						board[x][y].Vote(score);
					}
				}
			}
		}
	}

	Tile ChooseBest() {
		Tile out = voteFringe.get(0);
		for(Tile t: voteFringe) {
			if(out.getScore() < t.getScore()) {
				out = t;
			}
		}
		return out;
	}

	/**
	 * Invoke the Vote Strategy.
	 * @see Strategy
	 */
	public void play(Map m) {
		initialize(m);
		ChooseRandom(m);
		while(!m.done()) {
			VotePhase(m);
			Tile guess = ChooseBest();
			System.out.println(guess.getScore());
			if(guess.getScore() < 0.2) {
				ChooseRandom(m);
			} else {
				//Remove from votefringe
				voteFringe.remove(guess);
				Reveal(guess.x, guess.y, m);
			}
		}
		for(int y = height -1 ; y >= 0; y --) {
			for(int x = 0; x < width; x ++) {
				if(voteFringe.contains(board[x][y])) {
					System.out.print(board[x][y].getScore() + " ");
				} else if (fringe.contains(board[x][y])) {
					System.out.print("! ");
				} else {
					System.out.print("? ");
				}
			}
			System.out.println();
		}
	}

	class Tile {
		int x;
		int y;
		private int votes;
		private double score;
		Tile(int xPos, int yPos) {
			x = xPos;
			y = yPos;
			votes = 0;
			score = 0;
		}

		void Vote(double vote) {
			votes++;
			score += vote;
		}
		
		public double getScore() {
			return score/votes;
		}

		void Reset() {
			votes = 1;
			score = 0;
		}
	}
}
