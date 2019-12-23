package com.example.soyoung.ssomp3player;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "musicDB";
    private static final int VERSION = 1;

    // 데이터베이스 생성
    public MyDBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    // 테이블 생성
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String str = "CREATE TABLE musicTBL (" +
                "title CHAR(40) PRIMARY KEY," +
                "singer CHAR(20)," +
                "genre CHAR(20)," +
                "score CHAR(10)," +
                "album VARCHAR(200));";
        sqLiteDatabase.execSQL(str);
    }

    // 테이블 삭제 후 다시 생성
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS musicTBL");
        onCreate(sqLiteDatabase);
    }
}
