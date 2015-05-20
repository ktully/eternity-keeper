package uk.me.mantas.eternity.serializer;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.me.mantas.eternity.serializer.properties.MultiDimensionalArrayProperty.ArrayDimension;

public class ArrayAnalyser {
	List<ArrayDimension> dimensions = new ArrayList<>();
	List<Integer[]> coordSet = new ArrayList<>();
	Map<Integer, Integer> maxDimLen = new HashMap<>();

	public ArrayAnalyser (Object ar) {
		if (!ar.getClass().isArray()) {
			throw new IllegalArgumentException();
		}

		int dim = ar.getClass().getName().lastIndexOf("[") + 1;
		int[] coord = new int[dim];
		extractElement(ar, coord, 0);

		for (int i = 0; i < dim; i++) {
			int max = maxDimLen.get(i);
			dimensions.add(new ArrayDimension(0, max));
		}
	}

	private void extractElement (Object ar, int[] coord, int dim) {
		int len = Array.getLength(ar);
		Integer max = maxDimLen.get(dim);
		if (max == null || len > max) {
			max = len;
		}

		maxDimLen.put(dim, max);

		for (int i = 0; i < len; i++) {
			Object el = Array.get(ar, i);
			if (el != null && el.getClass().isArray()) {
				coord[dim] = i;
				extractElement(el, coord, dim + 1);
			} else {
				coordSet.add(box(coord));
			}
		}
	}

	private Integer[] box (int[] in) {
		Integer[] out = new Integer[in.length];
		for (int i = 0; i < in.length; i++) {
			out[i] = in[i];
		}

		return out;
	}

	public List<ArrayDimension> getDimensions () {
		return dimensions;
	}

	public List<Integer[]> getCoordSet () {
		return coordSet;
	}
}
