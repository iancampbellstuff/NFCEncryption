package edu.gsu.cs.nfcencryption.database;

/**
 * See <a href="http://stackoverflow.com/a/7297532/1298685">this stackoverflow answer</a> for
 * reference.
 *
 * @author Ian A. Campbell
 * @author Andrew J. Rutherford
 */
public final class PasswordTable {

    /**
     *
     */
    public static final String NAME = "Password";

    /**
     *
     */
    private PasswordTable() {
    }

    /**
     *
     */
    public static enum Columns {

        /**
         * Used to restrict the number of columns in the <code>Password</code> table to always be
         * <code>1</code>.
         */
        ID("Id"),

        /**
         *
         */
        SALT("Salt"),

        /**
         *
         */
        HASH("Hash"),

        /**
         *
         */
        ALGORITHM_TYPE("AlgorithmType");

        /**
         *
         */
        private final String columnName;

        /**
         *
         * @param columnName
         */
        private Columns(String columnName) {
            this.columnName = columnName;
        }

        /**
         *
         * @return
         */
        public String getColumnName() {
            return this.columnName;
        }

        /**
         *
         * @return
         */
        @Override
        public String toString() {
            return this.columnName;
        }
    }
}
