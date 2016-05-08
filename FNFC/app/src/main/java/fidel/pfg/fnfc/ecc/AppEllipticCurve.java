package fidel.pfg.fnfc.ecc;


import android.util.Log;

import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Base64;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.spec.ECField;
import java.security.spec.ECFieldF2m;
import java.security.spec.EllipticCurve;
import java.util.Random;

import fidel.pfg.fnfc.exceptions.CurveNotLoaded;

/**
 * Created by fidel on 28/04/2016.
 */
public class AppEllipticCurve {

    // Constants
    private static final int DEFAULT_KEY_SIZE = 160;
    private static final int DEFAULT_SEED_SIZE = 8;
    private static final int DEFAULT_REDUCTION_POLYNOMIAL = 2;
    private static final int DEFAULT_COFACTOR_VALUE = 2;
    private static final int TARGET_NFCTAG_SIZE = 137;
    private static final int TARGET_NFCTAG_OFFSET = 7;

    // Attr
    private BigInteger k;
    private X9ECParameters ecparams = null;
    private ECCurve.F2m ecurve;
    private ECParameterSpec params = null;


    // Default Constructor
    public AppEllipticCurve() {
        // Constructor
    }

    public X9ECParameters loadEC(String curveName, BigInteger k) throws IllegalArgumentException , CurveNotLoaded {
        // Default values
        SecureRandom rnd = new SecureRandom();

        // Get a new key if necessary
        if( k == null ){
            newKey();
        }else{
            this.k = k;
        }

        // Load curve over F2m field
        ecparams = ECNamedCurveTable.getByName(curveName);
        if (ecparams == null) {
            throw new IllegalArgumentException("Curve name does not exists");
        }
        this.ecurve = (ECCurve.F2m) ecparams.getCurve();

        return ecparams;
    }


    /**
     * Gets a random point from the loaded elliptic curve
     * @return random point of the loaded curve
     * @throws CurveNotLoaded
     */
    public ECPoint.F2m getRandomPoint() throws CurveNotLoaded {
        // Curve params check
        if(ecparams == null){
            throw new CurveNotLoaded("Curve params are not loaded");
        }

        // Set a new random point from curve params
        SecureRandom rnd = new SecureRandom();
        ECPoint.F2m g = (org.bouncycastle.math.ec.ECPoint.F2m) ecparams.getG();
        BigInteger n = ecparams.getN();
        int nBitLength = n.bitLength();
        BigInteger x;
        do
        {
            x = new BigInteger(nBitLength, rnd);
        }
        while (x.equals(BigInteger.ZERO)  || (x.compareTo(n) >= 0));

        return (ECPoint.F2m) g.multiply(x).multiply(this.k).normalize();
    }

    /**
     * Generate and set a new secure random key with a default key size
     * @return random key
     */
    public BigInteger newKey(){
        // Generate and set a new key pairs randomly
        SecureRandom rand = new SecureRandom();
        this.k = new BigInteger(DEFAULT_KEY_SIZE, rand);

        Log.i("newKey", "Key generated :" + "[" + k.toString() + "]");

        return k;
    }

    /**
     * Multiply the given point n+1 times
     * @param p1 the point to multiply
     * @param n times to multiply +1
     * @return p1*(n+1) as {@link ECPoint.F2m}
     */
    public ECPoint.F2m addPoint(ECPoint.F2m p1, int n) {

        ECPoint.F2m result = p1;
        for ( int i = 0; i<n ; i++){
            result = addPointsCustom(result, p1);
            try {
                Log.i("ADD POINT", " : ["+ i +"] Coded as "+ encode(result));
            } catch (CurveNotLoaded curveNotLoaded) {
                curveNotLoaded.printStackTrace();
            }
        }
        //return (ECPoint.F2m) p1.multiply(new BigInteger(Integer.toString(n+1))).normalize();
        return result;
    }

    /**
     * Add p2 to p1
     * @param p1
     * @param p2
     * @return result of p1+p2
     */
    public ECPoint.F2m addPointsCustom(ECPoint.F2m p1, ECPoint.F2m p2){
        ECCurve.F2m ec = (ECCurve.F2m) p1.getCurve();
        ECPoint.F2m other = p2;

        // If the point is the infinity
        if (p1.isInfinity()) { return other; }
        if (other.isInfinity()) { return p1; }

        ECFieldElement.F2m x2 = (ECFieldElement.F2m)other.getX();
        ECFieldElement.F2m y2 = (ECFieldElement.F2m)other.getY();
        ECFieldElement.F2m x1 = (ECFieldElement.F2m)p1.getX();
        ECFieldElement.F2m y1 = (ECFieldElement.F2m)p1.getY();

        // Check if other = this or other = -this
        if (x1.equals(x2))
        {
            if (y1.equals(y2))
            {
                // this = other, i.e. this must be doubled
                return (ECPoint.F2m)p1.twice();
            }

            // this = -other, i.e. the result is the point at infinity
            return (ECPoint.F2m)ecurve.getInfinity();
        }

        ECFieldElement.F2m lambda
                = (ECFieldElement.F2m)(y1.add(y2)).divide(x1.add(x2));
        ECFieldElement.F2m x3
                = (ECFieldElement.F2m)lambda.square().add(lambda).add(x1).add(x2).add(ec.getA());
        ECFieldElement.F2m y3
                = (ECFieldElement.F2m)lambda.multiply(x1.add(x3)).add(x3).add(y1);

        // Return with Normalize
        return (org.bouncycastle.math.ec.ECPoint.F2m) ec.createPoint(x3.toBigInteger(), y3.toBigInteger(), true).normalize();
    }

    /**
     * Encode the ECPoint.F2m given
     * @param point
     * @return encoded point
     * @throws CurveNotLoaded
     */
    public String encode(ECPoint.F2m point) throws CurveNotLoaded{
        // If curve not loaded
        if(ecurve == null){
            throw new CurveNotLoaded("Elliptic Curve is not set");
        }

        return new BigInteger(point.getEncoded(true)).toString();
    }

    /**
     * Gets the ECPoint.F2m from encoded value
     * @param pointValue
     * @return decoded point
     * @throws CurveNotLoaded
     */
    public ECPoint.F2m decode(String pointValue) throws CurveNotLoaded{
        // If curve not loaded
        if(ecurve == null){
            throw new CurveNotLoaded("Elliptic Curve is not set");
        }

        return (ECPoint.F2m) ecurve.decodePoint(new BigInteger(pointValue).toByteArray()).normalize();
    }










    // ################ OLD
    // Constructor with params
/*    public AppEllipticCurve(EllipticCurve ec, BigInteger k) {
        this.setValuesGivingEc(ec);

        // Key Pairs
        if ( k == null){
            this.loadKeyPairs(k);
        }
    }

    private void setValuesGivingEc(EllipticCurve ec) {
        // this.ec = ec;
    }

    public void loadKeyPairs(BigInteger k){
        this.k = k;

        Log.i("loadKeyPairs", "Key pairs setted :" + "[" + k.toString() +"]");
    }

    public AppEllipticCurve loadCurve(String name, BigInteger k) {
        // Change values for actual instance
        this.k = k;
        // New Bouncy Castle as name

        return this;
    }

    public BigInteger getK() {
        return k;
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
    }*/
}
