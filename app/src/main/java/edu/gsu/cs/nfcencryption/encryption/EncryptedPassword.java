package edu.gsu.cs.nfcencryption.encryption;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * See <a href="http://stackoverflow.com/a/18143616">this stackoverflow answer</a> for reference.
 *
 * @author Ian A. Campbell
 * @author Andrew J. Rutherford
 */
public final class EncryptedPassword {

    /**
     * An <code>enum</code> instance that contains fields necessary for the
     * <code> {@link EncryptedPassword#getHash(char[], byte[])
     * -hash(char[], byte[]):String}</code> method.
     */
    private final EncryptionAlgorithm encryptionAlgorithm;

    /**
     * See the <a href="https://en.wikipedia.org/wiki/Salt_(cryptography)">Wikipedia page on
     * <em>Salt (cryptography)</em></a> for reference.
     */
    private final byte[] salt;

    /**
     *
     */
    private final byte[] hash;

    /**
     * This constructor is used to generate new values, to then be used to update the database.
     *
     * @param password  passed from <code>NFCHandler</code>
     * @param encryptionAlgorithm passed from <code>NFCHandler</code>
     */
    public EncryptedPassword(char[] password, EncryptionAlgorithm encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
        this.salt = this.getRandomSalt();
        this.hash = this.getHash(password, this.salt);
    }

    /**
     * Parameters passed to this constructor are to first be retrieved from the local database.
     *
     * @param algorithmType retrieved from the local database
     * @param salt          retrieved from the local database
     * @param hash          retrieved from the local database
     */
    public EncryptedPassword(byte[] salt, byte[] hash, String algorithmType) {
        this.encryptionAlgorithm = EncryptionAlgorithm.valueOf(algorithmType);
        this.salt = salt;
        this.hash = hash;
    }

    /**
     *
     * @return
     */
    public byte[] getSalt() {
        return this.salt;
    }

    /**
     *
     * @return
     */
    public byte[] getHash() {
        return this.hash;
    }

    /**
     *
     * @return
     */
    public String getAlgorithmType() {
        return this.encryptionAlgorithm.getType();
    }

    /**
     * See the <a href="https://docs.oracle.com/javase/7/docs/api/java/security/SecureRandom.html">
     * Oracle documentation on <code>SecureRandom</code></a> and the
     * <a href="https://en.wikipedia.org/wiki/Salt_(cryptography)">Wikipedia page on
     * <em>Salt (cryptography)</em></a> for reference.
     *
     * @return a random byte[] salt for hashing a password.
     */
    private byte[] getRandomSalt() {
        byte[] randomBytes = new byte[16];
        new SecureRandom().nextBytes(randomBytes);
        return randomBytes;
    }

    /**
     * @param password
     * @param salt
     * @return
     */
    private byte[] getHash(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt,
                this.encryptionAlgorithm.getIterations(), this.encryptionAlgorithm.getKeyLength());

        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(this.encryptionAlgorithm.getType());
            return skf.generateSecret(spec).getEncoded();

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalArgumentException(e);

        } finally {
            spec.clearPassword();
        }
    }

    /**
     *
     * @param password
     * @return
     */
    public boolean matches(char[] password) {
        byte[] passwordHash = this.getHash(password, this.salt);

        if (passwordHash.length != this.hash.length) {
            return false;
        }

        for (int i = 0, len = passwordHash.length; i < len; i++) {
            if (passwordHash[i] != this.hash[i]) {
                return false;
            }
        }

        return true;
    }
}
