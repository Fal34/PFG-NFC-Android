/**
 * Interface to implement basic operations of Elliptic Curves with the bouncy castle package.
 * @author Fidel Abascal
 * @date 01/05/2016
 */

package fidel.pfg.fnfc.ecc.interfaces;

import java.math.BigInteger;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.math.ec.ECPoint;

import fidel.pfg.fnfc.exceptions.CurveNotLoaded;


public interface AppEllipticCurveI {

    /**
     * Load instance parameters of a elliptic curve from given values
     * @param {@link String} curveName
     * @param {@link java.math.BigInteger} k
     * @return {@link org.bouncycastle.asn1.x9.X9ECParameters} X9ECParameters of curve
     * @throws {@link IllegalArgumentException} IllegalArgumentException
     * @throws {@link fidel.pfg.fnfc.exceptions.CurveNotLoaded} CurveNotLoaded
     */
    public X9ECParameters loadEC(String curveName, BigInteger k) throws IllegalArgumentException , CurveNotLoaded;

    /**
     * Gets a random point from the loaded elliptic curve
     * @return {@link org.bouncycastle.math.ec.ECPoint.F2m} random point of the loaded curve
     * @throws {@link fidel.pfg.fnfc.exceptions.CurveNotLoaded} CurveNotLoaded
     */
    public ECPoint.F2m getRandomPoint() throws CurveNotLoaded;

    /**
     * Generate and set a new secure random key with a default key size
     * @return {@link org.bouncycastle.math.ec.ECPoint.F2m} random point
     */
    public BigInteger newKey();

    /**
     * Multiply the given point {@link org.bouncycastle.math.ec.ECPoint.F2m} p1 n+1 times
     * @param {@link org.bouncycastle.math.ec.ECPoint.F2m} p1 the point to multiply
     * @param n times to multiply +1
     * @return p1*(n+1) as {@link org.bouncycastle.math.ec.ECPoint.F2m}
     */
    public ECPoint.F2m addPoint(ECPoint.F2m p1, int n);

    /**
     * Add the {@link org.bouncycastle.math.ec.ECPoint.F2m} p2 to p1
     * @param {@link org.bouncycastle.math.ec.ECPoint.F2m} p1
     * @param {@link org.bouncycastle.math.ec.ECPoint.F2m} p2
     * @return result of p1+p2 as {@link org.bouncycastle.math.ec.ECPoint.F2m}
     */
    public ECPoint.F2m addPointsCustom(ECPoint.F2m p1, ECPoint.F2m p2);

    /**
     * Encode the {@link org.bouncycastle.math.ec.ECPoint.F2m} given
     * @param {@link org.bouncycastle.math.ec.ECPoint.F2m} point
     * @return {@link String} String encoded point
     * @throws {@link fidel.pfg.fnfc.exceptions.CurveNotLoaded} CurveNotLoaded
     */
    public String encode(ECPoint.F2m point) throws CurveNotLoaded;

    /**
     * Gets the ECPoint.F2m from encoded value
     * @param {@link String} pointValue
     * @return {@link org.bouncycastle.math.ec.ECPoint.F2m} decoded point
     * @throws {@link fidel.pfg.fnfc.exceptions.CurveNotLoaded} CurveNotLoaded
     */
    public ECPoint.F2m decode(String pointValue) throws CurveNotLoaded;

}
