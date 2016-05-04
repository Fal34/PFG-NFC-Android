package fidel.pfg.fnfc;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

public class ReadNFCFragment extends Fragment {

    private TextView readNfcText;
    private TextView readNfcText2;
    private ImageView readNfcStress;
    private TextView readNfcResultText;
    private TextView readNfcResult;

    public static String TRIGGERED_FROM_NFC = "triggeredFromNFC";
    private boolean triggeredFromNFC;
    private boolean isResultsActive = false;

    public ReadNFCFragment() {
        // Required empty public constructor
    }

    protected void setTriggeredFromNFC(boolean triggeredFromNFC) {
        this.triggeredFromNFC = triggeredFromNFC;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // SQLiteDatabase db = MainActivity.getDBCon();
        // Inflate
        View view = inflater.inflate(R.layout.fragment_verify_nfc, container, false);
        boolean triggeredFromNFC= false;
        Bundle extras = getArguments();
        if(extras!=null){
            triggeredFromNFC = extras.getBoolean(TRIGGERED_FROM_NFC,false);
        }

        // Init elements
        this.readNfcText = (TextView) view.findViewById(R.id.read_nfc_text);
        this.readNfcText2 = (TextView) view.findViewById(R.id.read_nfc_text_2);
        this.readNfcStress = (ImageView) view.findViewById(R.id.read_nfc_stress);
        this.readNfcResultText = (TextView) view.findViewById(R.id.read_nfc_result_text);
        this.readNfcResult = (TextView) view.findViewById(R.id.read_nfc_result);

        // Init animation
        Animation animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.rotate_from_centre);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.RESTART);
        animation.setInterpolator(new LinearInterpolator());
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Log.i("animation", "start");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // If partner is visible, then start animation
                TextView targetPartner = (TextView)getView().findViewById(R.id.read_nfc_text);
                if(targetPartner.getVisibility() == View.VISIBLE){
                    readNfcStress.startAnimation(animation);
                }
                Log.i("animation","end");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

                Log.i("animation","repeat");
            }
        });
        this.readNfcStress.startAnimation(animation);

        // if triggered
        if(triggeredFromNFC) {
            // Change title
            getActivity().setTitle(getResources().getString(R.string.verify_nfc));
        }
        // Default call
        hideResults();

        return view;
    }

    /** Methods to show and hide result from a NFC-T */
    private void showResults(){
        readNfcText.setVisibility(View.GONE);
        readNfcText2.setVisibility(View.GONE);
        readNfcStress.clearAnimation();
        readNfcStress.setVisibility(View.GONE);
        readNfcResultText.setVisibility(View.VISIBLE);
        readNfcResult.setVisibility(View.VISIBLE);
        this.isResultsActive = true;
    }
    private void hideResults(){
        readNfcText.setVisibility(View.VISIBLE);
        readNfcText2.setVisibility(View.VISIBLE);
        readNfcStress.getAnimation().reset();
        readNfcStress.setVisibility(View.VISIBLE);
        readNfcResultText.setVisibility(View.GONE);
        readNfcResult.setVisibility(View.GONE);
        this.isResultsActive = false;
    }

    protected void putResult(String result){
        Log.i("putResult", "Showing result: " + result);
        readNfcResult.setText(result);
        // If results elements are not active
        if(!this.isResultsActive){
          showResults();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //
    }

}
