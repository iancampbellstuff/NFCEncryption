package edu.gsu.cs.nfcencryption.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import edu.gsu.cs.nfcencryption.ApplicationContext;
import edu.gsu.cs.nfcencryption.util.ErrorHandler;

/**
 *
 * @author Ian A. Campbell
 * @author Andrew J. Rutherford
 */
public final class LocalDatabase extends SQLiteOpenHelper {

    /**
     *
     */
    private static final String DATABASE_NAME = "NFCEncryption";

    /**
     *
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Used for a <em>Singleton</em> implementation.
     */
    private static LocalDatabase INSTANCE;

    /**
     * Used for a <em>Singleton</em> implementation.
     *
     * @param context
     */
    private LocalDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Any <code>Activity</code>'s <code>Context</code> can be passed -- this will be
     * used to instantiate the database with the <code>getApplicationContext()</code> method.
     *
     * @param context
     * @return
     */
    public static LocalDatabase getInstanceOf(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new LocalDatabase(context.getApplicationContext());
        }

        return INSTANCE;
    }

    /**
     * Here using <code></code>{@link edu.gsu.cs.nfcencryption.ApplicationContext#getInstanceOf()
     * ApplicationContext.getInstanceOf()}</code>.
     *
     * @return
     */
    public static LocalDatabase getInstanceOf() {
        return getInstanceOf(ApplicationContext.getInstanceOf());
    }

    /**
     * The database connection is to persist throughout the runtime of the application, so
     * this method is <em>only</em> to be called when exiting the application or the application
     * is terminated abnormally.
     */
    public void closeConnection() {
        this.close();
    }

    /**
     * This method is to be used for any <em>DML</em> <code>SQL</code> statement that affects the
     * database and is not expected to return a result.
     *
     * @param sql
     * @throws SQLiteException
     */
    public void executeInTransaction(String sql) throws SQLiteException {
        SQLiteDatabase db = this.getWritableDatabase();

        db.beginTransaction();
        try {
            db.execSQL(sql);
            db.setTransactionSuccessful();

        } catch (Throwable e) {
            ErrorHandler.handle(e);

        } finally {
            db.endTransaction();
        }
    }

    /**
     * This method is overridden so as to enable foreign-key constraints -- this is not enabled
     * by default. This method is invoked by the parent class, and is <em>not</em> to be called.
     *
     * @param db
     */
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        // Enabling foreign-key constraints here:
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    /**
     * <code>CREATE TABLE</code> statements are to be executed here, where the OS only executes
     * this on initial install, if the database doesn't already exist. This method is invoked by
     * the parent class, and is <em>not</em> to be called.
     *
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating table here:
        db.execSQL("CREATE TABLE IF NOT EXISTS " + PasswordTable.NAME + " (" +
                        PasswordTable.Columns.ID + " INTEGER PRIMARY KEY NOT NULL UNIQUE, " +
                        PasswordTable.Columns.SALT + " BLOB NOT NULL, " +
                        PasswordTable.Columns.HASH + " BLOB NOT NULL, " +
                        PasswordTable.Columns.ALGORITHM_TYPE + " TEXT NOT NULL, " +
                        "CHECK (" + PasswordTable.Columns.ID + " = 1));"
        );
    }

    /**
     * This is required, currently not implemented. This method is invoked by the parent class,
     * and is <em>not</em> to be called.
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }
}
