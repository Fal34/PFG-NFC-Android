package tet;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
import java.util.zip.DataFormatException;

import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECCurve.F2m;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

public class AppEllipticCurve implements AppECI{
    // Constants
    private static final int DEFAULT_KEY_SIZE = 8;
    private static final int DEFAULT_SEED_SIZE = 8;
    private static final int DEFAULT_REDUCTION_POLYNOMIAL = 2;
    private static final int DEFAULT_COFACTOR_VALUE = 2;
    private static final int TARGET_NFCTAG_SIZE = 137;
    private static final int TARGET_NFCTAG_OFFSET = 7;
    
    // Attr
    private BigInteger a, b, k, r;
    private X9ECParameters ecparams = null;
    private ECCurve.F2m ecurve;
    private ECParameterSpec params = null;
    
    /** Constructor */
    public AppEllipticCurve(){
    	// Constructor
    }
    
    /** Getters And setters 
     * @throws CurveNotLoaded */
    
    
	@Override
	public X9ECParameters newEC(String curveName) throws IllegalArgumentException , CurveNotLoaded {
		
		// Default values
		SecureRandom rnd = new SecureRandom();
		// Load keys
		newKeys();
		
	    // Load curve over F2m field in X9.62 list curves
		ecparams  = ECNamedCurveTable.getByName(curveName);
		if (ecparams == null){
			throw new IllegalArgumentException("Curve name does not exists");
		}
	    this.ecurve = (F2m) ecparams.getCurve();
	    
		ECPoint p = getRandomPoint();
		ECCurve ecurve = p.getCurve();
		
		// Curve and random point values
		System.out.println("###########\t Curve and Random point values");
		System.out.println("Curve name : " + curveName);
		System.out.println("Curve cofactor : "+ ecurve.getCofactor());
		System.out.println("Curve order : "+ ecurve.getOrder());
		System.out.println("Curve order length : "+ ecurve.getOrder().bitLength());
		System.out.println("Random P coord X : " + p.getAffineXCoord().toString());
		System.out.println("Random P coord Y : " + p.getAffineYCoord().toString());
		
		// Encoding
		System.out.println("###########\t Points Encoding");
		BigInteger biP = new BigInteger(p.getEncoded(true));
		BigInteger biPFalse = new BigInteger(p.getEncoded(false));
		System.out.println("P encoded as BI (with " + biP.bitLength()  + " bit length): " + biP);
		System.out.println("P encoded as BI no compression (with " + biPFalse.bitLength()  + " bit length): " + biPFalse);
		System.out.println("Random Points are equals?: " + p.equals(getRandomPoint()));
		
		// Decoding
		System.out.println("###########\t Points Decoding");
		ECPoint decodedP = ecurve.decodePoint(biP.toByteArray());
		System.out.println("Decoded P coord X: " + decodedP.getAffineXCoord().toString());
		System.out.println("Decoded P coord Y: " + decodedP.getAffineYCoord().toString());
		System.out.println("Are decoded P and original P equals?: " + decodedP.equals(p));
		
		// Sum points
		ECPoint p2 = decodedP.twice().normalize();
		ECPoint customP2 = addPoint(decodedP,1);
		System.out.println("###########\t Sum Points");
		System.out.println("Point P decoded : " + decodedP.toString());
		System.out.println("P twice : " + p2.toString());
		System.out.println("P+P custom funct : " + addPoint(decodedP,1));
		System.out.println("P+P is equals to P twice? " + p2.equals(customP2));
		
		// Sum points II
		System.out.println("###########\tSum points II");
		ECPoint p3 = p2.add(decodedP).normalize();
		ECPoint customP3 = addPoint(decodedP,2);
		ECPoint customP3Plus = addPoints(p2,decodedP);
		System.out.println("P3 is : " + p3);
		System.out.println("Custom P3 as 3*p is equals to p3? " + p3.equals(customP3));
		System.out.println("Custom P3 as p2+p is equals to p3? " + p3.equals(customP3Plus));
		
		// NFC Outputs
		System.out.println("###########\t NFC outputs");
		String userId = "CCC11";
		String value = userId+","+biP.toString();
		System.out.println("Value is " + value);
		System.out.println("Value length " + value.length());
		String valueToBase64 = new String(Base64.encode(value.getBytes()));
		System.out.println("In Base64 is : " + valueToBase64);
		System.out.println("Base64 length is : " + valueToBase64.length());
		System.out.println("Is avialable size in NFC-T? " + ( valueToBase64.length() <= (TARGET_NFCTAG_SIZE-TARGET_NFCTAG_OFFSET)));
		
		String base64ToValue = new String(Base64.decode(valueToBase64.getBytes()));
		System.out.println("Decoded from Base64 is : " + base64ToValue);
		System.out.println("Decoded from Base64 is equals to value? " + base64ToValue.equals(value));
		
		
		
		
		

		// Result
		System.out.println("\n\n\n#####################");
		
		return ecparams;
	}

	/**
	 * Gets a random point 'g' generator
	 * 
	 * Following the generic algorithm of add points
	 */
	public ECPoint getG(ECCurve ec){
		// Set random points of a generator
		
		BigInteger x,y;
		
		// ec.createPoint(x, y);
		
		return null;
	}
		
	@Override
	public X9ECParameters loadEC(String curveName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigInteger[] newKeys() {
		// Generate and set a new key pairs randomly
        SecureRandom rand = new SecureRandom();
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
		SecureRandom rnd = new SecureRandom();
		ECPoint g = ecparams.getG();
		BigInteger n = ecparams.getN();
		int nBitLength = n.bitLength();
		BigInteger x;
		do
		{
		    x = new BigInteger(nBitLength, rnd);
		}
		while (x.equals(0)  || (x.compareTo(n) >= 0));
		ECPoint randomPoint = g.multiply(x).multiply(this.k).normalize();
		
		return randomPoint;
	}
	
	@Override
	public ECPoint getPoint(BigInteger x, BigInteger y) throws CurveNotLoaded{
		if(ecurve == null){
			throw new CurveNotLoaded("Elliptic Curve is not set");
		}
		
		ECPoint result = ecurve.createPoint(x, y).normalize();
		return result;
	}
	
	@Override
	public ECPoint addPoint(ECPoint p1, int n) {
		return p1.multiply(new BigInteger(Integer.toString(n+1))).normalize();
	}

	@Override
	public ECPoint addPoints(ECPoint p1, ECPoint p2) {
		return p1.add(p2).normalize();
	}

	@Override
	public BigInteger[] getECPointCoords(ECPoint p1) {
		return new BigInteger[]{
				p1.getXCoord().toBigInteger(), 
				p1.getYCoord().toBigInteger()};
	}
}
