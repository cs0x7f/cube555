package cs.cube555;

import static cs.cube555.Util.*;

class PhaseSearch {
	SolutionChecker callback = null;
	int[] solution = new int[255];
	int ccidx;

	void solve(CubieCube[] cc, SolutionChecker callback) {
		if (SKIP_MOVES == null) {
			SKIP_MOVES = genSkipMoves(VALID_MOVES);
		}
		this.callback = callback;
		Node[][] nodes = new Node[cc.length][];
		int[][] prun = new int[cc.length][];
		for (ccidx = 0; ccidx < cc.length; ccidx++) {
			nodes[ccidx] = initFrom(cc[ccidx]);
			prun[ccidx] = new int[nodes[ccidx].length];
			for (int i = 0; i < nodes[ccidx].length; i++) {
				prun[ccidx][i] = nodes[ccidx][i].getPrun();
			}
		}
		out: for (int maxl = 0; maxl < 30; maxl++) {
			for (ccidx = 0; ccidx < cc.length; ccidx++) {
				for (int i = 0; i < nodes[ccidx].length; i++) {
					if (maxl >= prun[ccidx][i] &&
					        idaSearch(nodes[ccidx][i], 0, maxl, VALID_MOVES.length) == 0) {
						break out;
					}
				}
			}
		}
	}

	Node[] initFrom(CubieCube cc) {
		return null;
	}

	abstract static class Node {
		abstract int doMovePrun(Node node, int move, int maxl);
		abstract boolean isSolved(int maxl);
		abstract int getPrun();
	}

	Node[] searchNode = new Node[30];

	private int idaSearch(Node node, int depth, int maxl, int lm) {
		if (node.isSolved(maxl)) {
			return callback.check(solution, depth, ccidx);
		}
		long skipMoves = SKIP_MOVES[lm];
		for (int move = 0; move < VALID_MOVES.length; move++) {
			if ((skipMoves >> move & 1) != 0) {
				continue;
			}
			int prun = searchNode[depth].doMovePrun(node, move, maxl);
			if (prun >= maxl) {
				continue;
			}
			solution[depth] = VALID_MOVES[move];
			int ret = idaSearch(searchNode[depth], depth + 1, maxl - 1, move);
			if (ret == 0) {
				return 0;
			}
		}
		return 1;
	}

	long[] SKIP_MOVES;
	int[] VALID_MOVES;
}