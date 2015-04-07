package edu.gsu.cs.nfcencryption.util;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

/**
 *
 * @author Ian A. Campbell
 * @author Andrew J. Rutherford
 */
public final class SfxPlayer {

    /**
     * Set as the application-applicationContext from the first-passed <code>Context</code> when the
     * <em>Singleton</em> is instantiated.
     */
    private final Context applicationContext;

    /**
     * Used for a <em>Singleton</em> implementation.
     */
    private static SfxPlayer INSTANCE;

    /**
     * <code>private</code> for a <em>Singleton</em> implementation.
     * @param context
     */
    private SfxPlayer(Context context) {
        this.applicationContext = context.getApplicationContext();
    }

    /**
     *
     * @param context
     * @return a <em>Singleton</em> instance of this class.
     */
    public static SfxPlayer getInstanceOf(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SfxPlayer(context);
        }

        return INSTANCE;
    }

    /**
     * Used to play a sound in a separate <code>Thread</code></code> from the executing code, and
     * in a <code>synchronized</code> block for safety, so that other tasks may execute in parallel.
     *
     * @param ringtone
     */
    private void playRingtoneThread(final Ringtone ringtone) {
        synchronized(this) {
            new Thread() {
                @Override
                public void run() {
                    ringtone.play();

                    // waiting some brief amount of time before stopping the sound:
                    long duration = 1000,
                            startTime = System.currentTimeMillis(), stopTime = startTime + duration;
                    do {
                        startTime = System.currentTimeMillis();
                    } while (ringtone.isPlaying() && startTime < stopTime);
                    ringtone.stop();
                }
            }.start();
        }
    }

    /**
     * See <a href="http://stackoverflow.com/a/8568304">this stackoverflow answer</a> for reference.
     */
    public void playNotificationSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone ringtone = RingtoneManager.getRingtone(this.applicationContext, notification);

            this.playRingtoneThread(ringtone);

        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * See <a href="http://stackoverflow.com/a/8568304">this stackoverflow answer</a> for reference.
     */
    public void playAlarmSound() {
        try {
            Uri alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            final Ringtone ringtone = RingtoneManager.getRingtone(this.applicationContext, alarm);

            this.playRingtoneThread(ringtone);

        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
}
