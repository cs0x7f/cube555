package cs.cube555;

import static cs.cube555.Util.*;

class Phase4Search extends PhaseSearch {
	static int[] VALID_MOVES = new int[] {
	    Ux1, Ux2, Ux3, Rx2, Fx2, Dx1, Dx2, Dx3, Lx2, Bx2,
	    ux2, rx2, fx2, dx2, lx2, bx2
	};

	static long[] SKIP_MOVES = genSkipMoves(VALID_MOVES);

	static int[][] MEdgeMove = new int[70][VALID_MOVES.length];
	static int[][] HEdgeMove = new int[70 * 1680][VALID_MOVES.length];
	static int[][] LEdgeMove = new int[70 * 1680][VALID_MOVES.length];

	static void initEdgeMove() {
		Phase4Edge edge = new Phase4Edge();
		for (int mEdge = 0; mEdge < 70; mEdge++) {
			for (int i = 0; i < 1680; i++) {
				for (int m = 0; m < VALID_MOVES.length; m++) {
					edge.setMEdge(mEdge);
					edge.setHEdge(i);
					edge.setLEdge(i);
					edge.doMove(m);
					MEdgeMove[mEdge][m] = edge.getMEdge();
					HEdgeMove[mEdge * 1680 + i][m] = edge.getHEdge();
					LEdgeMove[mEdge * 1680 + i][m] = edge.getLEdge();
				}
			}
		}
	}

	static PruningTable EdgePrun;

	static void initEdgePrun() {
		EdgePrun = new PruningTable(new Coord() {
			{
				N_IDX = 70 * 1680 * 1680;
				N_MOVES = VALID_MOVES.length;
			}
			int mEdge = 0;
			int lEdge = 0;
			int hEdge = 0;
			void set(int i) {
				mEdge = i % 70;
				lEdge = i / 70 % 1680;
				hEdge = i / 70 / 1680;
			}
			int getMoved(int move) {
				return (HEdgeMove[mEdge * 1680 + hEdge][move] * 1680 + LEdgeMove[mEdge * 1680 + lEdge][move]) * 70 + MEdgeMove[mEdge][move];
			}
		}, null, "Phase4Edge");
	}

	static int[] RLCenter2Half = new int[2450];
	static int[] Half2RLCenter = new int[216];
	static int[][] UDCenterMove = new int[4900][VALID_MOVES.length];
	static int[][] RLCenterMove = new int[216][VALID_MOVES.length];

	static void initCenterMove() {
		Phase4Center center = new Phase4Center();
		for (int i = 0; i < RLCenter2Half.length; i++) {
			RLCenter2Half[i] = -1;
		}
		int tail = 0;
		Half2RLCenter[0] = 0;
		RLCenter2Half[0] = tail++;
		while (tail < 216) {
			for (int i = 0; i < RLCenter2Half.length; i++) {
				if (RLCenter2Half[i] == -1) {
					continue;
				}
				for (int m = 10; m < VALID_MOVES.length; m++) {
					center.setRLCenter(i);
					center.doMove(m);
					int idx = center.getRLCenter();
					if (RLCenter2Half[idx] == -1) {
						Half2RLCenter[tail] = idx;
						RLCenter2Half[idx] = tail++;
					}
				}
			}
		}
		for (int m = 0; m < VALID_MOVES.length; m++) {
			for (int i = 0; i < 4900; i++) {
				center.setUDCenter(i);
				center.doMove(m);
				UDCenterMove[i][m] = center.getUDCenter();
			}
			for (int i = 0; i < 216; i++) {
				center.setRLCenter(Half2RLCenter[i]);
				center.doMove(m);
				RLCenterMove[i][m] = RLCenter2Half[center.getRLCenter()];
			}
		}
	}

	static PruningTable CenterPrun;

	static void initCenterPrun() {
		int[] UDSOLVED = new int[] {0, 1895, 1967, 2905, 2977, 4876};
		int[] RLSOLVED = new int[UDSOLVED.length / 2];
		for (int i = 0; i < UDSOLVED.length / 2; i++) {
			RLSOLVED[i] = RLCenter2Half[UDSOLVED[i]];
		}
		CenterPrun = new PruningTable(RLCenterMove, UDCenterMove, RLSOLVED, UDSOLVED, "Phase4Center");
		MLEdgeRLCenterPrun = new PruningTable(new Coord() {
			{
				N_IDX = 70 * 1680 * 216;
				N_MOVES = VALID_MOVES.length;
			}
			int rlCenter = 0;
			int mEdge = 0;
			int lEdge = 0;
			void set(int i) {
				rlCenter = i % 216;
				mEdge = i / 216 % 70;
				lEdge = i / 216 / 70;
			}
			int getMoved(int move) {
				return (LEdgeMove[mEdge * 1680 + lEdge][move] * 70 + MEdgeMove[mEdge][move]) * 216 + RLCenterMove[rlCenter][move];
			}
		}, RLSOLVED, "MLEdgeRLCenter");
		MHEdgeRLCenterPrun = new PruningTable(new Coord() {
			{
				N_IDX = 70 * 1680 * 216;
				N_MOVES = VALID_MOVES.length;
			}
			int rlCenter = 0;
			int mEdge = 0;
			int hEdge = 0;
			void set(int i) {
				rlCenter = i % 216;
				mEdge = i / 216 % 70;
				hEdge = i / 216 / 70;
			}
			int getMoved(int move) {
				return (HEdgeMove[mEdge * 1680 + hEdge][move] * 70 + MEdgeMove[mEdge][move]) * 216 + RLCenterMove[rlCenter][move];
			}
		}, RLSOLVED, "MHEdgeRLCenter");

		MHEdgeUDCenterPrun = new PruningTable(new Coord() {
			{
				N_IDX = 70 * 1680 * 4900;
				N_MOVES = VALID_MOVES.length;
			}
			int udCenter = 0;
			int mEdge = 0;
			int hEdge = 0;
			void set(int i) {
				udCenter = i % 4900;
				mEdge = i / 4900 % 70;
				hEdge = i / 4900 / 70;
			}
			int getMoved(int move) {
				return (HEdgeMove[mEdge * 1680 + hEdge][move] * 70 + MEdgeMove[mEdge][move]) * 4900 + UDCenterMove[udCenter][move];
			}
		}, UDSOLVED, "MHEdgeUDCenter");

		MLEdgeUDCenterPrun = new PruningTable(new Coord() {
			{
				N_IDX = 70 * 1680 * 4900;
				N_MOVES = VALID_MOVES.length;
			}
			int udCenter = 0;
			int mEdge = 0;
			int lEdge = 0;
			void set(int i) {
				udCenter = i % 4900;
				mEdge = i / 4900 % 70;
				lEdge = i / 4900 / 70;
			}
			int getMoved(int move) {
				return (LEdgeMove[mEdge * 1680 + lEdge][move] * 70 + MEdgeMove[mEdge][move]) * 4900 + UDCenterMove[udCenter][move];
			}
		}, UDSOLVED, "MLEdgeUDCenter");

	}

	static PruningTable MLEdgeRLCenterPrun;
	static PruningTable MHEdgeRLCenterPrun;
	static PruningTable MLEdgeUDCenterPrun;
	static PruningTable MHEdgeUDCenterPrun;

	static class Phase4Node extends Node {
		int rlCenter;
		int udCenter;
		int mEdge;
		int lEdge;
		int hEdge;
		int getPrun() {
			int prun = CenterPrun.getPrun(rlCenter, udCenter);
			prun = Math.max(prun, EdgePrun.getPrun((hEdge * 1680 + lEdge) * 70 + mEdge));
			prun = Math.max(prun, MLEdgeRLCenterPrun.getPrun((lEdge * 70 + mEdge) * 216 + rlCenter));
			prun = Math.max(prun, MHEdgeRLCenterPrun.getPrun((hEdge * 70 + mEdge) * 216 + rlCenter));
			prun = Math.max(prun, MLEdgeUDCenterPrun.getPrun((lEdge * 70 + mEdge) * 4900 + udCenter));
			return Math.max(prun, MHEdgeUDCenterPrun.getPrun((hEdge * 70 + mEdge) * 4900 + udCenter));
		}
		boolean isSolved() {
			return mEdge == 0 && lEdge == 0 && hEdge == 0;
		}
		int doMovePrun(Node node0, int move, int maxl) {
			Phase4Node node = (Phase4Node) node0;
			rlCenter = RLCenterMove[node.rlCenter][move];
			udCenter = UDCenterMove[node.udCenter][move];
			mEdge = MEdgeMove[node.mEdge][move];
			lEdge = LEdgeMove[node.mEdge * 1680 + node.lEdge][move];
			hEdge = HEdgeMove[node.mEdge * 1680 + node.hEdge][move];
			if (maxl <= MLEdgeUDCenterPrun.getPrun((lEdge * 70 + mEdge) * 4900 + udCenter)) {
				return maxl;
			}
			if (maxl <= MHEdgeUDCenterPrun.getPrun((hEdge * 70 + mEdge) * 4900 + udCenter)) {
				return maxl;
			}
			if (maxl <= MLEdgeRLCenterPrun.getPrun((lEdge * 70 + mEdge) * 216 + rlCenter)) {
				return maxl;
			}
			if (maxl <= MHEdgeRLCenterPrun.getPrun((hEdge * 70 + mEdge) * 216 + rlCenter)) {
				return maxl;
			}
			if (maxl <= CenterPrun.getPrun(rlCenter, udCenter)) {
				return maxl;
			}
			if (maxl <= EdgePrun.getPrun((hEdge * 1680 + lEdge) * 70 + mEdge)) {
				return maxl;
			}
			return getPrun();
		}
	}

	Phase4Search() {
		super.VALID_MOVES = VALID_MOVES;
		for (int i = 0; i < searchNode.length; i++) {
			searchNode[i] = new Phase4Node();
		}
	}

	Node[] initFrom(CubieCube cc) {
		Phase4Edge edge = new Phase4Edge();
		Phase4Center center = new Phase4Center();

		for (int i = 0; i < 8; i++) {
			center.udxCenter[i] = cc.xCenter[i] == 0 ? 0 : -1;
			center.udtCenter[i] = cc.tCenter[i] == 0 ? 0 : -1;
			center.rlxCenter[i] = cc.xCenter[16 + (i & 4) + (i + 1) % 4] == 1 ? 0 : -1;
			center.rltCenter[i] = cc.tCenter[16 + (i & 4) + (i + 1) % 4] == 1 ? 0 : -1;
		}
		int rlCenter = RLCenter2Half[center.getRLCenter()];
		int udCenter = center.getUDCenter();

		int maskY = 0;
		for (int i = 0; i < 4; i++) {
			maskY |= 1 << (cc.wEdge[8 + i] % 12);
			maskY |= 1 << (cc.wEdge[8 + i + 12] % 12);
			maskY |= 1 << (cc.mEdge[8 + i] >> 1);
		}
		maskY ^= 0xfff;
		int bitCnt = Integer.bitCount(maskY);

		Node[] nodes = new Node[Cnk[bitCnt][4]];
		// System.out.println(nodes.length);
		int idx = 0;
		for (int mask = maskY; mask != 0; mask = mask - 1 & maskY) {
			if (Integer.bitCount(mask) != 4) {
				continue;
			}
			for (int i = 0; i < 8; i++) {
				int e = cc.mEdge[i] >> 1;
				edge.mEdge[i] = (mask >> e & 1) == 0 ? -1 : Integer.bitCount(mask & ((1 << e) - 1));
				e = cc.wEdge[i] % 12;
				edge.lEdge[i] = (mask >> e & 1) == 0 ? -1 : Integer.bitCount(mask & ((1 << e) - 1));
				e = cc.wEdge[i + 12] % 12;
				edge.hEdge[i] = (mask >> e & 1) == 0 ? -1 : Integer.bitCount(mask & ((1 << e) - 1));
			}
			edge.isStd = false;
			Phase4Node node = new Phase4Node();
			node.mEdge = edge.getMEdge();
			node.lEdge = edge.getLEdge();
			node.hEdge = edge.getHEdge();
			node.rlCenter = rlCenter;
			node.udCenter = udCenter;
			nodes[idx] = node;
			idx++;
		}

		return nodes;
	}
}