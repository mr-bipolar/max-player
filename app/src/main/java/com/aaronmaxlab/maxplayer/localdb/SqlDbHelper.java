package com.aaronmaxlab.maxplayer.localdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.aaronmaxlab.maxplayer.models.PlaylistModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SqlDbHelper extends SQLiteOpenHelper {

    private final Context context;
    private static final String DatabaseName = "m3u8Library.db";
    private static final int DatabaseVersion = 1;
    private static final String TableName = "channels";
    private static final String TableCatName = "category";
    private static final String ColumnId = "id";
    private static final String ColumnTitle = "channelName";
    private static final String ColumnIcon = "channelIcon";
    private static final String ColumnUrl = "channelUrl";
    private static final String ColumnCategory = "category";
    private static final String TablePlaylist = "playlist";
    private static final String PlaylistName = "playlistName";
    private static final String PlaylistUrl = "playlistUrl";
    private static final String PlaylistCount = "playlistCount";



     public SqlDbHelper(@Nullable Context context) {
        super(context, DatabaseName, null, DatabaseVersion);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
         String query  = " CREATE TABLE if NOT EXISTS "+
                 TableName        + " ( "     +
                 ColumnId         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                 ColumnTitle      + " TEXT, " +
                 ColumnIcon       + " TEXT, " +
                 ColumnUrl        + " TEXT, " +
                 ColumnCategory   + " TEXT);";
         database.execSQL(query);

        String catQuery  = " CREATE TABLE if NOT EXISTS "+
                TableCatName     + " ( "     +
                ColumnId         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ColumnCategory   + " TEXT);";
        database.execSQL(catQuery);

        String playlistQuery = " CREATE TABLE if NOT EXISTS "+
                TablePlaylist    + " ( " +
                ColumnId         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PlaylistName     + " TEXT, " +
                PlaylistUrl      + " TEXT UNIQUE, " +
                PlaylistCount    + " INTEGER);";
        database.execSQL(playlistQuery);

        String catInsert = "INSERT INTO "+
                TableCatName + "(" +
                ColumnCategory + ") VALUES"
                + "('Animation'),"
                + "('Comedy'),"
                + "('Cooking'),"
                + "('Documentary'),"
                + "('Education'),"
                + "('Entertainment'),"
                + "('General'),"
                + "('Kids'),"
                + "('Movies'),"
                + "('Music'),"
                + "('News'),"
                + "('Religious'),"
                + "('Series'),"
                + "('Sports'),"
                + "('Travel'),"
                + "('Weather'),"
                + "('18+'),"
                + "('Undefined')";
        database.execSQL(catInsert);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int i, int i1) {
        database.execSQL("DROP TABLE IF EXISTS " + TableName);
        onCreate(database);
    }

   public boolean addChannel(String title, String icon, String url, String category){
        try (SQLiteDatabase database = this.getWritableDatabase()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ColumnTitle, title);
            contentValues.put(ColumnIcon, icon);
            contentValues.put(ColumnUrl, url);
            contentValues.put(ColumnCategory, category);

            long result = database.insert(TableName, null, contentValues);

            return  result != -1;

        }

    }

    void addCategory(String cName){
        long result;
        try (SQLiteDatabase database = this.getWritableDatabase()) {
            ContentValues cv = new ContentValues();
            cv.put(ColumnCategory, cName);

            result = database.insert(TableCatName, null, cv);
        }
        if (result == -1) {
            Toast.makeText(context, "Submit Failed..", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(context, "Successfully Added", Toast.LENGTH_SHORT).show();
        }
    }

    public Cursor readAllData(){
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            String query = "SELECT * FROM " + TableName;
            database = this.getReadableDatabase();
        if (database != null){
            cursor = database.rawQuery(query, null);
        }
        }catch (SQLiteException e) {
            e.printStackTrace();
        }
        return cursor;
    }

    Cursor readCatData() {
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            String query = "SELECT * FROM " + TableCatName;
            database = this.getReadableDatabase();

            if (database != null) {
                cursor = database.rawQuery(query, null);
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        }

        return cursor;
    }


    // channel data
    public List<String[]> readChannelsData() {
        List<String[]> channels = new ArrayList<>();
        String query = "SELECT * FROM " + TableName;

        try (SQLiteDatabase database = this.getReadableDatabase()) {
            try (Cursor cursor = database.rawQuery(query, null)) {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        channels.add(new String[]{
                                cursor.getString(0),
                                cursor.getString(1),
                                cursor.getString(2),
                                cursor.getString(3),
                                cursor.getString(4)});
                    }
                }
            }
        } catch (Exception e) {
            Log.d("READ CHANNEL DATA", Objects.requireNonNull(e.getMessage()));
        }

        return channels.isEmpty() ? null : channels; // Return null if no channels were found
    }



    void updateChannel(String chId, String title, String icon, String url, String category){
        long result;
        try (SQLiteDatabase database = this.getWritableDatabase()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ColumnTitle, title);
            contentValues.put(ColumnIcon, icon);
            contentValues.put(ColumnUrl, url);
            contentValues.put(ColumnCategory, category);

            result = database.update(TableName, contentValues, "id=?", new String[]{chId});
        }

        if (result == -1){
             Toast.makeText(context, "Update Failed...", Toast.LENGTH_SHORT).show();
         }else{
             Toast.makeText(context, "Successfully Updated...", Toast.LENGTH_SHORT).show();
         }
    }

   public   void deleteChannel(String chId){
        long result;
        try (SQLiteDatabase database = this.getWritableDatabase()) {
            result = database.delete(TableName, "channelUrl=?", new String[]{chId});
        }

        if (result == -1){
             Toast.makeText(context, "Failed to Delete...", Toast.LENGTH_SHORT).show();
         }else{
             Toast.makeText(context, "Successfully Deleted...", Toast.LENGTH_SHORT).show();
         }
    }

    void updatePlaylist(String catId, String catName){
        long result;
        try (SQLiteDatabase database = this.getWritableDatabase()) {
            ContentValues cv = new ContentValues();
            cv.put(ColumnCategory, catName);

            result = database.update(TableCatName, cv, "id=?", new String[]{catId});
        }

        if (result == -1){
            Toast.makeText(context, "Update Failed...", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "Successfully Updated...", Toast.LENGTH_SHORT).show();
        }
    }

    void  deletePlaylist(String catId){
        long result;
        try (SQLiteDatabase database = this.getWritableDatabase()) {
            result = database.delete(TableCatName, "id=?", new String[]{catId});
        }
        if (result == -1){
            Toast.makeText(context, "Failed to Delete...", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "Successfully Deleted...", Toast.LENGTH_SHORT).show();
        }
    }

    public List<PlaylistModel> getAllPlaylists() {

        List<PlaylistModel> playlistList = new ArrayList<>();

        try (SQLiteDatabase database = this.getReadableDatabase();
             Cursor cursor = database.query(
                     TablePlaylist,
                     null,
                     null,
                     null,
                     null,
                     null,
                     null)) {

            if (cursor != null && cursor.moveToFirst()) {
                do {

                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(ColumnId));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(PlaylistName));
                    String url = cursor.getString(cursor.getColumnIndexOrThrow(PlaylistUrl));
                    int playlistCount = cursor.getInt(cursor.getColumnIndexOrThrow(PlaylistCount));

                    playlistList.add(new PlaylistModel(id, name, url, playlistCount));

                } while (cursor.moveToNext());
            }
        }

        return playlistList;
    }

    public boolean addM3uPlaylist(String playlistName, int playlistCount, String playlistUrl) {

        try (SQLiteDatabase database = this.getWritableDatabase()) {

            ContentValues cv = new ContentValues();
            cv.put(PlaylistName, playlistName);
            cv.put(PlaylistUrl, playlistUrl);
            cv.put(PlaylistCount, playlistCount);

            long result = database.insert(TablePlaylist, null, cv);

            return result != -1;
        }
    }

    public void updateM3uPlaylist(String oldUrl, String newName, String newUrl) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues cv = new ContentValues();
            cv.put(PlaylistName, newName);
            cv.put(PlaylistUrl, newUrl);
            db.update(TablePlaylist, cv, "playlistUrl=?", new String[]{oldUrl});

        }
    }

    public boolean deleteM3uPlaylist(String playlistUrl) {
        try (SQLiteDatabase database = this.getWritableDatabase()) {
            int rowsDeleted = database.delete(TablePlaylist, "playlistUrl=?", new String[]{playlistUrl});
            return rowsDeleted > 0;
        }
    }



}
