package cs.cube555;

import java.util.ArrayList;

import static cs.cube555.Util.*;

public class Search {
	static long startTime;

	static void printSolution(int[] solution) {
		StringBuffer sb = new StringBuffer();
		for (int move : solution) {
			sb.append(move2str[move]).append(' ');
		}
		sb.append(String.format("(%df)", solution.length));
		System.out.println(sb);
	}

	static void solveSingle(CubieCube cc) {
		Phase1Search p1search = new Phase1Search();
		Phase2Search p2search = new Phase2Search();
		Phase3Search p3search = new Phase3Search();
		Phase4Search p4search = new Phase4Search();
		Phase5Search p5search = new Phase5Search();

		int phase1SolsSize = 10;
		int phase2SolsSize = 500;
		int phase3SolsSize = 500;
		int phase4SolsSize = 500;
		int phase5SolsSize = 1;
		int sollen = 0;
		startTime = System.nanoTime();
		System.out.println(cc);
		cc = new SolvingCube(cc);

		SolvingCube[] p1cc = new SolvingCube[3];
		for (int i = 0; i < 3; i++) {
			p1cc[i] = new SolvingCube(cc);
			cc.doConj(16);
		}
		ArrayList<SolvingCube> p1sols = new ArrayList<SolvingCube>();
		p1search.solve(p1cc, new SolutionChecker() {
			@Override
			int check(int[] solution, int length, int ccidx) {
				SolvingCube sc = new SolvingCube(p1cc[ccidx]);
				sc.doMove(copySolution(solution, length));
				p1sols.add(sc);
				return p1sols.size() >= phase1SolsSize ? 0 : 1;
			}
		});
		System.out.println(String.format("Phase1 Finished in %d ms", (System.nanoTime() - startTime) / 1000000));
		startTime = System.nanoTime();
		for (SolvingCube sc : p1sols) {
			sc.addCheckPoint();
		}

		ArrayList<SolvingCube> p2sols = new ArrayList<SolvingCube>();
		p2search.solve(p1sols.toArray(new SolvingCube[0]), new SolutionChecker() {
			@Override
			int check(int[] solution, int length, int ccidx) {
				SolvingCube sc = new SolvingCube(p1sols.get(ccidx));
				sc.doMove(copySolution(solution, length));
				p2sols.add(new SolvingCube(sc));
				sc.doConj(16);
				p2sols.add(new SolvingCube(sc));
				sc.doConj(16);
				p2sols.add(new SolvingCube(sc));
				return p2sols.size() >= phase2SolsSize ? 0 : 1;
			}
		});
		System.out.println(String.format("Phase2 Finished in %d ms", (System.nanoTime() - startTime) / 1000000));
		startTime = System.nanoTime();
		for (SolvingCube sc : p2sols) {
			sc.addCheckPoint();
		}

		ArrayList<SolvingCube> p3sols = new ArrayList<SolvingCube>();
		p3search.solve(p2sols.toArray(new SolvingCube[0]), new SolutionChecker() {
			@Override
			int check(int[] solution, int length, int ccidx) {
				SolvingCube sc = new SolvingCube(p2sols.get(ccidx));
				for (int i = 0; i < length; i++) {
					sc.doMove(solution[i]);
				}
				int maskY = 0;
				int maskZ = 0;
				for (int i = 0; i < 4; i++) {
					maskY |= 1 << (sc.wEdge[8 + i] % 12);
					maskY |= 1 << (sc.wEdge[8 + i + 12] % 12);
					maskY |= 1 << (sc.mEdge[8 + i] >> 1);
					maskZ |= 1 << (sc.wEdge[4 + i] % 12);
					maskZ |= 1 << (sc.wEdge[4 + i + 12] % 12);
					maskZ |= 1 << (sc.mEdge[4 + i] >> 1);
				}
				if (Integer.bitCount(maskY) <= 8) {
					p3sols.add(sc);
				}
				if (Integer.bitCount(maskZ) <= 8) {
					SolvingCube sc1 = new SolvingCube(sc);
					sc1.doConj(1);
					p3sols.add(sc1);
				}
				return p3sols.size() >= phase3SolsSize ? 0 : 1;
			}
		});
		System.out.println(String.format("Phase3 Finished in %d ms", (System.nanoTime() - startTime) / 1000000));
		startTime = System.nanoTime();
		for (SolvingCube sc : p3sols) {
			sc.addCheckPoint();
		}

		ArrayList<SolvingCube> p4sols = new ArrayList<SolvingCube>();
		p4search.solve(p3sols.toArray(new SolvingCube[0]), new SolutionChecker() {
			@Override
			int check(int[] solution, int length, int ccidx) {
				SolvingCube sc = new SolvingCube(p3sols.get(ccidx));
				sc.doMove(copySolution(solution, length));
				sc.doConj(1);
				p4sols.add(sc);
				return p4sols.size() >= phase4SolsSize ? 0 : 1;
			}
		});
		System.out.println(String.format("Phase4 Finished in %d ms", (System.nanoTime() - startTime) / 1000000));
		startTime = System.nanoTime();
		for (SolvingCube sc : p4sols) {
			sc.addCheckPoint();
		}

		ArrayList<SolvingCube> p5sols = new ArrayList<SolvingCube>();
		p5search.solve(p4sols.toArray(new SolvingCube[0]), new SolutionChecker() {
			@Override
			int check(int[] solution, int length, int ccidx) {
				SolvingCube sc = new SolvingCube(p4sols.get(ccidx));
				sc.doMove(copySolution(solution, length));
				p5sols.add(sc);
				return p5sols.size() >= phase5SolsSize ? 0 : 1;
			}
		});
		System.out.println(String.format("Phase5 Finished in %d ms", (System.nanoTime() - startTime) / 1000000));
		startTime = System.nanoTime();

		for (SolvingCube sc : p5sols) {
			sc.addCheckPoint();
			System.out.println(sc);
			System.out.println("Reduction: " + sc.length());
		}
	}

	public static void solveTest() {
		CubieCube.initSym();

		Phase1Search.initCenter();

		Phase2Search.initCenterMove();
		Phase2Search.initCenterPrun();

		Phase3Search.initWEdgeSymMove();
		Phase3Search.initMEdgeMove();
		Phase3Search.initCenterMove();
		Phase3Search.initPrun();

		Phase4Search.initCenterMove();
		Phase4Search.initEdgeMove();
		Phase4Search.initMLEdgeSymMove();
		Phase4Search.initPrun();

		Phase5Search.initLEdgeSymMove();
		Phase5Search.initCenterMove();
		Phase5Search.initEdgeMove();
		Phase5Search.initPrun();

		java.util.Random gen = new java.util.Random(42L);
		for (int i = 0; i < 100; i++) {
			CubieCube cc = Tools.randomCube(gen);

			// CubieCube cc = new CubieCube();
			// int[] scramble = new int[100];
			// for (int j = 0; j < 100; j++) {
			// 	scramble[j] = gen.nextInt(36);
			// }
			// System.out.print("Scramble: ");
			// printSolution(scramble);
			// cc.doMove(scramble);

			solveSingle(cc);
		}
		// CubieCube cc = Tools.randomCube(gen);
		// solveSingle(cc);
	}

	public static void main(String[] args) {
		solveTest();
	}
}