package cs.cube555;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;

class TableCache {

	static boolean SaveToFile(String filename, Object obj) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename));
			oos.writeObject(obj);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	static Object LoadFromFile(String filename) {
		Object ret;
		try {
			ObjectInputStream oos = new ObjectInputStream(new FileInputStream(filename));
			ret = oos.readObject();
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return ret;
	}
}