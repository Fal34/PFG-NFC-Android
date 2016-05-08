package fidel.pfg.fnfc;

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fidel.pfg.fnfc.ddbb.AppDbSQLiteHelper;

public class UsersListFragment extends Fragment {

    // Attr
    private TableLayout table_content;
    private boolean needHead = true;
    private TextView show_users_error;
    private ScrollView users_list_container;

    public UsersListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        SQLiteDatabase db = MainActivity.getDBCon();
        String name, key_p, key_q, key_val;
        int count = 0;
        List<String> list_row;

        // Inflate
        View view = inflater.inflate(R.layout.fragment_users_list, container, false);

        this.show_users_error = (TextView) view.findViewById(R.id.show_users_error);
        this.users_list_container = (ScrollView) view.findViewById(R.id.users_list_container);
        this.table_content = (TableLayout) view.findViewById(R.id.users_list_table_content);

        // Access to DB
        Cursor cursor = AppDbSQLiteHelper.getUsersInfo(db);
        if (cursor.moveToFirst()) {
            // Reset
            if (this.table_content.getChildCount() > 0) {
                this.table_content.removeAllViews();
                needHead = true;
            }
            this.setTableHeader(view);

            while (!cursor.isAfterLast()) {
                list_row = new ArrayList<>();
                list_row.add(Integer.toString(++count));
                list_row.add(cursor.getString(cursor
                        .getColumnIndex("user_id")));
                list_row.add( cursor.getString(cursor
                        .getColumnIndex("p")));
                list_row.add( cursor.getString(cursor
                        .getColumnIndex("q")));
                list_row.add( cursor.getString(cursor
                        .getColumnIndex("set_q")));
                list_row.add( cursor.getString(cursor
                        .getColumnIndex("val")));

                Log.i("Row debuggin", list_row.toString());

                this.addRegister(view, list_row);
                cursor.moveToNext();
            }
            showUsers();
        } else {
            // No users
            showUsersError();
        }
        return view;
    }

    private void addRegister(View view, List<String> list_row) {
        TableRow row = null;
        int params = list_row.size();
        boolean isOdds = (Integer.parseInt(list_row.get(0)) % 2 != 0);
        row = new TableRow(view.getContext());
        row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));

        for (int i = 0; i < params; i++) {
            TextView tv = new TextView(view.getContext());
            // Attr
            tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tv.setBackgroundResource(R.drawable.cell_shape);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(15);
            tv.setTextIsSelectable(true);

            tv.setPadding(15, 20, 15, 20);

            if (isOdds) {
                tv.setBackgroundTintList(view.getContext().getResources().getColorStateList(R.color.white));
            } else {
                tv.setBackgroundTintList(view.getContext().getResources().getColorStateList(R.color.light_grey));
            }
            // Value
            tv.setText(list_row.get(i));
            row.addView(tv);
        }
        this.table_content.addView(row);
    }

    private void setTableHeader(View view) {

        Context context = view.getContext();
        TableRow tr = new TableRow(context);
        tr.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));

        TextView td = new TextView(context);
        td.setText(R.string.number_symbol);
        this.setCommonCellProperties(context, td);
        tr.addView(td);

        td = new TextView(context);
        td.setText(R.string.user);
        this.setCommonCellProperties(context,td);
        tr.addView(td);

        td = new TextView(context);
        td.setText(R.string.p);
        this.setCommonCellProperties(context, td);
        tr.addView(td);

        td = new TextView(context);
        td.setText(R.string.q);
        this.setCommonCellProperties(context, td);
        tr.addView(td);

        td = new TextView(context);
        td.setText(R.string.original_q);
        this.setCommonCellProperties(context, td);
        tr.addView(td);

        td = new TextView(context);
        td.setText(R.string.value);
        this.setCommonCellProperties(context, td);
        tr.addView(td);

        // Adding row to Table
        table_content.addView(tr);
        this.needHead = false;
    }

    private void setCommonCellProperties(Context context,TextView td){
        td.setPadding(30, 30, 30, 30);
        td.setTextSize(17);
        td.setBackgroundResource(R.drawable.cell_shape);
        td.setBackgroundTintList(context.getResources().getColorStateList(R.color.grey));
        td.setTextColor(Color.BLACK);
        td.setGravity(Gravity.CENTER_HORIZONTAL);
    }

    private void showUsers(){
        this.users_list_container.setVisibility(View.VISIBLE);
        this.show_users_error.setVisibility(View.GONE);
    }

    private void showUsersError(){
        this.users_list_container.setVisibility(View.GONE);
        this.show_users_error.setVisibility(View.VISIBLE);
    }
}
