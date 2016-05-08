package fidel.pfg.fnfc;

import android.app.Activity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fidel.pfg.fnfc.ddbb.AppDbSQLiteHelper;
import fidel.pfg.fnfc.utils.Utils;

public class RegisterFragment extends Fragment {

    // Attr
    Spinner users_list_dropdown;
    Button register_new_user;
    NumberPicker user_q_value;

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        SQLiteDatabase db = MainActivity.getDBCon();
        // Inflate
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        Bundle extras = getArguments();
        if(extras!=null){
            //
        }

        // Inicialización
        users_list_dropdown = (Spinner)view.findViewById(R.id.users_list_spinner);
        user_q_value = (NumberPicker)view.findViewById(R.id.user_q_value);
        user_q_value.setMinValue(2);
        user_q_value.setMaxValue(200);
        user_q_value.setValue(15);

        register_new_user = (Button)view.findViewById(R.id.register_new_user);
        register_new_user.setOnClickListener(new View.OnClickListener() {
             final Activity currentActivity = getActivity();

             @Override
             public void onClick(View v) {
                 // Checks
                 new AlertDialog.Builder(currentActivity)
                         .setTitle(R.string.user_create)
                         .setMessage(R.string.user_create_confirm)
                         .setIcon(R.drawable.ic_warning)
                         .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                             // On Click Dialog EventListener
                             public void onClick(DialogInterface dialog, int whichButton) {
                                 try {
                                     String q = Integer.toString(user_q_value.getValue());
                                     String userId = users_list_dropdown.getSelectedItem().toString().trim();
                                     Log.i("alertDialog", "UserID Selected: "+ users_list_dropdown.getSelectedItem().toString().trim() + "With Q: "+ q);
                                     ((MainActivity)currentActivity).makeNewRegisterFromUser(userId, q, false);
                                 } catch (Exception e) {
                                     e.printStackTrace();
                                     Utils.setToast(currentActivity, getResources().getString(R.string.error_user_register),
                                             Toast.LENGTH_LONG);
                                     return;
                                 }
                                 Utils.setToast(currentActivity, getResources().getString(R.string.success_user_register),
                                         Toast.LENGTH_LONG);
                             }
                         })
                         .setNegativeButton(R.string.cancel, null).show();
             }
         });

        // Obtengo lista de usuarios
        AppDbSQLiteHelper.getUsersNotInSystem(db);
        Cursor cursor = AppDbSQLiteHelper.getUsersNotInSystem(db);
        int cont = 0;
        String[] items = new String[cursor.getCount()];
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                items[cont++] = cursor.getString(cursor
                        .getColumnIndex("name"));
                cursor.moveToNext();
            }

            // Attach & show users list
            addUsersToSpinner(view, items);
            showUsers();
        }else{

            // Hide users list
            hideUsers();
            Utils.setToast(getActivity(), getResources().getString(R.string.error_no_users),
                    Toast.LENGTH_LONG);
        }


        return view;
    }

    private void addUsersToSpinner(View view, String... users){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, users);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        users_list_dropdown.setAdapter(adapter);
    }

    private void showUsers(){
        users_list_dropdown.setVisibility(View.VISIBLE);
        register_new_user.setVisibility(View.VISIBLE);
    }

    private void hideUsers(){
        users_list_dropdown.setVisibility(View.GONE);
        register_new_user.setVisibility(View.GONE);
    }
}