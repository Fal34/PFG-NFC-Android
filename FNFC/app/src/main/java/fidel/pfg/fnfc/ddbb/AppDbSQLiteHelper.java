package fidel.pfg.fnfc.ddbb;

/**
 * Created by fidel on 21/04/2016.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class AppDbSQLiteHelper extends SQLiteOpenHelper {

    public AppDbSQLiteHelper (Context context, String name,
                              CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("AppDbSQLiteHelper", "DB Creation!");
        //initDefaultEntries(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        Log.i("AppDbSQLiteHelper", "DB Open!");
        // Uncomment when db update
        initDefaultEntries(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnterior,
                          int versionNueva) {
        Log.i("AppDbSQLiteHelper","DB Update!");
        //NOTA: Por simplicidad del ejemplo aquí utilizamos directamente
        //      la opción de eliminar la tabla anterior y crearla de nuevo
        //      vacía con el nuevo formato.
        //      Sin embargo lo normal será que haya que migrar datos de la
        //      tabla antigua a la nueva, por lo que este método debería
        //      ser más elaborado.

        //Se elimina la versión anterior de la tabla
        db.execSQL("DROP TABLE IF EXISTS Keys;");
        db.execSQL("DROP TABLE IF EXISTS Users;");
        db.execSQL("DROP TABLE IF EXISTS EllipticCurves;");

        //Se crea la nueva versión de la tabla
        initDefaultEntries(db);
    }

    public static void initDefaultEntries(SQLiteDatabase db){
        //Sentence SQL for Users
        String table1 = "CREATE TABLE Users(" +
                "   user_id TEXT PRIMARY KEY," +
                "   name TEXT" +
                ");";
        String table2 = "CREATE TABLE Keys(" +
                "   key_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "   user_id TEXT," +
                "   p TEXT," +
                "   q INTEGER," +
                "   val TEXT," +
                "   FOREIGN KEY(user_id) REFERENCES Users(user_id) ON DELETE CASCADE" +
                ");";
        String table3 = "CREATE TABLE EllipticCurves(" +
                "   ec_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "   field TEXT," +
                "   a TEXT," +
                "   b TEXT," +
                "   k TEXT," +
                "   r TEXT," +
                "   seed TEXT" +
                ");";
        String sqlCreate = table1+ table2 + table3;

        Log.i("AppDbSQLiteHelper", "Creación de tablas + Mock!");

        db.execSQL("PRAGMA foreign_keys=ON");
        db.execSQL("DROP TABLE IF EXISTS Keys;");
        db.execSQL("DROP TABLE IF EXISTS Users;");
        db.execSQL("DROP TABLE IF EXISTS EllipticCurves;");
        db.execSQL(table1);
        db.execSQL(table2);
        db.execSQL(table3);

        db.execSQL("CREATE INDEX user_id_index ON Keys(user_id);");

        // Initial Default Mock
        ContentValues newEntry;

        // EC
        newEntry = new ContentValues();
        newEntry.put("ec_id", 0);
        newEntry.put("field", "25");
        newEntry.put("a", "1");
        newEntry.put("b", "2");
        newEntry.put("k", "33333");
        newEntry.put("r", "44444");
        newEntry.put("seed", "123");
        db.insert("EllipticCurves", null, newEntry);

        // 1º
        newEntry = new ContentValues();
        newEntry.put("user_id", "AAA1");
        newEntry.put("name", "All At All");
        db.insert("Users", null, newEntry);

        newEntry = new ContentValues();
        newEntry.put("key_id", 0);
        newEntry.put("user_id", "AAA1");
        newEntry.put("q", 10);
        newEntry.put("p", "AAAAAAAAAAAAAAAAAA");
        newEntry.put("val", "asdj90TSk6JOS$jks54&JK$5345");
        db.insert("Keys", null, newEntry);

        // 2º
        newEntry = new ContentValues();
        newEntry.put("user_id", "BBB1");
        newEntry.put("name", "Bit Byte Bit");
        db.insert("Users", null, newEntry);

        newEntry = new ContentValues();
        newEntry.put("key_id", 1);
        newEntry.put("user_id", "BBB1");
        newEntry.put("q", 11);
        newEntry.put("p", "BBBBBBBBBBBBBBBBB");
        newEntry.put("val", "bsadaarrsds4544132&JK$5345");
        db.insert("Keys", null, newEntry);

        // 3º
        newEntry = new ContentValues();
        newEntry.put("user_id", "CCC1");
        newEntry.put("name", "Clear Clean Clear");
        db.insert("Users", null, newEntry);

        newEntry = new ContentValues();
        newEntry.put("key_id", 2);
        newEntry.put("user_id", "CCC1");
        newEntry.put("q", 12);
        newEntry.put("p", "CCCCCCCCCCCCCCCCC");
        newEntry.put("val", "csdjasd354AGAR344123fh6dfgDF");
        db.insert("Keys", null, newEntry);

        // 4º
        newEntry = new ContentValues();
        newEntry.put("user_id", "DDD1");
        newEntry.put("name", "Do Did Dot");
        db.insert("Users", null, newEntry);

        newEntry = new ContentValues();
        newEntry.put("key_id", 3);
        newEntry.put("user_id", "DDD1");
        newEntry.put("q", 2);
        newEntry.put("p", "DDDDDDDDDDDDDDD");
        newEntry.put("val", "aisjdoij4oijRJPOIJkjmsasd");
        db.insert("Keys", null, newEntry);

        // 5º
        newEntry = new ContentValues();
        newEntry.put("user_id", "FFF1");
        newEntry.put("name", "Far Fast Far");
        db.insert("Users", null, newEntry);

        newEntry = new ContentValues();
        newEntry.put("key_id", 4);
        newEntry.put("user_id", "FFF1");
        newEntry.put("q", 2);
        newEntry.put("p", "FFFFFFFFFFFFFFF");
        newEntry.put("val", "sfeGDDIGOKDgdLGJSJEasd256184");
        db.insert("Keys", null, newEntry);

        // 6º Users no registered
        newEntry = new ContentValues();
        newEntry.put("user_id", "GGG1");
        newEntry.put("name", "Go Give Go");
        db.insert("Users", null, newEntry);

        // 7º Users no registered
        newEntry = new ContentValues();
        newEntry.put("user_id", "HHH1");
        newEntry.put("name", "High Hi High");
        db.insert("Users", null, newEntry);
    }

    public static Cursor getUsersInfo(SQLiteDatabase db){
        String query = "SELECT Users.name, Users.user_id, Keys.p, Keys.q, Keys.val FROM Keys, Users WHERE Users.user_id=Keys.user_id";
        return db.rawQuery(query,null);
    }

    public static Cursor getUserInfo(SQLiteDatabase db, String userId){
        String query = "SELECT Keys.p, Keys.q, Keys.val FROM Keys WHERE Keys.user_id = \"" + userId+"\"";
        return db.rawQuery(query,null);
    }

    public static Cursor getUsersNotInSystem(SQLiteDatabase db){
        String query = "SELECT * FROM Users LEFT JOIN Keys ON Keys.user_id = Users.user_id " +
                "WHERE Keys.user_id IS NULL";
        return db.rawQuery(query,null);
    }

    public static Cursor getECValues(SQLiteDatabase db){
        String query = "SELECT * FROM EllipticCurves LIMIT 1";
        return db.rawQuery(query,null);
    }

    public static void prepareNewSystem(SQLiteDatabase db) {
        db.delete("Keys", null, null);
        db.delete("EllipticCurves", null, null);
    }

    public static void setNewSystem(SQLiteDatabase db, String... values){
        ContentValues newEntry = new ContentValues();
        newEntry.put("field", values[0]);
        newEntry.put("a",  values[1]);
        newEntry.put("b",  values[2]);
        newEntry.put("k",  values[3]);
        newEntry.put("r",  values[4]);
        newEntry.put("seed", values[5]);
        db.insert("EllipticCurves", null, newEntry);
    }

    public static void registerNewUser(SQLiteDatabase db, String[] values) {
        ContentValues newEntry = new ContentValues();
        newEntry.put("user_id", values[0]);
        newEntry.put("p",  values[1]);
        newEntry.put("q",  values[2]);
        newEntry.put("val",  values[3]);
        db.insert("Keys", null, newEntry);
    }

    public static void updateUser(SQLiteDatabase db, String[] userInfo) {
        ContentValues newEntry = new ContentValues();
        newEntry.put("q",  userInfo[2]);
        newEntry.put("val",  userInfo[3]);
        db.update("Keys", newEntry, "user_id=\"" + userInfo[0] + "\"", null);
    }

    public static String getUserIdFromName(SQLiteDatabase db, String userId) {

        String query = "SELECT Users.user_id FROM Users WHERE Users.name=\""+userId+"\"";
        Cursor result = db.rawQuery(query,null);
        if(!result.moveToFirst()){
            return null;
        }

        return result.getString(result
                .getColumnIndex("user_id"));
    }
}