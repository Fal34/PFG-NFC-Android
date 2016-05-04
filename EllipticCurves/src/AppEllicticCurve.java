import java.math.BigInteger;
import java.util.Random;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.math.ec.ECPoint;

public class AppEllicticCurve implements AppECI{
    // Constants
    private static final int DEFAULT_KEY_SIZE = 8;
    private static final int DEFAULT_SEED_SIZE = 8;
    
    // Attr
    private BigInteger a, b, k, r;
    private X9ECParameters ecparams;
    
	@Override
	public X9ECParameters newEC(int field, BigInteger a, BigInteger b) {
		// TODO Auto-generated method stub
		return null;
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
	public ECPoint getPoint(BigInteger x, BigInteger y) {
		// TODO Auto-generated method stub
		return null;
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
