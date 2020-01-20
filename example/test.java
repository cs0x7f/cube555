import cs.cube555.Search;
import cs.cube555.Tools;
import cs.cube555.Search.Logger;

public class test {
	public static void solveTest() {
		Search.init();
		Search search = new Search();
		java.util.Random gen = new java.util.Random(42L);
		for (int i = 0; i < 100; i++) {
			String cc = Tools.randomCube(gen);
			String solution = search.solveReduction(cc, 0)[0];
			System.out.println(solution);
			Logger.print(i + 1);
		}
	}

	public static void main(String[] args) {
		solveTest();
	}
}