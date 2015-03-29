package edu.gsu.cs.nfcencryption.password;

/**
 * <em>package-access</em> only.
 *
 * @author Ian A. Campbell
 * @author Andrew J. Rutherford
 */
enum EncryptionAlgorithm {

    /**
     * See the <a href="https://en.wikipedia.org/wiki/PBKDF2">Wikipedia page on <em>PBKDF2</em></a>
     * for reference.
     */
    PBKDF2("PBKDF2WithHmacSHA1", 10000, 256);

    /**
     *
     */
    private final String type;

    /**
     * A required parameter for the usage of <code>PBEKeySpec</code> in <code>
     * {@link EncryptedPassword#getHash(char[], byte[])
     * -getHash(char[], byte[]):String}</code>.
     */
    private final int iterations;

    /**
     * A required parameter for the usage of <code>PBEKeySpec</code> in <code>
     * {@link EncryptedPassword#getHash(char[], byte[])
     * -getHash(char[], byte[]):String}</code>.
     */
    private final int keyLength;

    /**
     * enums cannot be instantiated directly, <code>private</code> by default.
     *
     * @param type
     * @param iterations
     * @param keyLength
     */
    private EncryptionAlgorithm(String type, int iterations, int keyLength) {
        this.type = type;
        this.iterations = iterations;
        this.keyLength = keyLength;
    }

    /**
     * <em>package-access</em> only.
     *
     * @return
     */
    String getType() {
        return this.type;
    }

    /**
     * <em>package-access</em> only.
     *
     * @return
     */
    int getIterations() {
        return this.iterations;
    }

    /**
     * <em>package-access</em> only.
     *
     * @return
     */
    int getKeyLength() {
        return this.keyLength;
    }
}
