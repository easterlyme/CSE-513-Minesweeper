package vote;

/**
 * This is the Voting Strategy implementation.
 * @author Jacques Breaux
 */


import java.util.ArrayList;
import java.util.PriorityQueue;
import map.Strategy;
import map.Map;

public final class VoteStrategy implements Strategy {

	private ArrayList<Voter> fringe;
	private Voter[] voters;
	private int width;
	private int height;

	void initialize(Map m) {
		fringe = new ArrayList<Voter>();//initialize an empty fringe
		width = m.columns();
		height = m.rows();
		voters = new Voter[width * height];
		for (int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				voters[j * width + i] = new Voter(i,j);
			}
		}
	}

	void VotePhase() {
		for(Voter v: fringe) {
			v.Cast();
		}
	}

	/**
	 * @return false when unable to choose
	 */
	boolean ChoosePhase(Map m) {
		return false;
	}

	boolean ChooseRandom(Map m) {
		int x = m.pick(width);
		int y = m.pick(height);
		int q = m.probe(x,y);
		fringe.add(voters[x * width + y]);
		return true;
	}

	/**
	 * Invoke the Vote Strategy.
	 * @see Strategy
	 */
	public void play(Map m) {
		initialize(m);
		ChooseRandom(m);
		while(!m.done()) { //until the game is over
			VotePhase();
			if(!ChoosePhase(m)) { //Failure to Choose, implement guess strategy
				ChooseRandom(m);
			}
		}
	}
}
