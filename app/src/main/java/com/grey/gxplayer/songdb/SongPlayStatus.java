package com.grey.gxplayer.songdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.grey.gxplayer.models.PlayBackTrack;
import com.grey.gxplayer.util.MyUtil;

import java.util.ArrayList;

public class SongPlayStatus {

    private AudioDB audioDB = null;

    public static SongPlayStatus songPlayStatus = null;


    public SongPlayStatus(Context context){
        audioDB = AudioDB.getInstance(context);
    }

    public static SongPlayStatus getInstance(Context context){
        if(songPlayStatus == null){
            songPlayStatus = new SongPlayStatus(context);
        }
        return songPlayStatus;
    }

    public void onCreate(SQLiteDatabase db){
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ");
        builder.append(SongColumn.NAME);
        builder.append("(");
        builder.append(SongColumn.TRACK_ID);
        builder.append(" LONG NOT NULL,");
        builder.append(SongColumn.SOURCE_ID);
        builder.append(" LONG NOT NULL,");
        builder.append(SongColumn.SOURCE_TYPE);
        builder.append(" INT NOT NULL,");
        builder.append(SongColumn.SOURCE_POSITION);
        builder.append(" INT NOT NULL)");
        db.execSQL(builder.toString());
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion < 2 && newVersion >= 2){
            onCreate(db);
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ SongColumn.NAME);
    }

    public synchronized void saveSongInDb(ArrayList<PlayBackTrack> list){
        SQLiteDatabase sqLiteDatabase = audioDB.getWritableDatabase();
        sqLiteDatabase.beginTransaction();
        try {
            sqLiteDatabase.delete(SongColumn.NAME, null, null);
        }finally {
            sqLiteDatabase.endTransaction();
        }
        int PROGRESS_NUM = 20;
        int position = 0;
        while (position < list.size()){

            sqLiteDatabase.beginTransaction();
            try {
                for (int i = 0; i < list.size() && i < list.size() + PROGRESS_NUM; i++) {
                    PlayBackTrack track = list.get(i);
                    ContentValues values = new ContentValues(4);
                    values.put(SongColumn.TRACK_ID, track.mId);
                    values.put(SongColumn.SOURCE_ID, track.sourceId);
                    values.put(SongColumn.SOURCE_TYPE, track.mIdType.mId);
                    values.put(SongColumn.SOURCE_POSITION, track.mCurrentPos);
                    sqLiteDatabase.insert(SongColumn.NAME, null, values);
                }
                sqLiteDatabase.setTransactionSuccessful();
            }finally {
                sqLiteDatabase.endTransaction();
                position += PROGRESS_NUM;
            }
        }
    }

    public ArrayList<PlayBackTrack> getSongToDb(){
        ArrayList<PlayBackTrack> result = new ArrayList<>();
        Cursor cursor = audioDB.getReadableDatabase().query(SongColumn.NAME, null, null, null, null, null ,null );
        try {
            if(cursor != null && cursor.moveToFirst()){
                result.ensureCapacity(cursor.getCount());
                do {
                    result.add(new PlayBackTrack(
                            cursor.getLong(0),
                            cursor.getLong(1),
                            MyUtil.IdType.getInstance(cursor.getInt(2)),
                            cursor.getInt(3)));
                }while (cursor.moveToNext());
            }
            return result;
        } finally {
            if(cursor != null){
                cursor.close();
                cursor = null;
            }
        }

    }

    public static class SongColumn {
        public static String NAME = "playbacktrack";
        public static String TRACK_ID = "trackid";
        public static String SOURCE_ID = "sourceid";
        public static String SOURCE_TYPE = "sourcetype";
        public static String SOURCE_POSITION = "sourceposition";
    }
}
