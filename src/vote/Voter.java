package vote;

import map.Map;
import java.util.ArrayList;

class Voter {
	private ArrayList<Voter> neighbors;
	private int x;
	private int y;
	private int count;

	private int score;
	private Map myMap;

	Voter(int xPos, int yPos, Map m) {
		neighbors = new ArrayList<Voter>();
		x = xPos;
		y = yPos;
		count = -1;
		myMap = m;
	}



	public int X() {
		return x;
	}

	public int Y() {
		return y;
	}

	/**
	 * @return if true, it is done voting, it assumes all around it bombs
	 */
	public boolean Cast() {
		int ballot = (int)((count / (float)neighbors.size()) * 100);
		if(ballot % 100 != 0) {
			for(Voter v: neighbors) {
				v.VoteUncertainty(ballot);
			}
		} else {
			for(Voter v: neighbors) {
				myMap.mark(v.X(), v.Y());
			}
			return true;
		}
		return false;
	}

	public void VoteUncertainty(int vote) {
		if (vote == 100 || score == 800) {//Certainly a Bomb
			score = 800;
		} else if (vote == 0) {//Certainly not a Bomb
			score = -1;
		}
		else if(score >= 0) { //only vote if not set to safe
			score += vote;
		}
	}

	public void Notify() { //Notify neighbors of removal
		for(Voter v: neighbors) {
			v.Remove(this);
		}
	}

	public void Remove(Voter v) {
		neighbors.remove(v);
	}
}
