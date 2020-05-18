package com.grey.gxplayer.dataloader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.grey.gxplayer.models.Artists;

import java.util.ArrayList;
import java.util.List;

public class ArtistLoader {

    public List<Artists> getArtists(Context context, Cursor cursor){

        List<Artists> list = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(new Artists(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getInt(3)));
            } while (cursor.moveToNext());
            if (cursor != null) {
                cursor.close();
            }
        }

        return list;
    }

    public Artists getArtist(Context context, long id){
        return artists(makeCursor(context, "_id=?", new String[]{String.valueOf(id)}));
    }

    private Artists artists(Cursor cursor) {
        Artists artists = new Artists();
        if(cursor.moveToFirst() && cursor != null){
            artists = new Artists(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getInt(2),
                    cursor.getInt(3));
        }
        if (cursor != null){
            cursor.close();
        }
        return artists;
    }

    public List<Artists> artistsList(Context context){
        return getArtists(context, makeCursor(context, null, null));
    }

    public Cursor makeCursor(Context context, String selection, String[] selectionArg){

        Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{
                MediaStore.Audio.Artists._ID, //0
                MediaStore.Audio.Artists.ARTIST, //1
                MediaStore.Audio.Artists.NUMBER_OF_ALBUMS, //2
                MediaStore.Audio.Artists.NUMBER_OF_TRACKS, //3
        };
        String sortOrder = MediaStore.Audio.Artists.DEFAULT_SORT_ORDER;
        Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArg, sortOrder);
        return cursor;
    }
}
