package cs.cube555;

abstract class SolutionChecker {

	abstract int check(int[] solution, int length, int ccidx);

	static int[] copySolution(int[] solution, int length) {
		int[] solutionCopy = new int[length];
		for (int i = 0; i < length; i++) {
			solutionCopy[i] = solution[i];
		}
		return solutionCopy;
	}
}