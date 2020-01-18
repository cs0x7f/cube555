package cs.cube555;

import static cs.cube555.Util.*;

/*
           0  0  1
           3     1
           3  2  2

20 20 21   8  8  9    16 16 17   12 12  13
23    21   11    9    19    17   15     13
23 22 22   11 10 10   19 18 18   15 14  14

           4  4  5
           7     5
           7  6  6
*/

class Phase1Search extends PhaseSearch {

	static int[] VALID_MOVES = new int[] {
	    Ux1, Ux2, Ux3, Rx1, Rx2, Rx3, Fx1, Fx2, Fx3, Dx1, Dx2, Dx3, Lx1, Lx2, Lx3, Bx1, Bx2, Bx3,
	    ux1, ux2, ux3, rx1, rx2, rx3, fx1, fx2, fx3, dx1, dx2, dx3, lx1, lx2, lx3, bx1, bx2, bx3
	};

	static int[][] TCenterMove;
	static int[][] XCenterMove;

	static PruningTable TCenterPrun;
	static PruningTable XCenterPrun;

	static void initCenterMove() {
		Phase1Center ct = new Phase1Center();
		TCenterMove = new int[735471][VALID_MOVES.length];
		XCenterMove = new int[735471][VALID_MOVES.length];
		for (int i = 0; i < 735471; i++) {
			for (int m = 0; m < VALID_MOVES.length; m++) {
				ct.setTCenter(i);
				ct.setXCenter(i);
				ct.doMove(m);
				TCenterMove[i][m] = ct.getTCenter();
				XCenterMove[i][m] = ct.getXCenter();
			}
		}
	}

	static void initCenterPrun() {
		TCenterPrun = new PruningTable(TCenterMove, null, "Phase1TCenter");
		XCenterPrun = new PruningTable(XCenterMove, null, "Phase1XCenter");
	}

	static class Phase1Node extends Node {
		int tCenter;
		int xCenter;
		int getPrun() {
			return Math.max(TCenterPrun.getPrun(tCenter), XCenterPrun.getPrun(xCenter));
		}
		boolean isSolved() {
			return tCenter == 0 && xCenter == 0;
		}
		int doMovePrun(Node node0, int move, int maxl) {
			Phase1Node node = (Phase1Node) node0;
			tCenter = TCenterMove[node.tCenter][move];
			xCenter = XCenterMove[node.xCenter][move];
			return getPrun();
		}
	}

	Phase1Search() {
		super.VALID_MOVES = VALID_MOVES;
		super.MIN_BACK_DEPTH = 5;
		for (int i = 0; i < searchNode.length; i++) {
			searchNode[i] = new Phase1Node();
		}
	}

	Node[] initFrom(CubieCube cc) {
		Phase1Center ct = new Phase1Center();
		for (int i = 0; i < 24; i++) {
			ct.xCenter[i] = cc.xCenter[i] == 1 || cc.xCenter[i] == 4 ? 0 : -1;
			ct.tCenter[i] = cc.tCenter[i] == 1 || cc.tCenter[i] == 4 ? 0 : -1;
		}
		Phase1Node node = new Phase1Node();
		node.xCenter = ct.getXCenter();
		node.tCenter = ct.getTCenter();
		return new Node[] {node};
	}
}