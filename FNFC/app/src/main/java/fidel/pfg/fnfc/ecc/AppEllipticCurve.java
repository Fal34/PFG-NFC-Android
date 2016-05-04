package fidel.pfg.fnfc.ecc;


import android.util.Log;

import java.math.BigInteger;
import java.security.spec.ECField;
import java.security.spec.ECFieldF2m;
import java.security.spec.EllipticCurve;
import java.util.Random;

/**
 * Created by fidel on 28/04/2016.
 */
public class AppEllipticCurve {

    // Constants
    private static final int DEFAULT_KEY_SIZE = 8;
    public static final int DEFAULT_SEED_SIZE = 8;

    // Attr
    private ECFieldF2m field;
    private BigInteger a, b, k, r;
    private EllipticCurve ec;


    // Default Constructor
    public AppEllipticCurve() {

    }

    // Constructor with params
    public AppEllipticCurve(EllipticCurve ec, BigInteger k, BigInteger r) {
        this.setValuesGivingEc(ec);

        // Key Pairs
        if ( k == null || r == null){
            this.loadKeyPairs(k,r);
        }
    }

    private void setValuesGivingEc(EllipticCurve ec) {
        this.ec = ec;
        this.field = (ECFieldF2m) ec.getField();
        this.a = ec.getA();
        this.b = ec.getB();
    }

    public void loadKeyPairs(BigInteger k, BigInteger r){
        this.k = k;
        this.r = r;
        Log.i("loadKeyPairs", "Key pairs setted :" + "[" + k.toString() + "," + r.toString() + "]");
    }

    public AppEllipticCurve loadCurve(int field, String a, String b,byte[] seed) {
        // Change values for actual instance
        EllipticCurve newEc = new EllipticCurve(new ECFieldF2m(field), new BigInteger(a), new BigInteger(b),seed);
        setValuesGivingEc(newEc);

        // Generate new key pars
        // newKeyPairs();

        return this;
    }

    public void newKeyPairs(){
        // Generate new key pars randomly

        Random rand = new Random();
        this.k = new BigInteger(DEFAULT_KEY_SIZE, rand);
        this.r = new BigInteger(DEFAULT_KEY_SIZE, rand);

        Log.i("newKeyPairs", "Key pairs generated :" + "[" + k.toString() + "," + r.toString() + "]");
    }

    public BigInteger getK() {
        return k;
    }
    public BigInteger getR() {
        return r;
    }

    public String getRandomPoint() {

        // Generate a random EC point in the elliptic curve
        // [TODO]
        return "PPPPPPP";
    }

    public String sumECPoint(int i, String p) {

        // Sum i times the EC point P
        // [TODO]

        return i+"XXXXXXX";
    }

    public String sumECPoint(String val, String p) {

        // Sum once the point p to the value 'val'
        // [TODO]

        return "+1YYYYYY";
    }
}
