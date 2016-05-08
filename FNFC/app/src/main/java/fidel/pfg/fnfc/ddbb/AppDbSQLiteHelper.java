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
        // initDefaultEntries(db);
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
                "   set_q INTEGER," +
                "   val TEXT," +
                "   FOREIGN KEY(user_id) REFERENCES Users(user_id) ON DELETE CASCADE" +
                ");";
        String table3 = "CREATE TABLE EllipticCurves(" +
                "   ec_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "   name TEXT," +
                "   k TEXT" +
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
        newEntry.put("name", "c2pnb163v1");
        newEntry.put("k", "838828326113658401440043399564525405856963575389");
        db.insert("EllipticCurves", null, newEntry);

        // 1º
        newEntry = new ContentValues();
        newEntry.put("user_id", "AAA1");
        newEntry.put("name", "All At All");
        db.insert("Users", null, newEntry);

        newEntry = new ContentValues();
        newEntry.put("key_id", 0);
        newEntry.put("user_id", "AAA1");
        newEntry.put("q", 12);
        newEntry.put("set_q", 12);
        newEntry.put("p", "751012021171870452122918090790420094175625927151260");
        newEntry.put("val", "756620029338740724273078556156344245242171310654053");
        // Correct NFC-T value QUFBMSw3NDg4NTc2MzQ1OTgzMjQ0MDI3NzU0NjU0OTE2MzU3MDU1MjM4MDk2NjUyNTIyODE0Nzk=
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
        newEntry.put("set_q", 11);
        newEntry.put("p", "749625052286325696773395092432110531154728259913960");
        newEntry.put("val", "748604342202344059562009720188517464986466483459791");
        // Correct NFC-T value QkJCMSwxMTMzMjMyNTA1ODA1OTE3MTQ5ODk2ODk2MDMxNDg3NjQ3NDY0OTM2NjE3NzQxMjYxNjEw
        db.insert("Keys", null, newEntry);

        // 3º
        newEntry = new ContentValues();
        newEntry.put("user_id", "CCC1");
        newEntry.put("name", "Clear Clean Clear");
        db.insert("Users", null, newEntry);

        newEntry = new ContentValues();
        newEntry.put("key_id", 2);
        newEntry.put("user_id", "CCC1");
        newEntry.put("q", 10);
        newEntry.put("set_q", 10);
        newEntry.put("p", "1133766180472940559559092094346106918804428817663744");
        newEntry.put("val", "750958089461982330060227610117491527585509164263455");
        // Correct NFC-T value Q0NDMSwxMTI1Mjc1NzQwMzU1NDkwNzA0ODU0ODI4MDg5MzI1NDY4MTU3NDg3NDIwMjA3Mzk1NTkx
        db.insert("Keys", null, newEntry);

        // 4º
        newEntry = new ContentValues();
        newEntry.put("user_id", "DDD1");
        newEntry.put("name", "Do Did Dot");
        db.insert("Users", null, newEntry);

        newEntry = new ContentValues();
        newEntry.put("key_id", 3);
        newEntry.put("user_id", "DDD1");
        newEntry.put("q", 4);
        newEntry.put("set_q", 4);
        newEntry.put("p", "1131939708685729064218967552048319698271733323874032");
        newEntry.put("val", "1129524977008033452470965922928541402746439226182454");
        // Correct NFC-T value REREMSwxMTI4OTcwMDMwMDc0ODcyNTM2NTQxOTE4MDE1NjA1Nzk1OTY0MTc0Nzk3NTcxMTU4NTc4
        db.insert("Keys", null, newEntry);

        // 5º
        newEntry = new ContentValues();
        newEntry.put("user_id", "FFF1");
        newEntry.put("name", "Far Fast Far");
        db.insert("Users", null, newEntry);

        newEntry = new ContentValues();
        newEntry.put("key_id", 4);
        newEntry.put("user_id", "FFF1");
        newEntry.put("q", 3);
        newEntry.put("set_q", 3);
        newEntry.put("p", "1130691625499494260672214820340173855464238755368475");
        newEntry.put("val", "757384461553174506009490769200927317615180960694701");
        // Correct NFC-T value RkZGMSwxMTI0NjM1MDYxNzA4NzE2MjU4MTg4NjMwNDM2NTc5OTc3ODY3MTM5MTAzNDczNDQ0MDM3
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
        String query = "SELECT Users.name, Users.user_id, Keys.p, Keys.q, Keys.set_q, Keys.val FROM Keys, Users WHERE Users.user_id=Keys.user_id";
        return db.rawQuery(query,null);
    }

    public static Cursor getUserInfo(SQLiteDatabase db, String userId){
        String query = "SELECT Keys.p, Keys.q, Keys.set_q, Keys.val FROM Keys WHERE Keys.user_id = \"" + userId+"\"";
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

    public static void deleteUserFromDB(SQLiteDatabase db, String userID) {
        db.delete("Keys", "user_id = ?", new String[] { userID });
    }

    public static void prepareNewSystem(SQLiteDatabase db) {
        db.delete("Keys", null, null);
        db.delete("EllipticCurves", null, null);
    }

    public static void setNewSystem(SQLiteDatabase db, String... values){
        ContentValues newEntry = new ContentValues();
        newEntry.put("name", values[0]);
        newEntry.put("k", values[1]);
        db.insert("EllipticCurves", null, newEntry);
    }

    public static void registerNewUser(SQLiteDatabase db, String[] values) {
        ContentValues newEntry = new ContentValues();
        newEntry.put("user_id", values[0]);
        newEntry.put("p",  values[1]);
        newEntry.put("q",  values[2]);
        newEntry.put("set_q",  values[3]);
        newEntry.put("val",  values[4]);
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