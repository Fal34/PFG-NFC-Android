/**
 * Implementation of the interface {@link main.AppEllipticCurveI} to work with Elliptic Curves and the bouncy castle package.
 * @author Fidel Abascal
 * @date 26/04/2016
 */
package fidel.pfg.fnfc.ecc;

import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.SecureRandom;

import fidel.pfg.fnfc.ecc.interfaces.AppEllipticCurveI;
import fidel.pfg.fnfc.exceptions.CurveNotLoaded;

public class AppEllipticCurve implements AppEllipticCurveI{

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

    @Override
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

    @Override
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

    @Override
    public BigInteger newKey(){
        // Generate and set a new key pairs randomly
        SecureRandom rand = new SecureRandom();
        do{
            this.k = new BigInteger(DEFAULT_KEY_SIZE, rand);
        }
        while(this.k.bitLength() != DEFAULT_KEY_SIZE);

        return k;
    }

    @Override
    public ECPoint.F2m addPoint(ECPoint.F2m p1, int n) {
        ECPoint.F2m result = p1;
        for ( int i = 0; i<n ; i++){
            result = addPointsCustom(result, p1);
        }

        return result;
    }

    @Override
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

    @Override
    public String encode(ECPoint.F2m point) throws CurveNotLoaded{
        // If curve not loaded
        if(ecurve == null){
            throw new CurveNotLoaded("Elliptic Curve is not set");
        }

        return new BigInteger(point.getEncoded(true)).toString();
    }

    @Override
    public ECPoint.F2m decode(String pointValue) throws CurveNotLoaded{
        // If curve not loaded
        if(ecurve == null){
            throw new CurveNotLoaded("Elliptic Curve is not set");
        }

        return (ECPoint.F2m) ecurve.decodePoint(new BigInteger(pointValue).toByteArray()).normalize();
    }
}
