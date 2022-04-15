package com.zuescoder69.wordle.userData;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.zuescoder69.wordle.params.Params;

public class DbHandler extends SQLiteOpenHelper {

    Context context;

    public DbHandler(Context context) {
        super(context, Params.DB_NAME, null, Params.DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createDailyGameModeTable = "CREATE TABLE " + Params.PREVIOUS_DAILY_GAME_TABLE
                + "("
                + Params.KEY_ROW + " INTEGER PRIMARY KEY,"
                + Params.KEY_LETTER1 + " TEXT,"
                + Params.KEY_LETTER2 + " TEXT,"
                + Params.KEY_LETTER3 + " TEXT,"
                + Params.KEY_LETTER4 + " TEXT,"
                + Params.KEY_LETTER5 + " TEXT"
                + ")";
        db.execSQL(createDailyGameModeTable);

        String createClassicGameModeTable = "CREATE TABLE " + Params.PREVIOUS_CLASSIC_GAME_TABLE
                + "("
                + Params.KEY_ROW + " INTEGER PRIMARY KEY,"
                + Params.KEY_LETTER1 + " TEXT,"
                + Params.KEY_LETTER2 + " TEXT,"
                + Params.KEY_LETTER3 + " TEXT,"
                + Params.KEY_LETTER4 + " TEXT,"
                + Params.KEY_LETTER5 + " TEXT"
                + ")";
        db.execSQL(createClassicGameModeTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + Params.PREVIOUS_DAILY_GAME_TABLE);
        onCreate(db);
    }

    /**
     * Adding notes in the notes table
     */
    public void addRow(Integer row, String letter1, String letter2, String letter3, String letter4, String letter5, String gameMode) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Params.KEY_ROW, row);
        values.put(Params.KEY_LETTER1, letter1);
        values.put(Params.KEY_LETTER2, letter2);
        values.put(Params.KEY_LETTER3, letter3);
        values.put(Params.KEY_LETTER4, letter4);
        values.put(Params.KEY_LETTER5, letter5);
        long result = db.insert(gameMode, null, values);
        if (result == -1) {
            Log.d("DbHandler", "Row Not Added");
        } else {
            Log.d("DbHandler", "Row Added");
        }
    }

    /**
     * Getting all the notes of a particular user based on their email id and ordering them by date
     */
    public Cursor readRowFromDB(Integer row, String gameMode) {
        String query = "SELECT * FROM "
                + gameMode
                + " WHERE "
                + Params.KEY_ROW
                + " ="
                + "'" + row + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    public void dropTable(String gameMode) {
        SQLiteDatabase db=this.getWritableDatabase();
        String query="DELETE FROM "
                + gameMode;

        db.execSQL(query);
    }
}
