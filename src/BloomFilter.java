import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.tomgibara.bits.BitVector;

/**
 * Bloom filter for efficiently managing large sets.
 * 
 * @author ben
 * @param E
 *
 */
public class BloomFilter {

	private int size;
	private int flippedBits;
	private int capacity;
	private int numHashes;

	private BitVector store;
	private MessageDigest digester;

	/**
	 * Space-efficient probabilistic datastore for set membership.
	 * 
	 * Based expectedItems and falsePositiveRate, determines the optimal
	 * capacity and number of hashes to use in the bloom filter.
	 * 
	 * https://en.wikipedia.org/wiki/Bloom_filter#Optimal_number_of_hash_functions
	 * 
	 * @param expectedItems
	 * @param falsePositiveRate
	 * @throws NoSuchAlgorithmException
	 */
	public BloomFilter(int expectedItems, float falsePositiveRate) throws NoSuchAlgorithmException {
		// Optimal size m, inserted elements m, and false positive probability.
		// m = - (n ln(p)) / (ln2)^2
		// Number of hashes k = - ln(p)/ln(2)
		this((int) (-expectedItems * Math.log(falsePositiveRate) / Math.pow(Math.log(2), 2)),
				(int) (-Math.log(falsePositiveRate) / Math.log(2)));
	}

	/**
	 * Constructor for users with specific needs.
	 * @param capacity
	 * @param numHashes
	 * @throws NoSuchAlgorithmException
	 */
	public BloomFilter(int capacity, int numHashes) throws NoSuchAlgorithmException {
		this.capacity = capacity;
		this.numHashes = numHashes;
		this.store = new BitVector(capacity);
		this.digester = MessageDigest.getInstance("MD5");
		this.size = 0;
		this.flippedBits = 0;
	}

	public int getCapacity() {
		return capacity;
	}

	public int getNumHashes() {
		return numHashes;
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * This is useful to know when the filter is getting full and should be
	 * rebuilt with a larger capacity.
	 * 
	 * @return
	 */
	public int flippedBits() {
		return flippedBits;
	}

	/**
	 * True if this key may have been added. Wrong flasePositiveRate fraction of
	 * the time. False if it was definitely not added.
	 * 
	 * @param key
	 * @return
	 */
	public boolean contains(String key) {
		int hashes[] = getHashes(key);
		for (int hash : hashes) {
			if (!store.getBit(hash % capacity)) {
				return false;
			}
		}
		return true;
	}

	public void add(String key) {
		size++;
		int hashes[] = getHashes(key);
		for (int hash : hashes) {
			boolean prev = store.getThenSetBit(hash % capacity, true);
			if (!prev) {
				flippedBits++;
			}
		}
	}

	/**
	 * Compute numHashes independent hashes of the key.
	 * 
	 * This could be made more efficient by using different subsets of each hash
	 * multiple times but it could be premature optimization assuming the keys
	 * are reasonably small.
	 * 
	 * @param key
	 *            String to hash.
	 * @return List of ints
	 */
	private int[] getHashes(String key) {
		int hashes[] = new int[this.numHashes];

		for (int i = 0; i < this.numHashes; i++) {
			byte[] bytes = digester.digest(String.format("%s%s%d", key, "magicsalt", i).getBytes());
			// Pack first 4 byttes into an int
			hashes[i] = Math.abs(bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF));

		}
		return hashes;
	}

	public boolean containsAll(Collection<String> keys) {
		for (String key : keys) {
			if (!this.contains(key)) {
				return false;
			}
		}
		return true;
	}

	public void addAll(Collection<String> keys) {
		for (String key : keys) {
			this.add(key);
		}
	}

	public void clear() {
		size = 0;
		flippedBits = 0;
		store.clear();
	}
}
