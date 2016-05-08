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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
    private Spinner curve_name_spinner;


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

        // Buton System EC & Onclick
        ec_create = (Button) view.findViewById(R.id.ec_create);
        curve_name_spinner = (Spinner) view.findViewById(R.id.input_ec_curve_name);
        // Create an ArrayAdapter using the string array and a default spinner
        ArrayAdapter<CharSequence> staticAdapter = ArrayAdapter
                .createFromResource(view.getContext(), R.array.curve_name_array,
                        R.layout.spinner_item);

        // Specify the layout to use when the list of choices appears
        staticAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        curve_name_spinner.setAdapter(staticAdapter);
        curve_name_spinner.setSelection(0);
        ec_create.setOnClickListener(new View.OnClickListener() {
            final Activity currentActivity = getActivity();

            @Override
            public void onClick(View v) {
                // Checks
                final String curve_name = curve_name_spinner.getSelectedItem().toString().trim();

                new AlertDialog.Builder(currentActivity)
                        .setTitle(R.string.ec_restart)
                        .setMessage(R.string.ec_restart_confirm)
                        .setIcon(R.drawable.ic_warning)
                        .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                            // On Click Dialog EventListener
                            public void onClick(DialogInterface dialog, int whichButton) {
                                try {
                                    ((MainActivity) currentActivity).newEC(curve_name);
                                } catch (Exception e) {
                                    Utils.setToast(currentActivity, getResources().getString(R.string.error_new_ec),
                                            Toast.LENGTH_LONG);
                                    return;
                                }
                                curve_name_spinner.setSelection(0);
                            }
                        })
                        .setNegativeButton(R.string.cancel, null).show();
            }
        });

        return view;
    }

}