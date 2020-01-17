package cs.cube555;

import static cs.cube555.Util.*;

class Phase3Search extends PhaseSearch {

	static int[] VALID_MOVES = new int[] {
	    Ux1, Ux2, Ux3, Rx1, Rx2, Rx3, Fx1, Fx2, Fx3, Dx1, Dx2, Dx3, Lx1, Lx2, Lx3, Bx1, Bx2, Bx3,
	    ux2, rx2, fx2, dx2, lx2, bx2
	};

	static int[][] SymMove;

	static long[] SKIP_MOVES = genSkipMoves(VALID_MOVES);
	static int NEXT_AXIS = 0x12492;

	static int[][] CenterMove;
	static int[][] MEdgeMove;
	static int[][] MEdgeConj;
	static PruningTable CenterMEdgePrun;

	static int[][] WEdgeSymMove;
	static int[] WEdgeSym2Raw;
	static int[] WEdgeSelfSym;
	static int[] WEdgeRaw2Sym;

	static void initWEdgeSymMove() {
		Phase3Edge edge = new Phase3Edge();
		int symCnt = 0;
		WEdgeSym2Raw = new int[170971];
		WEdgeSelfSym = new int[170971];
		WEdgeRaw2Sym = new int[2704156];
		for (int i = 0; i < WEdgeRaw2Sym.length; i++) {
			if (WEdgeRaw2Sym[i] != 0) {
				continue;
			}
			edge.setWEdge(i);
			for (int sym = 0; sym < 16; sym++) {
				int idx = edge.getWEdge();
				WEdgeRaw2Sym[idx] = symCnt << 4 | sym;
				if (idx == i) {
					WEdgeSelfSym[symCnt] |= 1 << sym;
				}
				edge.doConj(0);
				if ((sym & 3) == 3) {
					edge.doConj(1);
				}
				if ((sym & 7) == 7) {
					edge.doConj(2);
				}
			}
			WEdgeSym2Raw[symCnt] = i;
			symCnt++;
		}
		WEdgeSymMove = new int[symCnt][VALID_MOVES.length];
		for (int i = 0; i < symCnt; i++) {
			for (int m = 0; m < VALID_MOVES.length; m++) {
				edge.setWEdge(WEdgeSym2Raw[i]);
				edge.doMove(m);
				WEdgeSymMove[i][m] = WEdgeRaw2Sym[edge.getWEdge()];
			}
		}
	}

	static void initMEdgeMove() {
		Phase3Edge edge = new Phase3Edge();
		MEdgeMove = new int[2048][VALID_MOVES.length];
		MEdgeConj = new int[2048][16];
		for (int i = 0; i < 2048; i++) {
			for (int m = 0; m < VALID_MOVES.length; m++) {
				edge.setMEdge(i);
				edge.doMove(m);
				MEdgeMove[i][m] = edge.getMEdge();
			}

			edge.setMEdge(i);
			for (int sym = 0; sym < 16; sym++) {
				MEdgeConj[i][CubieCube.SymMultInv[0][sym]] = edge.getMEdge();
				edge.doConj(0);
				if ((sym & 3) == 3) {
					edge.doConj(1);
				}
				if ((sym & 7) == 7) {
					edge.doConj(2);
				}
			}
		}
	}

	static void initCenterMove() {
		CenterMove = new int[1225][VALID_MOVES.length];
		Phase3Center center = new Phase3Center();
		for (int i = 0; i < 1225; i++) {
			for (int m = 0; m < VALID_MOVES.length; m++) {
				center.setCenter(i);
				center.doMove(m);
				CenterMove[i][m] = center.getCenter();
			}
		}
	}

	static PruningTable WEdgeSymPrun;
	static PruningTable WMEdgeSymPrun;

	static void initPrun() {
		CenterMEdgePrun = new PruningTable(CenterMove, MEdgeMove, Phase3Center.SOLVED_CENTER, null, "Phase3CenterMEdge");

		WEdgeSymPrun = new PruningTable(new SymCoord() {
			{
				N_IDX = 170971;
				N_MOVES = VALID_MOVES.length;
				N_SYM = 16;
				SelfSym = WEdgeSelfSym;
			}
			int getMoved(int move) {
				return WEdgeSymMove[idx][move];
			}
		}, null, "Phase3WEdgeSym");

		WMEdgeSymPrun = new PruningTable(new SymCoord() {
			{
				N_IDX = 170971;
				N_MOVES = VALID_MOVES.length;
				N_SYM = 16;
				SelfSym = WEdgeSelfSym;
			}
			int getMoved(int move) {
				return WEdgeSymMove[idx][move];
			}
		}, new RawCoord() {
			{
				N_IDX = 2048;
			}
			int getMoved(int move) {
				return MEdgeMove[idx][move];
			}
			int getConj(int idx, int conj) {
				return MEdgeConj[idx][conj];
			}
		}, null, "Phase3MWEdgeSym");
	}

	static class Phase3Node extends Node {
		int center;
		int mEdge;
		int wEdge;
		int getPrun() {
			return Math.max(CenterMEdgePrun.getPrun(center, mEdge),
			                WMEdgeSymPrun.getPrun(wEdge >> 4, MEdgeConj[mEdge][wEdge & 0xf]));
		}
		boolean isSolved(int maxl) {
			return maxl == 0 && mEdge == 0 && (wEdge >> 4) == 0;
		}
		int doMovePrun(Node node0, int move, int maxl) {
			Phase3Node node = (Phase3Node) node0;
			center = CenterMove[node.center][move];
			mEdge = MEdgeMove[node.mEdge][move];
			wEdge = WEdgeSymMove[node.wEdge >> 4][SymMove[node.wEdge & 0xf][move]];
			wEdge = wEdge & ~0xf | CubieCube.SymMult[wEdge & 0xf][node.wEdge & 0xf];
			return getPrun();
		}
	}

	Phase3Search() {
		super.VALID_MOVES = VALID_MOVES;
		for (int i = 0; i < searchNode.length; i++) {
			searchNode[i] = new Phase3Node();
		}
	}

	Node[] initFrom(CubieCube cc) {
		if (SymMove == null) {
			SymMove = CubieCube.getSymMove(VALID_MOVES, 16);
		}
		Phase3Center ct = new Phase3Center();
		Phase3Edge ed = new Phase3Edge();
		for (int i = 0; i < 8; i++) {
			ct.xCenter[i] = cc.xCenter[16 + (i & 4) + (i + 1) % 4] == 1 ? 0 : -1;
			ct.tCenter[i] = cc.tCenter[16 + (i & 4) + (i + 1) % 4] == 1 ? 0 : -1;
		}
		int center = ct.getCenter();
		Node[] nodes = new Node[2048];
		for (int idx = 0; idx < 2048; idx++) {
			int flip = idx << 1 | (Integer.bitCount(idx) & 1);
			for (int i = 0; i < 24; i++) {
				ed.wEdge[i] = cc.wEdge[i] < 12 ? 0 : -1;
				if ((flip >> (cc.wEdge[i] % 12) & 1) != 0) {
					ed.wEdge[i] = -1 - ed.wEdge[i];
				}
			}
			for (int i = 0; i < 12; i++) {
				ed.mEdge[i] = cc.mEdge[i] & 1;
				ed.mEdge[i] ^= flip >> (cc.mEdge[i] >> 1) & 1;
			}
			Phase3Node node = new Phase3Node();
			node.mEdge = ed.getMEdge();
			node.wEdge = WEdgeRaw2Sym[ed.getWEdge()];
			node.center = center;
			nodes[idx] = node;
		}
		return nodes;
	}

	public static void main(String[] args) {
		initMEdgeMove();
		initCenterMove();
		initWEdgeSymMove();
		initPrun();
	}
}