/**
 *  Eternity Keeper, a Pillars of Eternity save game editor.
 *  Copyright (C) 2015 Kim Mantas
 *
 *  Eternity Keeper is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  Eternity Keeper is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


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
