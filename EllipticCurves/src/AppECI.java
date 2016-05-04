import java.math.BigInteger;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.math.ec.ECPoint;

public interface AppECI {

	/**
	 * New EC from current params
	 * @param field
	 * @param a
	 * @param b
	 * @return curve params
	 */
	public X9ECParameters newEC(int field, BigInteger a, BigInteger b );
	
	/**
	 * Load EC from current params
	 * Need the generated seed
	 * @param field
	 * @param a
	 * @param b
	 * @param seed
	 * @return curve params
	 */
	public X9ECParameters loadEC(int field, BigInteger a, BigInteger b, byte[] seed);
	
	/**
	 * Generate a new pair of BigIntegers of random private keys
	 * @return tuple of K and R values
	 */
	public BigInteger[] newKeys();
	
	/**
	 * Set current K and R keys from params
	 * @param k
	 * @param r
	 * @return ECPoint
	 */
	public void loadKeys(BigInteger k, BigInteger r);
	
	/**
	 * Get a random point form the curve
	 * @return ECPoint
	 * @throws CurveNotLoaded 
	 */
	public ECPoint getRandomPoint() throws CurveNotLoaded;
	
	/**
	 * Get a point from the curve with the given params
	 * @param x
	 * @param y
	 * @return ECPoint
	 */
	public ECPoint getPoint(BigInteger x, BigInteger y);
	
	/**
	 * Add the point p1 to point p1 n times
	 * If n is 1, then the result would be p1+p1
	 * If n is >1, then the result would be (p1+p1+...+p1) with n add operations
	 * @param p1
	 * @param n 
	 * @return result of add p1 n times
	 */
	public ECPoint addPoint(ECPoint p1,int n);
	
	/**
	 * Add p2 value to p1 point n times
	 * @param p1
	 * @param p2 
	 * @return result of p1+p2 n times
	 */
	public ECPoint addPoints(ECPoint p1, ECPoint p2);
	
	/**
	 * Gets the [x,y] values of given ECPoint 
	 * @return [x,y] values
	 */
	public BigInteger[] getECPointCoords(ECPoint p1);
}
