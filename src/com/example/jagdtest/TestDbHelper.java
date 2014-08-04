package com.example.jagdtest;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

public class TestDbHelper extends SQLiteOpenHelper {
    
    public static abstract class DbEn implements BaseColumns {
        public static final String TABLE_TQUESTION = "TQuestion";
        public static final String CN_NUM = "id_number";
        public static final String CN_ID_CATEG = "id_categ";
        public static final String CN_QUESTION = "question";
        public static final String CN_Q_TYPE = "question_type";
        public static final String CN_POINTS = "points";
        public static final String CN_EXT_POINTS = "extra_points";
        public static final String CN_STATUS = "status";
        public static final String CN_ID_EXP = "id_exp";

        public static final String TABLE_TEXP = "TExp";
        public static final String CN_EXPLANATION = "explanation";

        public static final String TABLE_TCATEG = "TCategory";
        public static final String CN_TIME_LIM = "time_lim_sec";
        public static final String CN_CATEGORY = "category";

        public static final String TABLE_TANSWER = "TAnswer";
        public static final String CN_ID_Q = "id_q";
        public static final String CN_ANSWER = "answer";
        public static final String CN_IS_ANSWER = "is_answ";
    }
    
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Jagd.db";
/*
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";*/
    private static final String COMMA_SEP = ",";
    private Context mContext;
    private SharedPreferences sp;
    private String DB_PATH;
    /*  CREATE TABLE TExp (_id INTEGER PRIMARY KEY AUTOINCREMENT,
        explanation TEXT NOT NULL)
        
        CREATE TABLE TCategory (_id INTEGER PRIMARY KEY AUTOINCREMENT,
        time_lim_sec INTEGER DEFAULT 0,
        category TEXT NOT NULL)
        
    CREATE TABLE TAnswer (_id INTEGER PRIMARY KEY AUTOINCREMENT,
        id_q INTEGER NOT NULL,
        answer TEXT NOT NULL,
        is_answ INTEGER DEFAULT 0,
    FOREIGN KEY(id_q) REFERENCES TQuestion(_id))
    
    
CREATE TABLE TQuestion (_id INTEGER PRIMARY KEY AUTOINCREMENT,
        id_number INTEGER,
        id_categ INTEGER NOT NULL,
        question INTEGER NOT NULL,
        question_type INTEGER NOT NULL,
        points INTEGER NOT NULL,
        extra_points INTEGER DEFAULT 0,
        status INTEGER DEFAULT 0,
        id_exp INTEGER,
    FOREIGN KEY(id_categ) REFERENCES TCategory(_id),
    FOREIGN KEY(id_exp) REFERENCES TExp(_id))
        */
    
/*    private static final String SQL_CREATE_TQUESTION =
        "CREATE TABLE " + DbEn.TABLE_TQUESTION + " (" +
        DbEn._ID + INT_TYPE + " PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
        DbEn.CN_NUM + INT_TYPE + " NOT NULL" + COMMA_SEP +
        DbEn.CN_ID_CATEG + INT_TYPE + " NOT NULL FOREIGN KEY(" + DbEn.CN_ID_CATEG + ") REFERENCES " +
                DbEn.TABLE_TCATEG + "(_id)" + COMMA_SEP +
        DbEn.CN_QUESTION + TEXT_TYPE + " NOT NULL" + COMMA_SEP +
        DbEn.CN_Q_TYPE + INT_TYPE + " DEFAULT 0" + COMMA_SEP +
        DbEn.CN_POINTS + INT_TYPE + " NOT NULL" + COMMA_SEP +
        DbEn.CN_EXT_POINTS + INT_TYPE + " DEFAULT 0 " + COMMA_SEP +
        DbEn.CN_STATUS + INT_TYPE + " DEFAULT 0 " + COMMA_SEP +
        DbEn.CN_ID_EXP + INT_TYPE + " FOREIGN KEY(" + DbEn.CN_ID_EXP + ") REFERENCES " +
                DbEn.TABLE_TEXP + "(_id))";

    private static final String SQL_CREATE_TEXP = 
        "CREATE TABLE " + DbEn.TABLE_TEXP + " (" +
        DbEn._ID + INT_TYPE + " PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
        DbEn.CN_EXPLANATION + TEXT_TYPE + " NOT NULL)";

    private static final String SQL_CREATE_TCATEGORY = 
        "CREATE TABLE " + DbEn.TABLE_TCATEG + " (" +
            DbEn._ID + INT_TYPE + " PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
            DbEn.CN_TIME_LIM + INT_TYPE + " DEFAULT 0" + COMMA_SEP +
            DbEn.CN_CATEGORY + TEXT_TYPE + " NOT NULL" + ")";

    private static final String SQL_CREATE_TANSWER = 
        "CREATE TABLE " + DbEn.TABLE_TANSWER + " (" +
            DbEn._ID + INT_TYPE + " PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
            DbEn.CN_ID_Q + INT_TYPE + " NOT NULL FOREIGN KEY(" + DbEn.CN_ID_Q + ") REFERENCES " +
                    DbEn.TABLE_TQUESTION + "(_id)" + COMMA_SEP +
            DbEn.CN_ANSWER + TEXT_TYPE + " NOT NULL" + COMMA_SEP +
            DbEn.CN_IS_ANSWER + INT_TYPE + " DEFAULT 0)";
*/
    public TestDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
        sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        String destPath = mContext.getFilesDir().getPath();
        destPath = destPath.substring(0, destPath.lastIndexOf("/"))
                + "/databases";
        DB_PATH = destPath + "/" + DATABASE_NAME;
    }
    public void onCreate(SQLiteDatabase db) {
    }
    
    public void createDataBase(){
        boolean mDataBaseExist = checkDataBase();
        if(!mDataBaseExist) {
            this.getReadableDatabase();
            this.close();
            try{
                copyDataBase();
            } catch (Exception e) {
                Log.e(e.getMessage(),e.getLocalizedMessage());
            }
        }
    }
        //Check that the database exists here: /data/data/your package/databases/DbName
        private boolean checkDataBase() {
            SQLiteDatabase checkDB = null;
            try{
              checkDB = SQLiteDatabase.openDatabase(DB_PATH,
                  null, SQLiteDatabase.OPEN_READONLY);
            }catch(SQLiteException e){
                Log.e(e.getLocalizedMessage(), e.getMessage());
            }
            if(checkDB == null) return false;
            checkDB.close();
            return true;
        }

        //Copy the database from assets
        private void copyDataBase() throws IOException{
            InputStream mInput = mContext.getAssets().open(DATABASE_NAME);
            OutputStream mOutput = new FileOutputStream(DB_PATH);
            byte[] mBuffer = new byte[1024];
            int mLength;
            while ((mLength = mInput.read(mBuffer))>0)
            {
                mOutput.write(mBuffer, 0, mLength);
            }
            mOutput.flush();
            mOutput.close();
            mInput.close();
        }
/*      //Open the database, so we can query it
        public boolean openDataBase() throws SQLException {
            mDataBase = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.CREATE_IF_NECESSARY);
            //mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            return mDataBase != null;
        }
        */
        
        public SQLiteDatabase openDataBase() throws SQLException{
            createDataBase();
            return SQLiteDatabase.openDatabase(DB_PATH,
                    null, SQLiteDatabase.OPEN_READONLY);
        }

    /*private void copyDataBase(){
        String destPath = mContext.getFilesDir().getPath();
        destPath = destPath.substring(0, destPath.lastIndexOf("/"))
                + "/databases";
        String DB_PATH = destPath + "/" + DATABASE_NAME;
        Log.i("Database", "New database is being copied to device!");
        byte[] buffer = new byte[1024];
        OutputStream myOutput = null;
        int length;
        InputStream myInput = null;
        try {
            myInput = mContext.getAssets().open(DATABASE_NAME);
            myOutput = new FileOutputStream(DB_PATH);
            while((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
            myOutput.close();
            myOutput.flush();
            myInput.close();
            Log.i("Database", "New database has been copied to device!");
        } catch(IOException e) {
            Log.i("Database", e.getLocalizedMessage());
        }
    }*/
    
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public SparseArray<String> getAllCategs(SQLiteDatabase db){
        SparseArray<String> sa = new SparseArray<String>();
        String select = "SELECT * FROM " + DbEn.TABLE_TCATEG 
                 + " ORDER BY " + DbEn._ID;
        Cursor cursor = db.rawQuery(select, null);
        if(cursor.getCount() > 0){
            while (cursor.moveToNext()){
                String s = cursor.getString(cursor.getColumnIndex(DbEn.CN_CATEGORY));
                sa.put(cursor.getInt(cursor.getColumnIndex(DbEn._ID)), s);
             }
        }
        cursor.close();
        return sa;
    }

    public SparseIntArray getAllNumsByCateg(SQLiteDatabase db, int categ){
        SparseIntArray sa = new SparseIntArray();
        //int categ = sp.getInt("category", -1);
        String where = " WHERE 1=1";
        if (categ > -1) where += " AND " + DbEn.CN_ID_CATEG + "=" + categ;
        
        String orderBy = " ORDER BY " + DbEn.CN_ID_CATEG + ", " + DbEn.CN_NUM;
        String select = "SELECT * FROM " + DbEn.TABLE_TQUESTION 
                 + where + orderBy;
        Cursor cursor = db.rawQuery(select, null);
        if(cursor.getCount() > 0){
            while (cursor.moveToNext()){
                int num = cursor.getInt(cursor.getColumnIndex(DbEn.CN_NUM));
                sa.put(cursor.getInt(cursor.getColumnIndex(DbEn._ID)), num);
             }
        }
        cursor.close();
        return sa;
    }
    
    public SparseArray<Question> getQuestionsByCateg(SQLiteDatabase db){
        SparseArray<Question> sa = new SparseArray<Question>();
        int categ = sp.getInt("category", -1);
        String where = " WHERE 1=1";
        if (categ > -1) where += " AND " + DbEn.CN_ID_CATEG + "=" + categ;
        
        String orderBy = " ORDER BY " + DbEn.CN_ID_CATEG + COMMA_SEP +
                DbEn.CN_NUM;
        String select = "SELECT * FROM " + DbEn.TABLE_TQUESTION 
                 + where + orderBy;
        Cursor cursor = db.rawQuery(select, null);
        if(cursor.getCount() > 0){
            while (cursor.moveToNext()){
                int _id = cursor.getInt(cursor.getColumnIndex(DbEn._ID));
                sa.put(_id, loadQuestion(db, cursor));
             }
        }
        cursor.close();
        return sa;
    }
    
    public HashSet<Answer> getAnswersByQId(SQLiteDatabase db, int _id){
        HashSet<Answer> ha = new HashSet<Answer>();
        String sql = "SELECT * FROM " + DbEn.TABLE_TANSWER 
                + " where " + DbEn.CN_ID_Q + "=" + _id;
        Cursor ca = db.rawQuery(sql, null);
        while (ca.moveToNext()){
            int idQ = ca.getInt(ca.getColumnIndex(DbEn.CN_ID_Q));
            int isA = ca.getInt(ca.getColumnIndex(DbEn.CN_IS_ANSWER));
            String answ = ca.getString(ca.getColumnIndex(DbEn.CN_ANSWER));
            ha.add(new Answer(idQ,answ,((isA == 1) ? true : false)));
        }
        ca.close();
        return ha;
    }
    
    public SparseArray<Question> getQuestionsByCategNum(SQLiteDatabase db){
        SparseArray<Question> sa = new SparseArray<Question>();
        int rand = sp.getInt("random", 0);
        int numb = sp.getInt("number", 35);
        int categ = sp.getInt("category", -1);//1
        String range = sp.getString("range", "");// 5 AND 55

        String where = " WHERE 1=1";
        if (categ > -1) where += " AND " + DbEn.CN_ID_CATEG + "=" + categ;
        if (rand == 0 && !range.isEmpty()) where += " AND " + DbEn.CN_NUM + " BETWEEN " + range;
        
        String orderBy = " ORDER BY " + DbEn.CN_ID_CATEG + COMMA_SEP +
                DbEn.CN_NUM;
        String select = "SELECT * FROM " + DbEn.TABLE_TQUESTION 
                 + where + orderBy 
                 + ((rand == 1) ? ", RANDOM()" : "")
                 + " LIMIT " + numb;
        Cursor cursor = db.rawQuery(select, null);
        if(cursor.getCount() > 0){
            while (cursor.moveToNext()){
                int _id = cursor.getInt(cursor.getColumnIndex(DbEn._ID));
                sa.put(_id, loadQuestion(db, cursor));
             }
        }
        cursor.close();
        return sa;
    }

    private Question loadQuestion(SQLiteDatabase db, Cursor cursor){
        int _id = cursor.getInt(cursor.getColumnIndex(DbEn._ID));
        int idNumber = cursor.getInt(cursor.getColumnIndex(DbEn.CN_NUM));
        int idCateg = cursor.getInt(cursor.getColumnIndex(DbEn.CN_ID_CATEG));
        String question = cursor.getString(cursor.getColumnIndex(DbEn.CN_QUESTION));
        int questionType = cursor.getInt(cursor.getColumnIndex(DbEn.CN_Q_TYPE));
        int points = cursor.getInt(cursor.getColumnIndex(DbEn.CN_POINTS));
        int extraPoints = cursor.getInt(cursor.getColumnIndex(DbEn.CN_EXT_POINTS));
        int status = cursor.getInt(cursor.getColumnIndex(DbEn.CN_STATUS));
        int idExp = cursor.getInt(cursor.getColumnIndex(DbEn.CN_ID_EXP));
        String explanation = getExplanationById(idExp, db);
        String category = getCategById(idCateg, db);
        Question q = new Question(_id,
                idNumber, idCateg, question,
                questionType, points, extraPoints,
                status, idExp, explanation, category);
        q.setAnswers(getAnswersByQId(db,_id));
        return q;
    }
    
    public String getExplanationById(int id, SQLiteDatabase db){
        String name = "";
        String select = "SELECT * FROM " + DbEn.TABLE_TEXP 
                + " WHERE " + DbEn._ID + "=" + id;
        Cursor cursor = db.rawQuery(select, null);
        if (cursor.moveToFirst()){
            name = cursor.getString(cursor.getColumnIndex(DbEn.CN_EXPLANATION));
        }
        cursor.close();
        return name;
    }

    public String getCategById(int id, SQLiteDatabase db){
        String name = "";
        String select = "SELECT * FROM " + DbEn.TABLE_TCATEG 
                + " WHERE " + DbEn._ID + "=" + id;
        Cursor cursor = db.rawQuery(select, null);
        if (cursor.moveToFirst()){
            name = cursor.getString(cursor.getColumnIndex(DbEn.CN_CATEGORY));
        }
        cursor.close();
        return name;
    }
    
    public long getCategTimeById(int id, SQLiteDatabase db){
        String select = "SELECT * FROM " + DbEn.TABLE_TCATEG 
                + " WHERE " + DbEn._ID + "=" + id;
        Cursor cursor = db.rawQuery(select, null);
        if (cursor.moveToFirst()){
            return cursor.getLong(cursor.getColumnIndex(DbEn.CN_TIME_LIM));
        }
        cursor.close();
        return 0;
    }
    
    public Question saveQuestion(SQLiteDatabase db, Question q){
        ContentValues values = new ContentValues();
        values.put(DbEn.CN_STATUS, q.getStatus());
        String selection = DbEn._ID + " = ?";
        String[] selectionArgs = {String.valueOf(q.getId())};
        db.update(DbEn.TABLE_TQUESTION, values, selection, selectionArgs);
        return q;
    }
    
    public int getRallyId(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sp.getInt("rally", 0);
    }
    public Question getQuestionsById(SQLiteDatabase db, int id) {
       Question q = null;
       String select = "SELECT * FROM " + DbEn.TABLE_TQUESTION 
                + " WHERE " + DbEn._ID + "=" + id;
       Cursor cursor = db.rawQuery(select, null);
       while (cursor.moveToNext()){
           q = loadQuestion(db, cursor);
       }
       cursor.close();
       return q;
    }
}
