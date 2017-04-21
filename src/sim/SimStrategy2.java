package sim;

/**
 * This is the Voting Strategy implementation.
 * @author Jacques Breaux
 */


import java.util.ArrayList;
import java.util.Iterator;
import map.Strategy;
import map.Map;

public final class SimStrategy2 implements Strategy {

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

	Tile ChooseBest(Map m) {
		if(voteFringe.size() == 0) {
			return null;
		}
		Iterator<Tile> iFringe = fringe.iterator();
		int unprobed = 0;
		Tile out = null;
		while (iFringe.hasNext()) {
			Tile t = iFringe.next();
			unprobed = 0;
			int value = m.look(t.x,t.y);
			for(int i = t.x - 1;i <= t.x + 1; i++) {
				for(int j = t.y - 1;j <= t.y + 1; j++) {
					if(m.look(i,j) == Map.UNPROBED){
						unprobed++;//counting unprobed neighbors
					} 
					if(m.look(i,j) == Map.MARKED) {
						value--;
					}
				}
			}
			t.value = value;//update the value so that i can choose the best
		}
		int best = 100;
		for (Tile t: voteFringe) {
			int sum = 0;
			for(int i = t.x - 1;i <= t.x + 1; i++) {
				for(int j = t.y - 1;j <= t.y + 1; j++) {
					int cur = m.look(i,j);
					if(cur > 0) {
						sum += cur;
					}
					if(cur == 0) {
						return t;
					}
				}
			}
			if (sum < best) {
				best = sum;
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
					} 
					if(m.look(i,j) == Map.MARKED) {
						value--;
					}
				}
			}
			
			if(value <= -1) { //It can't be negative, so set cost to a high value
				return 1000000;
			} else if(unprobed < value) { //Impossible solution, so set cost to a high value
				return 1000000;
			}
			out += value;
		}
		return out;
	}


	/*
	 * Add for back tracking
	 */
	SimulationResults Simulate(Map m, SimulationResults current, int depth, int score) {
		current.best = ChooseBest(m);
		current.score = sumFringe(m) + score;
		if(depth <= 0 || voteFringe.size() - current.marked.size() < 2) { //stop marking if we're down to 1 tile
			return current;
		}
		SimulationResults best = current.Clone();
		for(int i = 0; i < voteFringe.size(); i++) {
			Tile t = voteFringe.get(i);
			Map clonedMap = m.Clone();
			clonedMap.mark(t.x,t.y);
			SimulationResults child = current.Clone();
			child.marked.add(t);
			child.bombs = current.bombs + 1;
			child.score = sumFringe(clonedMap) + score;
			if(child.score <= best.score) {//only investigate score that are lower by marking that
				SimulationResults next = Simulate(clonedMap, child, depth - 1, score);
				if(next.score < best.score || (best.score == next.score && best.bombs > next.bombs)) {
					best = next;
				}
			}
		}
		return best;
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
				SimulationResults out = Simulate(m, new SimulationResults(), 4, 0);
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
		int value;
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
		int bombs;
		ArrayList<Tile> marked;
		Tile best;
		SimulationResults() {
			score = 100000;
			bombs = 0;
			best = null;
			marked = new ArrayList<Tile>();
		}
		SimulationResults Clone() {
			SimulationResults out = new SimulationResults();
			out.score = this.score;
			out.best = this.best;
			out.marked = (ArrayList<Tile>)this.marked.clone();
			return out;
		}
	}
}
