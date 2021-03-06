package com.wingjay.jianshi.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wingjay.jianshi.bean.Diary;
import com.wingjay.jianshi.global.JianShiApplication;
import com.wingjay.jianshi.util.FullDateManager;

/**
 * Created by wingjay on 9/30/15.
 */
public class DbUtil {

    private static DbOpenHepler dbOpenHepler = JianShiApplication.getInstance().getDbOpenHepler();
    private static String deviceId = JianShiApplication.getInstance().getDeviceId();
    private final static Object object = new Object();

    public static long saveDiary(String title, String content, long date) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Diary.DEVICE_ID, deviceId);
        contentValues.put(Diary.TITLE, title);
        contentValues.put(Diary.CONTENT, content);
        contentValues.put(Diary.CREATED_TIME, date);

        long insertId = 0;
        synchronized (object) {
            SQLiteDatabase db = dbOpenHepler.getWritableDatabase();
            insertId = db.insert(Diary.getTableName(), Diary._ID, contentValues);
        }
        return insertId;
    }

    public static long updateDiary(String title, String content, long diaryId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Diary.TITLE, title);
        contentValues.put(Diary.CONTENT, content);
        contentValues.put(Diary.MODIFIED_TIME, FullDateManager.getTodayDateSeconds());

        long rowChanged = 0;
        synchronized (object) {
            SQLiteDatabase db = dbOpenHepler.getWritableDatabase();
            rowChanged = db.update(Diary.getTableName(), contentValues, Diary._ID + "=?",
                    new String[]{String.valueOf(diaryId)});
        }
        return (rowChanged > 0) ? diaryId : rowChanged;
    }

    public static Diary getDiary(long id) {
      Cursor cursor = getDiaryCursor(id);
      if (cursor.getCount() != 1) {
        return null;
      }
      Diary diary = null;
      if (cursor.moveToFirst()) {
        diary = new Diary();
        do {
          diary.setId(cursor.getLong(cursor.getColumnIndexOrThrow(Diary._ID)));
          diary.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(Diary.TITLE)));
          diary.setContent(cursor.getString(cursor.getColumnIndexOrThrow(Diary.CONTENT)));
          diary.setDeviceId(cursor.getString(cursor.getColumnIndexOrThrow(Diary.DEVICE_ID)));
          diary.setDeleted(cursor.getInt(cursor.getColumnIndexOrThrow(Diary.DELETED)));
          diary.setCreatedTime(cursor.getLong(cursor.getColumnIndexOrThrow(Diary.CREATED_TIME)));
          diary.setModifiedTime(cursor.getLong(cursor.getColumnIndexOrThrow(Diary.MODIFIED_TIME)));
        } while (cursor.moveToNext());
      }

      return diary;
    }

    public static Cursor getDiaryCursor(long id) {
        String where = Diary._ID + "=" + String.valueOf(id);
        String sortOrder = Diary.CREATED_TIME;
        Cursor cursor;
        synchronized (object) {
            SQLiteDatabase db = dbOpenHepler.getReadableDatabase();
            cursor = db.query(Diary.getTableName(),
                new String[]{Diary._ID, Diary.TITLE, Diary.CONTENT, Diary.DELETED, Diary.DEVICE_ID,
                    Diary.CREATED_TIME, Diary.MODIFIED_TIME}, where, null, null, null, sortOrder);
        }
        return cursor;
    }

    public static Cursor getAllDiary() {
        String where = Diary.DELETED + "=0 and " + Diary.CREATED_TIME + ">0";
        String sortOrder = Diary.CREATED_TIME + " desc ";
        Cursor cursor;
        synchronized (object) {
            SQLiteDatabase db = dbOpenHepler.getReadableDatabase();
            cursor = db.query(Diary.getTableName(), new String[]{Diary._ID, Diary.TITLE, Diary.CONTENT,
                    Diary.CREATED_TIME}, where, null, null, null, sortOrder);
        }
        return cursor;
    }

    public static int deleteDiary(long diaryId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Diary.DELETED, 1);

        int rowChanged = 0;
        synchronized (object) {
            SQLiteDatabase db = dbOpenHepler.getWritableDatabase();
            rowChanged = db.update(Diary.getTableName(), contentValues, Diary._ID + "=?",
                    new String[]{String.valueOf(diaryId)});
        }
        return rowChanged;
    }

}
