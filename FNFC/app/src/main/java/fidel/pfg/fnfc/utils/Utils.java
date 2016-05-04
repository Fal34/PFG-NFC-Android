package fidel.pfg.fnfc.utils;

import android.app.Activity;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

public class Utils {

    public static void setToast(Activity activity, String message, int duration){
        Toast.makeText(activity, message,
                Toast.LENGTH_LONG).show();
    }
}