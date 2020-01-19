package cs.cube555;

import static cs.cube555.Util.*;

class Phase5Search extends PhaseSearch {
	static int[] VALID_MOVES = new int[] {
	    Ux1, Ux2, Ux3, Rx2, Fx2, Dx1, Dx2, Dx3, Lx2, Bx2,
	    rx2, fx2, lx2, bx2
	};

	static long[] SKIP_MOVES = genSkipMoves(VALID_MOVES);

	static int[][] HEdgeMove;
	static int[][] LEdgeMove;
	static PruningTable EdgePrun;

	static void initEdgeMove() {
		HEdgeMove = new int[40320][VALID_MOVES.length];
		LEdgeMove = new int[40320][VALID_MOVES.length];
		Phase5Edge edge = new Phase5Edge();
		for (int i = 0; i < 40320; i++) {
			for (int m = 0; m < VALID_MOVES.length; m++) {
				edge.setLEdge(i);
				edge.setHEdge(i);
				edge.doMove(m);
				LEdgeMove[i][m] = edge.getLEdge();
				HEdgeMove[i][m] = edge.getHEdge();
			}
		}
	}

	static void initEdgePrun() {
		int[] Parity = new int[40320];
		for (int i = 0; i < 40320; i++) {
			Parity[i] = getParity(i, 8);
		}

		EdgePrun = new PruningTable(new Coord() {
			{
				N_IDX = 40320 * 20160;
				N_MOVES = VALID_MOVES.length;
			}
			int lEdge = 0;
			int hEdge = 0;
			void set(int i) {
				lEdge = i % 20160 << 1;
				hEdge = i / 20160;
				if (Parity[lEdge] != Parity[hEdge]) {
					lEdge ^= 1;
				}
			}
			int getMoved(int move) {
				return HEdgeMove[hEdge][move] * 20160 + (LEdgeMove[lEdge][move] >> 1);
			}
		}, null, "Phase5Edge");
	}

	static int[][] UDCenterMove;
	static int[][] CenterMove;
	static PruningTable CenterPrun;

	static void initCenterMove() {
		int[][] RFLBMove = new int[36][VALID_MOVES.length];
		int[][] TMove = new int[70][VALID_MOVES.length];
		int[][] XMove = new int[70][VALID_MOVES.length];
		CenterMove = new int[70 * 70 * 36][VALID_MOVES.length];
		Phase5Center center = new Phase5Center();
		for (int i = 0; i < 70; i++) {
			for (int m = 0; m < VALID_MOVES.length; m++) {
				center.setTCenter(i);
				center.setXCenter(i);
				center.doMove(m);
				TMove[i][m] = center.getTCenter();
				XMove[i][m] = center.getXCenter();
			}
		}
		for (int i = 0; i < 36; i++) {
			for (int m = 0; m < VALID_MOVES.length; m++) {
				center.setRFLBCenter(i);
				center.doMove(m);
				RFLBMove[i][m] = center.getRFLBCenter();
			}
		}
		for (int i = 0; i < 70 * 70 * 36; i++) {
			int tCenter = i % 70;
			int xCenter = i / 70 % 70;
			int rflbCenter = i / 70 / 70;
			for (int m = 0; m < VALID_MOVES.length; m++) {
				CenterMove[i][m] = (RFLBMove[rflbCenter][m] * 70 + XMove[xCenter][m]) * 70 + TMove[tCenter][m];
			}
		}
	}

	static PruningTable LEdgeCenterPrun;
	static PruningTable HEdgeCenterPrun;

	static void initPrun() {
		int[][] UDCenterMove = new int[4900][VALID_MOVES.length];
		for (int i = 0; i < 4900; i++) {
			for (int j = 0; j < VALID_MOVES.length; j++) {
				UDCenterMove[i][j] = CenterMove[i][j] % 4900;
			}
		}
		CenterPrun = new PruningTable(CenterMove, null, "Phase5Center");
		LEdgeCenterPrun = new PruningTable(LEdgeMove, UDCenterMove, null, null, "Phase5LEdgeCenter");
		HEdgeCenterPrun = new PruningTable(HEdgeMove, UDCenterMove, null, null, "Phase5HEdgeCenter");
	}

	static class Phase5Node extends Node {
		int lEdge;
		int hEdge;
		int center;
		int getPrun() {
			return Math.max(
			           CenterPrun.getPrun(center),
			           Math.max(LEdgeCenterPrun.getPrun(lEdge, center % 4900),
			                    HEdgeCenterPrun.getPrun(hEdge, center % 4900))
			       );
		}
		int doMovePrun(Node node0, int move, int maxl) {
			Phase5Node node = (Phase5Node) node0;
			center = CenterMove[node.center][move];
			lEdge = LEdgeMove[node.lEdge][move];
			hEdge = HEdgeMove[node.hEdge][move];
			return getPrun();
		}
	}

	Phase5Search() {
		super.VALID_MOVES = VALID_MOVES;
		for (int i = 0; i < searchNode.length; i++) {
			searchNode[i] = new Phase5Node();
		}
	}

	Node[] initFrom(CubieCube cc) {
		Phase5Edge edge = new Phase5Edge();
		Phase5Center center = new Phase5Center();
		int mask = 0;
		for (int i = 0; i < 8; i++) {
			mask |= 1 << (cc.mEdge[i] >> 1);
		}
		for (int i = 0; i < 8; i++) {
			int e = cc.mEdge[i] >> 1;
			edge.mEdge[i] = Integer.bitCount(mask & ((1 << e) - 1));
			e = cc.wEdge[i] % 12;
			edge.lEdge[i] = Integer.bitCount(mask & ((1 << e) - 1));
			e = cc.wEdge[i + 12] % 12;
			edge.hEdge[i] = Integer.bitCount(mask & ((1 << e) - 1));
			center.xCenter[i] = cc.xCenter[i] == 0 ? 0 : -1;
			center.tCenter[i] = cc.tCenter[i] == 0 ? 0 : -1;
			center.rflbCenter[i] = cc.tCenter[9 + i * 2] == 1 || cc.tCenter[9 + i * 2] == 2 ? 0 : -1;
		}
		edge.isStd = false;
		Phase5Node node = new Phase5Node();
		node.lEdge = edge.getLEdge();
		node.hEdge = edge.getHEdge();
		node.center = (center.getRFLBCenter() * 70 + center.getXCenter()) * 70 + center.getTCenter();
		return new Node[] {node};
	}
}