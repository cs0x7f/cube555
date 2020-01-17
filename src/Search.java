package cs.cube555;

import static cs.cube555.Util.*;

public class Search {
	static long startTime;

	static void printSolution(int[] solution, int cumlen) {
		StringBuffer sb = new StringBuffer();
		for (int move : solution) {
			sb.append(move2str[move]).append(' ');
		}
		sb.append(String.format("(%df, %df, %dms)", solution.length, cumlen, (System.nanoTime() - startTime) / 1000000));
		System.out.println(sb);
	}

	static void solveSingle(CubieCube cc) {
		int sollen = 0;
		startTime = System.nanoTime();
		System.out.println(cc);
		Phase1Search p1search = new Phase1Search();
		java.util.ArrayList<int[]> p1sols = new java.util.ArrayList<int[]>();

		CubieCube[] p1cc = new CubieCube[3];
		for (int i = 0; i < 3; i++) {
			p1cc[i] = new CubieCube(cc);
			cc.doConj(16);
		}
		int[] p1ccidx = new int[1];

		p1search.solve(p1cc, new SolutionChecker() {
			int check(int[] solution, int length, int ccidx) {
				p1ccidx[0] = ccidx;
				p1sols.add(copySolution(solution, length));
				return 0;
			}
		});
		cc.copy(p1cc[p1ccidx[0]]);
		System.out.println("P1Conj: " + p1ccidx[0]);
		for (int[] sol : p1sols) {
			cc.doMove(sol);
			printSolution(sol, sollen += sol.length);
			break;
		}
		// System.out.println("Phase1 Solved");
		// System.out.println(cc.toFacelet());
		// System.out.println(cc);

		Phase2Search p2search = new Phase2Search();
		java.util.ArrayList<int[]> p2sols = new java.util.ArrayList<int[]>();
		p2search.solve(new CubieCube[] {cc}, new SolutionChecker() {
			int check(int[] solution, int length, int ccidx) {
				p2sols.add(copySolution(solution, length));
				return 0;
			}
		});
		for (int[] sol : p2sols) {
			cc.doMove(sol);
			printSolution(sol, sollen += sol.length);
			break;
		}
		// System.out.println("Phase2 Solved");
		// System.out.println(cc.toFacelet());
		// System.out.println(cc);

		Phase3Search p3search = new Phase3Search();
		java.util.ArrayList<int[]> p3sols = new java.util.ArrayList<int[]>();
		p3search.solve(new CubieCube[] {cc}, new SolutionChecker() {
			@Override
			int check(int[] solution, int length, int ccidx) {
				CubieCube cc1 = new CubieCube(cc);
				for (int i = 0; i < length; i++) {
					cc1.doMove(solution[i]);
				}
				int maskY = 0;
				int maskZ = 0;
				for (int i = 0; i < 4; i++) {
					maskY |= 1 << (cc1.wEdge[8 + i] % 12);
					maskY |= 1 << (cc1.wEdge[8 + i + 12] % 12);
					maskY |= 1 << (cc1.mEdge[8 + i] >> 1);
					maskZ |= 1 << (cc1.wEdge[4 + i] % 12);
					maskZ |= 1 << (cc1.wEdge[4 + i + 12] % 12);
					maskZ |= 1 << (cc1.mEdge[4 + i] >> 1);
				}
				if (Integer.bitCount(maskY) > 8 && Integer.bitCount(maskZ) > 8) {
					return 1;
				}
				p3sols.add(copySolution(solution, length));
				return 0;
			}
		});
		for (int[] sol : p3sols) {
			cc.doMove(sol);
			printSolution(sol, sollen += sol.length);
			break;
		}

		int conj = 0;
		int maskY = 0;
		int maskZ = 0;
		for (int i = 0; i < 4; i++) {
			maskY |= 1 << (cc.wEdge[8 + i] % 12);
			maskY |= 1 << (cc.wEdge[8 + i + 12] % 12);
			maskY |= 1 << (cc.mEdge[8 + i] >> 1);
			maskZ |= 1 << (cc.wEdge[4 + i] % 12);
			maskZ |= 1 << (cc.wEdge[4 + i + 12] % 12);
			maskZ |= 1 << (cc.mEdge[4 + i] >> 1);
		}
		if (Integer.bitCount(maskY) > Integer.bitCount(maskZ)) {
			System.out.println("Conj1");
			cc.doConj(1);
			conj = 1;
		}

		// System.out.println("Phase3 Solved");
		// System.out.println(cc.toFacelet());
		// System.out.println(cc);


		Phase4Search p4search = new Phase4Search();
		java.util.ArrayList<int[]> p4sols = new java.util.ArrayList<int[]>();
		p4search.solve(new CubieCube[] {cc}, new SolutionChecker() {
			int check(int[] solution, int length, int ccidx) {
				p4sols.add(copySolution(solution, length));
				return 0;
			}
		});
		for (int[] sol : p4sols) {
			cc.doMove(sol);
			printSolution(sol, sollen += sol.length);
			break;
		}

		// System.out.println(cc);

		cc.doConj(1);
		Phase5Search p5search = new Phase5Search();
		java.util.ArrayList<int[]> p5sols = new java.util.ArrayList<int[]>();
		p5search.solve(new CubieCube[] {cc}, new SolutionChecker() {
			int check(int[] solution, int length, int ccidx) {
				int[] solCopy = copySolution(solution, length);
				for (int i = 0; i < solCopy.length; i++) {
					solCopy[i] = CubieCube.SymMove[1][solCopy[i]];
				}
				p5sols.add(solCopy);
				return 0;
			}
		});
		cc.doConj(3);

		for (int[] sol : p5sols) {
			cc.doMove(sol);
			printSolution(sol, sollen += sol.length);
			break;
		}
		System.out.println(cc);
		System.out.println("Reduction: " + sollen);
	}

	public static void solveTest() {
		CubieCube.initSym();
		// System.out.println(CubieCube.SymMove[2][0]);

		Phase1Search.TCenterMove = (int[][]) Tools.LoadFromFile("Phase1TCenterMove.jdata");
		Phase1Search.XCenterMove = (int[][]) Tools.LoadFromFile("Phase1XCenterMove.jdata");
		if (Phase1Search.TCenterMove == null || Phase1Search.XCenterMove == null) {
			Phase1Search.initCenterMove();
			Tools.SaveToFile("Phase1TCenterMove.jdata", Phase1Search.TCenterMove);
			Tools.SaveToFile("Phase1XCenterMove.jdata", Phase1Search.XCenterMove);
		}
		Phase1Search.initCenterPrun();
		Phase2Search.initCenterMove();
		Phase2Search.initCenterPrun();


		Phase3Search.initWEdgeSymMove();
		Phase3Search.initMEdgeMove();
		Phase3Search.initCenterMove();
		Phase3Search.initPrun();


		Phase4Search.initCenterMove();
		Phase4Search.initEdgeMove();
		Phase4Search.initCenterPrun();
		Phase4Search.initEdgePrun();


		Phase5Search.initCenterMove();
		Phase5Search.initEdgeMove();
		Phase5Search.initCenterPrun();
		// Phase5Search.initEdgePrun();
		Phase5Search.initEdgeCenterPrun();

		// if (true) {
		// 	CubieCube cc = new CubieCube();
		// 	cc.doMove(Ux3, Dx1, rx1, Dx3, Bx1, Ux2, Rx2, fx2, Fx2, Bx2, bx3, ux1, rx2, fx1, ux2, Ux1, Fx3, Ux2, Fx3, ux1, Lx3, Fx3, lx2, Bx2, Fx2, ux2, Lx2, bx1, Rx1, Fx3, Lx1, ux1, Dx1, rx1, Lx2, dx1, lx2, bx1, lx3, Dx3, Lx2, fx2, Dx2, Fx3, bx1, Rx1, fx2, lx1, Dx3, ux2, Lx2, dx2, Ux3, lx1, Lx3, rx2, Fx3, lx2, Dx1, Rx2);
		// 	solveSingle(cc);
		// }

		java.util.Random gen = new java.util.Random(42L);
		for (int i = 0; i < 100; i++) {
			CubieCube cc = Tools.randomCube(gen);
			solveSingle(cc);
		}
		// CubieCube cc = Tools.randomCube(gen);
		// solveSingle(cc);
	}

	public static void main(String[] args) {
		solveTest();
	}
}