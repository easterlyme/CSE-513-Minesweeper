package sim;

/**
 * This is the Voting Strategy implementation.
 * @author Jacques Breaux
 */


import java.util.ArrayList;
import java.util.Iterator;
import map.Strategy;
import map.Map;

public final class SimStrategy implements Strategy {

	private int width;
	private int height;

	private ArrayList<Tile> fringe;
	private ArrayList<Tile> voteFringe;
	private Tile[][] board;
	private int revealed = 0;
	private int total = 0;

	void initialize(Map m) {
		width = m.columns();
		height = m.rows();
		total = width * height;
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
/*
		while(voteFringe.contains(board[x][y]) && total - revealed != voteFringe.size()) {
			x = m.pick(width);
			y = m.pick(height);
		}
*/
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
		revealed++;
		voteFringe.remove(board[x][y]);
		fringe.add(board[x][y]);
		int q = m.probe(x,y);
		if(q == Map.BOOM)
			return;
		if(q == 0) {
			RevealSurrounding(x,y,m);
		} else {
			for(int i = x - 1; i <= x + 1; i++) {
				for(int j = y - 1; j <= y+1; j++) {
					if(m.look(i,j) == Map.UNPROBED && !voteFringe.contains(board[i][j])) {
						voteFringe.add(board[i][j]);
					}
				}
			}
		}
	}

	void VotePhase(Map m, boolean markStuff) {
		for(Tile t: voteFringe) {
			t.Reset();//Reset for votes
		}
		Iterator<Tile> iFringe = fringe.iterator();
		int unprobed = 0;
		while (iFringe.hasNext()) {
			Tile t = iFringe.next();
			unprobed = 0;
			int value = m.look(t.x,t.y);
			for(int i = t.x - 1;i <= t.x + 1; i++) {
				for(int j = t.y - 1;j <= t.y + 1; j++) {
					if(m.look(i,j) == Map.UNPROBED){
						unprobed++;//counting unprobed neighbors
					} else if(m.look(i,j) == Map.MARKED) {
						value--;
					}
				}
			}
			if(value == 0) { //remove from fringe, it's done
				if(markStuff) {
					iFringe.remove();
				}
				continue;
			}
			double score = 1.0 - (value / (double)unprobed);
			for(int x = t.x - 1;x <= t.x + 1; x++) {
				for(int y = t.y - 1;y <= t.y + 1; y++) {
					if(m.look(x,y) == Map.UNPROBED){
						if(score <= 5e-5 && markStuff) {//around me is bombs
							m.mark(x,y);
							voteFringe.remove(board[x][y]);//Clean up the vote fringe since i marked it as a bomb
						} else {
							board[x][y].Vote(score);
						}
					}
				}
			}
		}
	}

	Tile ChooseBest() {
		if(voteFringe.size() == 0) {
			return null;
		}
		Tile out = voteFringe.get(0);
		for(Tile t: voteFringe) {
			if(out.getScore() < t.getScore()) {
				out = t;
			}
		}
		return out;
	}

	int sumFringe(Map m) {
		Iterator<Tile> iFringe = fringe.iterator();
		int unprobed = 0;
		int out = 0;
		while (iFringe.hasNext()) {
			Tile t = iFringe.next();
			unprobed = 0;
			int value = m.look(t.x,t.y);
			for(int i = t.x - 1;i <= t.x + 1; i++) {
				for(int j = t.y - 1;j <= t.y + 1; j++) {
					if(m.look(i,j) == Map.UNPROBED){
						unprobed++;//counting unprobed neighbors
					} else if(m.look(i,j) == Map.MARKED) {
						value--;
					}
				}
			}
			if(value <= -1) { //It can't be negative, so increase cost a lot
				out += 1000;
			}
			out += value;
		}
		return out;
	}

	/*
	 * This method will "assume" that a tile is a bomb and then work from that assumption
	 */
	SimulationResults Simulate(Map m) {
		SimulationResults out = new SimulationResults();
		Map clone = m.Clone();
		Tile best = null;
		Tile marked = null;
		int fringeScore = 1000000;
		int loops = 0;
		while(clone.mines_minus_marks() > 0) {
			marked = null;
			for(Tile t: voteFringe) {
				clone.mark(t.x, t.y); //Mark this tile
				VotePhase(clone, false);//Go through the vote
				int newScore = sumFringe(clone);
				clone.unmark(t.x,t.y);
				Tile next = ChooseBest();
				if(fringeScore > newScore) {
					fringeScore = newScore;//Minimization of edge bombs
					marked = t.Clone();
					best = next.Clone();
				}
			}
			if(marked == null) {
				break;//Nothing new to mark
			}
			if(! out.marked.contains(marked)) {
				out.marked.add(marked);
				clone.mark(marked.x, marked.y);//Mark the best and continue to simulate
			}
		}
		out.score = fringeScore;
		out.best = best;
		return out;
	}

	/**
	 * Invoke the Precognition Vote Strategy.
	 * @see Strategy
	 */
	public void play(Map m) {
		initialize(m);
		ChooseRandom(m);
		while(!m.done()) {
			if(voteFringe.size() > 1) {
				SimulationResults out = Simulate(m);
				Tile chosen = out.best;
				Reveal(chosen.x, chosen.y,m);
			} else {
				ChooseRandom(m);
			}
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
			this.votes += 1;
			if(vote < 1e-2) {
				this.votes += 2;
			}
			score += vote;
		}
		
		public double getScore() {
			return score/votes;
		}

		void Reset() {
			votes = 0;
			score = 0;
		}

		Tile Clone() {
			Tile out = new Tile(this.x, this.y);
			out.votes = this.votes;
			out.score = this.score;
			return out;
		}
	}

	private class SimulationResults {
		int score;
		ArrayList<Tile> marked;
		Tile best;
		SimulationResults() {
			score = 0;
			best = null;
			marked = new ArrayList<Tile>();
		}
	}
}
