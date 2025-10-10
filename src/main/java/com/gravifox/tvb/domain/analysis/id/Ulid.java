package com.gravifox.tvb.domain.analysis.id;

import java.security.SecureRandom;

/**
 * ULID generator (without external deps).
 * Format: 26 chars (10 for timestamp, 16 for randomness) using Crockford Base32.
 * Time-ordered by millisecond. Not a monotonic variant (collisions are extremely unlikely).
 */
public final class Ulid {
    private static final char[] CROCKFORD = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();
    private static final SecureRandom RNG = new SecureRandom();

    private Ulid() {}

    public static String generate() {
        long time = System.currentTimeMillis(); // 48-bit space is enough for timestamp
        char[] out = new char[26];

        // Encode time (48 bits) into 10 base32 chars (MSB-first)
        long t = time;
        for (int i = 9; i >= 0; i--) {
            out[i] = CROCKFORD[(int) (t & 0x1F)];
            t >>>= 5;
        }

        // Encode randomness (80 bits) into 16 base32 chars
        // Generate 80 random bits (16 groups of 5 bits)
        int idx = 10;
        int bits = 0;
        int bitBuffer = 0;
        while (idx < 26) {
            if (bits < 5) {
                bitBuffer = (bitBuffer << 8) | RNG.nextInt(256);
                bits += 8;
            }
            int shift = bits - 5;
            int val = (bitBuffer >> shift) & 0x1F;
            bitBuffer &= ((1 << shift) - 1);
            bits = shift;
            out[idx++] = CROCKFORD[val];
        }
        return new String(out);
    }
}

