import static org.junit.Assert.*;

import java.security.NoSuchAlgorithmException;

import org.junit.Test;

public class BloomFilterTest {

	@Test
	public void test() {
		BloomFilter filter;
		final int TEST_SIZE = 10000;
		try {
			filter = new BloomFilter(TEST_SIZE, 0.01f);
		} catch (NoSuchAlgorithmException e) {
			fail(e.toString());
			return;
		}

		assertEquals(95850, filter.getCapacity());
		assertEquals(0, filter.size());
		assertEquals(6, filter.getNumHashes());
		final String TEST = "test";
		filter.add(TEST);
		assertEquals(filter.getNumHashes(), filter.flippedBits());
		assertEquals(1, filter.size());

		assertTrue(filter.contains("test"));
		assertFalse(filter.contains("hello"));
		for (int i = 0; i < TEST_SIZE; i++) {
			filter.add(Integer.toString(i));
		}
		assertTrue(filter.contains("test"));
		assertTrue(filter.contains("101"));
		assertEquals(TEST_SIZE + 1, filter.size());
		assertTrue(filter.flippedBits() >= TEST_SIZE);
		assertFalse(filter.contains("asdfasdfas"));
		assertFalse(filter.contains("hello"));
	}

}
