package de.danoeh.antennapod.storage.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class InboxSeenStorage extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "InboxSeen.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE = "InboxSeen";
    private static final String KEY_ITEM = "feeditem";
    private static final String KEY_SEEN_AT = "seen_at";
    private static final String PREFS = "InboxSeen";
    private static final String PREF_LAST_REFRESH = "lastRefresh";

    private static InboxSeenStorage instance;

    public static synchronized InboxSeenStorage getInstance(Context context) {
        if (instance == null) {
            instance = new InboxSeenStorage(context.getApplicationContext());
        }
        return instance;
    }

    public static void recordRefresh(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putLong(PREF_LAST_REFRESH, System.currentTimeMillis()).apply();
    }

    public static long getLastRefresh(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(PREF_LAST_REFRESH, 0);
    }

    private InboxSeenStorage(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" + KEY_ITEM + " INTEGER PRIMARY KEY, "
                + KEY_SEEN_AT + " INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public synchronized Map<Long, Long> getAll() {
        Map<Long, Long> result = new HashMap<>();
        try (Cursor cursor = getReadableDatabase().query(TABLE, new String[]{KEY_ITEM, KEY_SEEN_AT},
                null, null, null, null, null)) {
            while (cursor.moveToNext()) {
                result.put(cursor.getLong(0), cursor.getLong(1));
            }
        }
        return result;
    }

    public synchronized void markSeen(Collection<Long> itemIds, long seenAt) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (long itemId : itemIds) {
                ContentValues values = new ContentValues();
                values.put(KEY_ITEM, itemId);
                values.put(KEY_SEEN_AT, seenAt);
                db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public synchronized void retainOnly(Set<Long> itemIds) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (long itemId : getAll().keySet()) {
                if (!itemIds.contains(itemId)) {
                    db.delete(TABLE, KEY_ITEM + " = ?", new String[]{String.valueOf(itemId)});
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
