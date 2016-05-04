package fidel.pfg.fnfc.core.administration;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import fidel.pfg.fnfc.MainActivity;
import fidel.pfg.fnfc.R;
import fidel.pfg.fnfc.ddbb.AppDbSQLiteHelper;
import fidel.pfg.fnfc.utils.Utils;

public class AdministrationFragment extends Fragment {

    // DB
    private SQLiteDatabase db;
    private Button db_restart_button;
    // System
    private Button ec_create;
    private EditText input_ec_a;
    private EditText input_ec_b;
    private EditText input_ec_field;


    public AdministrationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        db = MainActivity.getDBCon();
        // Inflate
        View view = inflater.inflate(R.layout.fragment_administration, container, false);

        Bundle extras = getArguments();
        if(extras!=null){
           //
        }

        // Tab management
        Resources res = getResources();
        TabHost tabs=(TabHost)view.findViewById(android.R.id.tabhost);
        tabs.setup();
        TabHost.TabSpec spec=tabs.newTabSpec("tab_system");
        spec.setContent(R.id.tab_system);
        spec.setIndicator(getResources().getString(R.string.system));
        tabs.addTab(spec);
        spec=tabs.newTabSpec("tab_db");
        spec.setContent(R.id.tab_db);
        spec.setIndicator(getResources().getString(R.string.db));
        tabs.addTab(spec);
        // Set default tab
        tabs.setCurrentTab(0);

        // Init elements DB
        db_restart_button = (Button) view.findViewById(R.id.db_restart_button);
        db_restart_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Activity currentActivity = getActivity();
                new AlertDialog.Builder(currentActivity)
                        .setTitle(R.string.db_restart)
                        .setMessage(R.string.db_restart_confirm)
                        .setIcon(R.drawable.ic_warning)
                        .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                            // On Click Dialog EventListener
                            public void onClick(DialogInterface dialog, int whichButton) {
                                try {
                                    AppDbSQLiteHelper.initDefaultEntries(db);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Utils.setToast(currentActivity, getResources().getString(R.string.error_db_restart),
                                            Toast.LENGTH_LONG);
                                    return;
                                }
                                Utils.setToast(currentActivity, getResources().getString(R.string.success_db_restart),
                                        Toast.LENGTH_LONG);
                            }
                        })
                        .setNegativeButton(R.string.cancel, null).show();
            }
        });

        // Init Elements System EC
        input_ec_field = (EditText) view.findViewById(R.id.input_ec_field);
        input_ec_a = (EditText) view.findViewById(R.id.input_ec_a);
        input_ec_b = (EditText) view.findViewById(R.id.input_ec_b);

        // Buton System EC & Onclick
        ec_create = (Button) view.findViewById(R.id.ec_create);
        ec_create.setOnClickListener(new View.OnClickListener() {
            final Activity currentActivity = getActivity();
            @Override
            public void onClick(View v) {
                // Checks
                String ec_field = input_ec_field.getText().toString().trim();
                String ec_a = input_ec_a.getText().toString().trim();
                String ec_b = input_ec_b.getText().toString().trim();

                if(ec_field.length() == 0
                        || ec_a.length() == 0
                        || ec_b.length() == 0){
                    Utils.setToast(currentActivity, getResources().getString(R.string.error_empty_fields),
                            Toast.LENGTH_LONG);
                } else if(ec_a.equals("4") && ec_b.equals("27")){
                    Utils.setToast(currentActivity, getResources().getString(R.string.error_ec_constrains),
                            Toast.LENGTH_LONG);
                } else {
                    new AlertDialog.Builder(currentActivity)
                            .setTitle(R.string.ec_restart)
                            .setMessage(R.string.ec_restart_confirm)
                            .setIcon(R.drawable.ic_warning)
                            .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                                // On Click Dialog EventListener
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    try {
                                        String ec_field = input_ec_field.getText().toString().trim();
                                        String ec_a = input_ec_a.getText().toString().trim();
                                        String ec_b = input_ec_b.getText().toString().trim();
                                        ((MainActivity) currentActivity).newEC(Integer.parseInt(ec_field), ec_a, ec_b);
                                    } catch (Exception e) {
                                        Utils.setToast(currentActivity, getResources().getString(R.string.error_db_restart),
                                                Toast.LENGTH_LONG);
                                        return;
                                    }
                                    Utils.setToast(currentActivity, getResources().getString(R.string.success_db_restart),
                                            Toast.LENGTH_LONG);
                                    input_ec_field.setText("");
                                    input_ec_a.setText("");
                                    input_ec_b.setText("");
                                }
                            })
                            .setNegativeButton(R.string.cancel, null).show();
                }
            }
        });

        return view;
    }

}