package generalRBE;

import java.util.ArrayList;
import java.util.Arrays;

public class Array2StrConverter {
	public Integer[] stringtoArray(String str) {
		ArrayList<Integer> numbers = new ArrayList<Integer>();
		for (String s : str.split("\\s+")) {
				numbers.add(Integer.valueOf(s));
		}
		return numbers.toArray(new Integer[0]);
	}
}