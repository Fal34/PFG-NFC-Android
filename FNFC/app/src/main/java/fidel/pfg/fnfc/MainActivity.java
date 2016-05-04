package fidel.pfg.fnfc;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import fidel.pfg.fnfc.core.administration.AdministrationFragment;
import fidel.pfg.fnfc.ddbb.AppDbSQLiteHelper;
import fidel.pfg.fnfc.ecc.AppEllipticCurve;
import fidel.pfg.fnfc.utils.Utils;

import static android.nfc.NdefRecord.createMime;

public class MainActivity extends AppCompatActivity {

    // General Attr
    private Toolbar appbar;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private static SQLiteDatabase db = null;
    private static Context mContext;

    // NFC attr
    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter ndef;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;

    // NFC Write
    private boolean isANewUser = false;
    private String bufferWriteString = null;
    private String dbBufferWriteNewReg = null;

    // EC
    private AppEllipticCurve ecApp;

    // ###### EC Management ######

    /**
     * Makes a new register from a user given by params
     * @param userId
     * @param q
     * @return nfc value to write
     */
    public String makeNewRegisterFromUser(String userId, String q){
        String result = "";
        String p,val,dbVal;

        // Q default value
        if(q == null){
            q = "15";
        }
        userId = AppDbSQLiteHelper.getUserIdFromName(db,userId);
        Log.i("makeNewRegisterFromUser","got true user as: " + userId);
        // Retrive a random point form current EC
        p = this.ecApp.getRandomPoint();

        // Compute (Q)(K.P) for NFC-T
        val = this.ecApp.sumECPoint(Integer.parseInt(q),p);

        // Compute (Q+1)(K.P) for DB: sum P + ( (Q).(K.P) )
        dbVal = this.ecApp.sumECPoint(val,p);

        // Save in db if successfull nfc-write operation
        this.dbBufferWriteNewReg = userId + "," + p + "," + q +","+ dbVal;

        // Make new regiter to write (userId + value of Q.P )
        this.bufferWriteString = result = userId + "," + val;
        this.isANewUser = true;

        Log.i("makeNewRegisterFromUser","with nfc value: " + result);
        Log.i("makeNewRegisterFromUser","with db value: " + this.dbBufferWriteNewReg);
        return result;
    }

    /**
     * True if user is validated, else false
     */
    public boolean validateUser(String userId, String nfcValue){
        Log.i("validateUser","nfc value: " + nfcValue);
        Log.i("validateUser","user id: " + userId);

        String userP=null,userQ=null,userVal=null,nextValue = null;
        // Retrieve user ECPoint from DB
        Cursor cursor = AppDbSQLiteHelper.getUserInfo(db, userId);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                userP = cursor.getString(cursor
                        .getColumnIndex("p"));
                userQ = cursor.getString(cursor
                        .getColumnIndex("q"));
                userVal = cursor.getString(cursor
                        .getColumnIndex("val"));
                cursor.moveToNext();
            }

            Log.i("validateUser", "Usuario en el sistema, comprobación de clave: "+ userVal);

            // Get next value of current nfc value
            try{
                Log.i("validateUser", "#####   Try nextValue set");
                nextValue = this.ecApp.sumECPoint(nfcValue,userP);
            }catch(Exception e){
                e.printStackTrace();
                Log.i("validateUser", "Usuario en el sistema, no se ha podido obtener la siguiente Key");
                return false;
            }

            Log.i("validateUser","[NextVal]-" + nextValue + " [userValue]-" + userVal);
            if(userVal != null && userVal.equals(nextValue)){
                Log.i("validateUser","Usuario validado");

                String nfcNewValue = null;
                int intQ = Integer.parseInt(userQ);
                if(intQ-1 != 0){
                    nfcNewValue = this.ecApp.sumECPoint(Integer.parseInt(userQ)-1,userP);
                }else{
                    // All validations done, new EC Point for user is requiredd
                    // [TODO]
                    // return makeNewRegisterFromUser(userId,null) != null;
                }

                // Generate new info for user to NFC-T
                this.bufferWriteString = userId+","+nfcNewValue;
                Log.i("validateUser","buffer NFC : " + bufferWriteString);
                // Generate new info for user to DB
                this.dbBufferWriteNewReg = userId+","+userP+","+ (intQ-1) +","+nfcValue;
                Log.i("validateUser","buffer DB : " + dbBufferWriteNewReg);
                // Be ready to write in NFC-T and validate
                return true;
            }else{
                Log.i("validateUser", "Usuario en el sistema, Key+1 no coincide con la esperada");
                return false;
            }
        }

        Log.i("validateUser",">>>>>> Usuario no válido <<<<<<<");
        return false;
    }

    /**
     * Load a current EC from DB information
     * @return true if successful load, else otherwise
     */
    public boolean loadCurrentEC(){
        String a = null,b = null;
        int field = -1;
        BigInteger k = null,r = null, seed= null;

        Cursor cursor = AppDbSQLiteHelper.getECValues(db);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                field = Integer.parseInt(cursor.getString(cursor
                        .getColumnIndex("field")));
                a = cursor.getString(cursor
                        .getColumnIndex("a"));
                b = cursor.getString(cursor
                        .getColumnIndex("b"));
                k = new BigInteger(cursor.getString(cursor
                        .getColumnIndex("k")));
                r = new BigInteger(cursor.getString(cursor
                        .getColumnIndex("r")));
                seed = new BigInteger(cursor.getString(cursor
                        .getColumnIndex("seed")));

                cursor.moveToNext();
            }

            if(seed==null){
                return false;
            }

            Log.i("loadCurrentEC",field +" , "+ a +" , "+ b +" , "+ seed.toString()+" | Keys : "+ k.toString() + "," + r.toString());
            this.ecApp.loadCurve(field, a, b, seed.toByteArray());
            this.ecApp.loadKeyPairs(k, r);

            return true;
        }

        return false;
    }

    /**
     * Makes a new EC
     * @param field
     * @param a
     * @param b
     */
    public void newEC(int field, String a, String b){
        // Generate the seed
        Random rand = new Random();
        BigInteger seed = new BigInteger(AppEllipticCurve.DEFAULT_SEED_SIZE, rand);
        this.ecApp = this.ecApp.loadCurve(field, a, b, seed.toByteArray());
        this.ecApp.newKeyPairs();

        // Save result to DB
        Log.i("newEC","Saving result");
        AppDbSQLiteHelper.prepareNewSystem(db);
        AppDbSQLiteHelper.setNewSystem(db, Integer.toString(field), a, b, seed.toString(), this.ecApp.getK().toString(), this.ecApp.getR().toString());

        // BORRAR
        Utils.setToast(this, getResources().getString(R.string.new_ec_success),
                Toast.LENGTH_LONG);
        Log.i("newEC","NEW SYSTEM LOADED");
    }

    // ###### DB Management ######

    /**
     * Load DB
     */
    protected void loadDB(){
        //Abrimos la base de datos 'DBUsuarios' en modo escritura
        AppDbSQLiteHelper usdbh =
                new AppDbSQLiteHelper(this, "AppDb", null, 1);
        db = usdbh.getWritableDatabase();
    }

    /**
     * If a new user registration into the system is sucessfull write to the NFC-T, then is writed into the DB
     */
    public void writeNewRegisterToDB(){
        try{
            AppDbSQLiteHelper.registerNewUser(db, this.dbBufferWriteNewReg.split(","));
            Log.i("writeNewRegisterToDB","Escribiendo los nuevos datos del usuario en la base de datos");
        }catch(Exception e){
            e.printStackTrace();
        }

        // Clear buffers if successful
        this.isANewUser = false;
        this.bufferWriteString = null;
        this.dbBufferWriteNewReg = null;
    }

    /**
     * Update a register from buffer string to DB
     */
    public void updateRegisterToDB(){
        try{
            // From buffer values to db
            AppDbSQLiteHelper.updateUser(db, this.dbBufferWriteNewReg.split(","));
            Log.i("updateRegisterToDB", "Escribiendo los nuevos datos del usuario en la base de datos");
        }catch(Exception e){
            e.printStackTrace();
        }

        // Clear buffers if successful
        this.isANewUser = false;
        this.bufferWriteString = null;
        this.dbBufferWriteNewReg = null;
    }

    /**
     * Gets database connection as static
     * @return sqlite database
     */
    public static SQLiteDatabase getDBCon(){
        return db;
    }

    /**
     * Gets current db
     * @return sqlite database
     */
    protected SQLiteDatabase getDB(){
        return db;
    }

    /**
     * Close current DB
     */
    protected void closeDB(){
        db.close();
        db=null;
    }

    @Override
    protected void onDestroy() {
        this.closeDB();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch(item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appbar = (Toolbar)findViewById(R.id.appbar);
        setSupportActionBar(appbar);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

        // Load DataBase
        this.loadDB();

        // Load Elliptic Curve
        this.ecApp = new AppEllipticCurve();
        if ( this.loadCurrentEC()){
            Log.i("loadCurrentEC","Exito al cargar la curva eliptica");
        }else{
            Log.i("loadCurrentEC","Fallo al cargar la curva eliptica");
        }

        navView = (NavigationView) findViewById(R.id.navview);
        navView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {

                        boolean fragmentTransaction = false;
                        Fragment fragment = null;

                        // Menu selection switch
                        switch (menuItem.getItemId()) {
                            case R.id.menu_home:
                                fragment = new HomepageFragment();
                                fragmentTransaction = true;
                                break;
                            case R.id.menu_about:
                                fragment = new AboutFragment();
                                fragmentTransaction = true;
                                break;
                            case R.id.menu_opcion_register:
                                fragment = new RegisterFragment();
                                fragmentTransaction = true;
                                break;
                            case R.id.menu_deployment:
                                fragment = new AdministrationFragment();
                                fragmentTransaction = true;
                                break;
                            case R.id.menu_opcion_list:
                                fragment = new UsersListFragment();
                                fragmentTransaction = true;
                                break;
                            case R.id.menu_opcion_verify:
                                fragment = new ReadNFCFragment();
                                fragmentTransaction = true;
                                break;
                        }

                        Log.i("NavigationView", "Pulsada Opción: ["+ menuItem.getTitle() + "]");
                        if (fragmentTransaction) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.content_frame, fragment, fragment.getClass().getName())
                                    .commit();

                            //menuItem.setChecked(true);
                            getSupportActionBar().setTitle(menuItem.getTitle());
                        }
                        drawerLayout.closeDrawers();

                        return true;
                    }
                });


        //NFCHandle
        // Check for available NFC Adapter
        mContext = this;
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();

            return;
        }

        if (!mNfcAdapter.isEnabled()) {
            Log.i("nfc endabled?","NFC is disabled.");
        } else {
            Log.i("nfc endabled?", "NFC is enabled.");
        }

        handleIntent(getIntent());
    }
    @Override
    protected void onResume() {
        super.onResume();
        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        stopForegroundDispatch(this, mNfcAdapter);

        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        handleIntent(intent);
    }

    /**
     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        Log.i("setupForegroundDispatch","initialitation");

        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }
        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    /**
     * Disable foregroundDispatch of nfc adapter in activity
     * @param activity
     * @param adapter
     */
    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    private void handleIntent(Intent intent) {
        Log.i("handleIntent", "initialitation");
        String action = intent.getAction();

        // If ACTION_NDEF_DISCOVERED intent
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            // Inflate Fragment if necessary, if required fragment is displayed
            Fragment f = getSupportFragmentManager().findFragmentByTag(ReadNFCFragment.class.getName());
            if (!(f instanceof ReadNFCFragment)) {
                Fragment fragment = new ReadNFCFragment();
                Bundle data = new Bundle();
                data.putBoolean(ReadNFCFragment.TRIGGERED_FROM_NFC, true);
                fragment.setArguments(data);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, fragment, fragment.getClass().getName())
                        .commit();

                drawerLayout.closeDrawers();
            }else{
                ((ReadNFCFragment)f).setTriggeredFromNFC(true);
            }
            // End inflate

            // If mime type is ok then continue
            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask(this).execute(tag);
            } else {
                Log.d(TAG, "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            Log.i("action_tech_discovered","Etiqueta vacía");
            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask(this).execute(tag);
                    break;
                }
            }
        }
    }



    /**
     * Background task for reading the data. Do not block the UI thread while reading.
     * Author Ralf Wondratschek, edited by Fidel Abascal
     *
     */
    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        // Attr
        private MainActivity mainActivity;

        // Constructor with the main activity
        public NdefReaderTask(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            // If empty tag
            if(ndefMessage == null){
                return "";
            }
            NdefRecord[] records = ndefMessage.getRecords();
            String readResult= null;
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        readResult = readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }

            // If has buffered elements to write (writing new nfc content)
            if(mainActivity.bufferWriteString!=null){
                try{
                    writeTag(mainActivity.bufferWriteString, tag);
                    Log.i("readResult", "Escritura de la nueva NFC-T correcta :" + mainActivity.bufferWriteString);

                    return "-1";
                }catch(Exception e){
                    Log.i("readResult" , "Fallo al escribir :"+ mainActivity.dbBufferWriteNewReg);
                }

            }
            // If read and validate, then write
            Log.i("readResult" , "Pre if >> "+ readResult);
            if(readResult!=null){
                Log.i("readResult" , "Post if >> "+ readResult);
                // Prepare to validation NFC-T content
                // [TODO]

                String[] results = readResult.split(",");
                if(results.length!=2){
                    // Not validated
                    // [TODO]
                    Log.i("readResult", "Contenido sin el formato adecuado [" + results.toString() + "] y activity: " + this.mainActivity.getLocalClassName());
                    showValidationDialog(false);
                    return readResult;
                }

                Log.i("readResult" , "Results split >> "+ results.toString());
                boolean validated = this.mainActivity.validateUser(results[0],results[1]);
                if(validated){
                    // Validated ( return result & write )
                    // Write new content into the tag
                    // Recive new content
                    Log.i("doItInBackground", "VALIDATED with ["+readResult+"]");
                    String newContent = this.mainActivity.bufferWriteString;

                    // Write new content
                    Log.i("readResult" , "Pre write");

                    writeTag(newContent, tag);
                    Log.i("readResult" , "Post write");

                    // Show validated message!
                    showValidationDialog(true);

                    return readResult;

                } else{

                    // Not validated (only return result and dont write
                    // Show not validated message
                    Log.i("doItInBackground", "NO VALIDATED with [" + readResult + "]");
                    showValidationDialog(false);

                    return readResult;
                }
            }
            Log.i("readResult" , "Post if ");
            return null;
        }

        /**
         * Show a dialog with validation information
         * @param isValidated
         */
        private void showValidationDialog(final boolean isValidated){
            mainActivity.runOnUiThread(new Runnable() {
                public void run() {
                    final Dialog dialog = new Dialog(mainActivity, R.style.FullHeightDialog);
                    dialog.setContentView(R.layout.custom_dialog_layout);
                    // Set the custom dialog components - text, image and button
                    TextView text = (TextView) dialog.findViewById(R.id.custom_dialog_text);
                    ImageView image = (ImageView) dialog.findViewById(R.id.custom_dialog_image);

                    if(isValidated){
                        text.setText(getResources().getString(R.string.validated));
                        image.setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.ic_check));
                        //image.setColorFilter(getResources().getColor(R.color.green), android.graphics.PorterDuff.Mode.MULTIPLY);
                        image.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
                    }else{
                        text.setText(getResources().getString(R.string.not_validated));
                        image.setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.ic_do_not_disturb));
                        image.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                    }

                    Button dialogButton = (Button) dialog.findViewById(R.id.buttonCustomDialogAccept);
                    // if button is clicked, close the custom dialog
                    dialogButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            });
        }

        /**
         * Try to write text into a tag given
         * @param text
         * @param tag
         */
        private void writeTag(String text,Tag tag)
        {
            // record that contains our custom "retro console" game data, using custom MIME_TYPE
            String mimeType = MIME_TEXT_PLAIN;
            NdefRecord record = NdefRecord.createTextRecord(null,text);
            NdefMessage message = new NdefMessage(new NdefRecord[] { record });
            Context ctx = getApplicationContext();
            try
            {
                // see if tag is already NDEF formatted
                Ndef ndef = Ndef.get(tag);
                if (ndef != null)
                {
                    ndef.connect();
                    if (!ndef.isWritable())
                    {
                        mainActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Read-only tag.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    // work out how much space we need for the data
                    int size = message.toByteArray().length;
                    if (ndef.getMaxSize() < size)
                    {
                        mainActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Tag doesn't have enough free space.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    ndef.writeNdefMessage(message);
                    mainActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.write_ok), Toast.LENGTH_LONG).show();

                            // If is a new user who has been written
                            if( mainActivity.isANewUser){
                                mainActivity.writeNewRegisterToDB();
                            }else{
                                mainActivity.updateRegisterToDB();
                            }
                        }
                    });
                    ndef.close();
                }
                else
                {
                    // attempt to format tag
                    NdefFormatable format = NdefFormatable.get(tag);
                    if (format != null)
                    {
                        try
                        {
                            format.connect();
                            format.format(message);
                            Toast.makeText(ctx, "OK Writing", Toast.LENGTH_LONG ).show();
                        }
                        catch (IOException e)
                        {
                            Toast.makeText(ctx,"Unable to format tag to NDEF.", Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        Toast.makeText(ctx,"Tag doesn't appear to support NDEF format.", Toast.LENGTH_LONG).show();
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        /**
         * Read text from a nfc record
         * @param record
         * @return result of reading the record
         * @throws UnsupportedEncodingException
         */
        private String readText(NdefRecord record) throws UnsupportedEncodingException {
            /*
             * See NFC forum specification for "Text Record Type Definition" at 3.2.1
             *
             * http://www.nfc-forum.org/specs/
             *
             * bit_7 defines encoding
             * bit_6 reserved for future use, must be 0
             * bit_5..0 length of IANA language code
             */
            Log.i("readText","init");
            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String textEncoding;
            if((payload[0] & 128) == 0){
                textEncoding = "UTF-8";
            }else{
                textEncoding = "UTF-16";
            }

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Log.i("onPostExecute", "Read content: " + result);
                Fragment f = getSupportFragmentManager().findFragmentByTag(ReadNFCFragment.class.getName());

                // Is required fragment is displayed
                if (f instanceof ReadNFCFragment){
                    if(!(result.equals("-1"))){
                        ((ReadNFCFragment) f).putResult(result);
                    }
                }
                else{
                    Log.i("NO fragment inflated", "Error - With content [" + result + "]");
                    setToast(getResources().getString(R.string.error_or_empty_nfc), Toast.LENGTH_LONG);
                }
            }
        }
    }

    protected void setToast(String message, int duration){
        Utils.setToast(this, message, Toast.LENGTH_LONG);
    }

}
