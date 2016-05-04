import java.math.BigInteger;
import java.util.Random;
import java.util.zip.DataFormatException;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

public class AppEllipticCurve implements AppECI{
    // Constants
    private static final int DEFAULT_KEY_SIZE = 8;
    private static final int DEFAULT_SEED_SIZE = 8;
    private static final int DEFAULT_REDUCTION_POLYNOMIAL = 2;
    private static final int DEFAULT_COFACTOR_VALUE = 2;
    
    // Attr
    private BigInteger a, b, k, r;
    private X9ECParameters ecparams = null;
    private ECCurve.F2m ecurve;
    private ECParameterSpec params = null;
    
    /** Constructor */
    public AppEllipticCurve(){
    	// Constructor
    }
    
    /** Getters And setters */
    
    
	@Override
	public ECParameterSpec newEC(int field, BigInteger a, BigInteger b) throws DataFormatException {
		
		// Default values
		Random rnd = new Random();
		BigInteger order = new BigInteger(Integer.toString(field));
	    BigInteger cofactor = BigInteger.valueOf(DEFAULT_COFACTOR_VALUE);
	    byte[] seed = (new BigInteger(DEFAULT_SEED_SIZE,rnd)).toByteArray();
	    
	    // Check if field is prime
	    if( !isPrime(field) ){
	    	throw new DataFormatException("Field given (" + field + ") is not prime");
	    }
	    
	    // Load curve over F2m field
	    ecurve = new ECCurve.F2m(
	        field,
	        DEFAULT_REDUCTION_POLYNOMIAL, a, b, order, cofactor);

	    ECCurve ecurve2 = new ECCurve.F2m(
		        field,
		        DEFAULT_REDUCTION_POLYNOMIAL, a, b, order, cofactor);
	    
	    BigInteger x = new BigInteger(field,rnd);
	    BigInteger y = new BigInteger(field,rnd);
	    
	    ECPoint p = ecurve.createPoint(x,y);
	    ECFieldElement xgiven = p.getXCoord();

	    BigInteger w = new BigInteger(p.getXCoord().getEncoded());
	    BigInteger z = new BigInteger(p.getYCoord().getEncoded());
	    byte[] by= p.getEncoded(true);
	    ECPoint newp = ecurve.createPoint(w,z);
	    ECPoint p2 = p.twice().normalize();
	    
	    BigInteger w2 = new BigInteger(p2.getXCoord().getEncoded());
	    BigInteger z2 = new BigInteger(p2.getYCoord().getEncoded());
	    ECPoint newp2 = ecurve.createPoint(w2, z2);
	    //System.out.println("W is " + w.toString());
	    //System.out.println("Z is " + z.toString());
	    //System.out.println("P is " + p.toString());
	    //System.out.println("Is P equals to new P? " + p.equals(newp));
	    //System.out.println("new P is " + newp.toString());
	    
	    System.out.println("P is valid? " + p.isValid());
	    System.out.println("P2 is valid? " + p2.isValid());
	    System.out.println("new P2 is valid? " + newp2.isValid());
	    
	    System.out.println("2P is " + p2.toString());	    
	    System.out.println("new P2 is " + newp2.toString());
	    System.out.println("Is P2 equals to new P2? " + p2.equals(newp2));
	    //System.out.println("P is valid? ");
	    
	    // Set X9Params
	    // TODO	    
	    
//	    ECCurve curve = new ECCurve.Fp(
//	            new BigInteger("123"), // q
//	            new BigInteger("2", 16), // a
//	            new BigInteger("3", 16)); // b
//	    
//	    ECParameterSpec params = new ECParameterSpec(
//	            curve,
//	            curve.decodePoint(Hex.decode("020ffa963cdca8816ccc33b8642bedf905c3d358573d3f27fbbd3b3cb9aaaf")), // G
//	            new BigInteger("883423532389192164791648750360308884807550341691627752275345424702807307"), //n
//	            BigInteger.valueOf(1), //h
//	            seed); // seed
	    
	    // named curve ansi_X9_62
	    
		return params;
	}

	/**
	 * Check if number is prime
	 * @param n
	 * @return
	 */
	private boolean isPrime(int n) {
	    if(n < 2) return false;
	    if(n == 2 || n == 3) return true;
	    if(n%2 == 0 || n%3 == 0) return false;
	    long sqrtN = (int)Math.sqrt(n)+1;
	    for(int i = 6; i <= sqrtN; i += 6) {
	        if(n%(i-1) == 0 || n%(i+1) == 0) return false;
	    }
	    return true;
	}
	
	@Override
	public X9ECParameters loadEC(int field, BigInteger a, BigInteger b, byte[] seed) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigInteger[] newKeys() {
		// Generate and set a new key pairs randomly
        Random rand = new Random();
        this.k = new BigInteger(DEFAULT_KEY_SIZE, rand);
        this.r = new BigInteger(DEFAULT_KEY_SIZE, rand);
        
		return new BigInteger[]{k,r};
	}

	@Override
	public void loadKeys(BigInteger k, BigInteger r) {
        // Set key pairs
        this.k = k;
        this.r = r;
	}

	@Override
	public ECPoint getRandomPoint() throws CurveNotLoaded {
		// Curve params check
		if(ecparams == null){
			throw new CurveNotLoaded("Curve params are not loaded");
		}
		
		// Set a new random point from curve params
		Random rnd = new Random();
		ECPoint g = ecparams.getG();
		BigInteger n = ecparams.getN();
		int nBitLength = n.bitLength();
		BigInteger x;
		do
		{
		    x = new BigInteger(nBitLength, rnd);
		}
		while (x.equals(0)  || (x.compareTo(n) >= 0));
		ECPoint randomPoint = g.multiply(x);
		
		return randomPoint;
	}
	
	@Override
	public ECPoint getPoint(BigInteger x, BigInteger y) throws CurveNotLoaded{
		if(ecurve == null){
			throw new CurveNotLoaded("Elliptic Curve is not set");
		}
		
		ECPoint result = ecurve.createPoint(x, y);
		return result;
	}
	
	@Override
	public ECPoint addPoint(ECPoint p1, int n) {
		return p1.multiply(new BigInteger(Integer.toString(n+1)));
	}

	@Override
	public ECPoint addPoints(ECPoint p1, ECPoint p2) {
		return p1.add(p2);
	}

	@Override
	public BigInteger[] getECPointCoords(ECPoint p1) {
		return new BigInteger[]{
				p1.getXCoord().toBigInteger(), 
				p1.getYCoord().toBigInteger()};
	}

}
