package uk.me.mantas.eternity.tests;

import org.junit.Test;
import uk.me.mantas.eternity.EKUtils;

import static org.junit.Assert.assertArrayEquals;

public class EKUtilsTest {
	@Test
	public void removeBOMTest () {
		byte[] data = new byte[]{-17, -69, -65, 100, 97, 116, 97};
		byte[] actual = EKUtils.removeBOM(data);
		byte[] expected = new byte[]{100, 97, 116, 97};
		assertArrayEquals(expected, actual);
	}

	@Test
	public void addBOMTest () {
		byte[] actual = EKUtils.addBOM(new byte[]{});
		byte[] expected = new byte[]{-17, -69, -65};
		assertArrayEquals(expected, actual);
	}
}
