package de.danoeh.antennapod.ui.episodetags;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EpisodeTagStorage extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "EpisodeTags.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE = "EpisodeTags";
    private static final String KEY_ITEM = "feeditem";
    private static final String KEY_TAG = "tag";

    private static EpisodeTagStorage instance;

    public static synchronized EpisodeTagStorage getInstance(Context context) {
        if (instance == null) {
            instance = new EpisodeTagStorage(context.getApplicationContext());
        }
        return instance;
    }

    private EpisodeTagStorage(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" + KEY_ITEM + " INTEGER NOT NULL, "
                + KEY_TAG + " TEXT NOT NULL, PRIMARY KEY (" + KEY_ITEM + ", " + KEY_TAG + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public synchronized Set<String> getTags(long itemId) {
        Set<String> tags = new HashSet<>();
        try (Cursor cursor = getReadableDatabase().query(TABLE, new String[]{KEY_TAG},
                KEY_ITEM + " = ?", new String[]{String.valueOf(itemId)}, null, null, null)) {
            while (cursor.moveToNext()) {
                tags.add(cursor.getString(0));
            }
        }
        return tags;
    }

    public synchronized Map<Long, Set<String>> getAllItemTags() {
        Map<Long, Set<String>> result = new HashMap<>();
        try (Cursor cursor = getReadableDatabase().query(TABLE, new String[]{KEY_ITEM, KEY_TAG},
                null, null, null, null, null)) {
            while (cursor.moveToNext()) {
                long itemId = cursor.getLong(0);
                if (!result.containsKey(itemId)) {
                    result.put(itemId, new HashSet<>());
                }
                result.get(itemId).add(cursor.getString(1));
            }
        }
        return result;
    }

    public synchronized List<String> getAllTags() {
        List<String> tags = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().rawQuery("SELECT DISTINCT " + KEY_TAG + " FROM " + TABLE
                + " ORDER BY " + KEY_TAG + " COLLATE NOCASE", null)) {
            while (cursor.moveToNext()) {
                tags.add(cursor.getString(0));
            }
        }
        return tags;
    }

    public synchronized void setTags(Collection<Long> itemIds, Set<String> tags) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (long itemId : itemIds) {
                db.delete(TABLE, KEY_ITEM + " = ?", new String[]{String.valueOf(itemId)});
                for (String tag : tags) {
                    ContentValues values = new ContentValues();
                    values.put(KEY_ITEM, itemId);
                    values.put(KEY_TAG, tag);
                    db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        EventBus.getDefault().post(new ChangedEvent());
    }

    public static class ChangedEvent {
    }
}
