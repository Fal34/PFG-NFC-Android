

import java.math.BigInteger;
import java.util.Random;

import org.bouncycastle.asn1.nist.NISTNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

public class Main2 {

	public static void main(String[] args) {
		
		X9ECParameters x9 = createParameters();

		
		ECPoint g = x9.getG();
		Random random = new Random();
		BigInteger n = x9.getN();
		int nBitLength = n.bitLength();
		BigInteger x;
		do
		{
		    x = new BigInteger(nBitLength, random);
		}
		while (x.equals(0)  || (x.compareTo(n) >= 0));
		ECPoint randomPoint = g.multiply(x);
		
		
		System.out.println("Punto G :" + g.toString());
		System.out.println("Punto Aleatorio :" + randomPoint.toString());
		
		
		
		
	}
	
	public static X9ECParameters createParameters()
	{
	    BigInteger c2m163v1n = new BigInteger("0400000000000000000001E60FC8821CC74DAEAFC1", 16);
	    BigInteger c2m163v1h = BigInteger.valueOf(2);

	    ECCurve c2m163v1 = new ECCurve.F2m(
	        163,
	        1, 2, 8,
	        new BigInteger("072546B5435234A422E0789675F432C89435DE5242", 16),
	        new BigInteger("00C9517D06D5240D3CFF38C74B20B6CD4D6F9DD4D9", 16),
	        c2m163v1n, c2m163v1h);

	    
	    return new X9ECParameters(
	        c2m163v1,
	        c2m163v1.decodePoint(
	            Hex.decode("0307AF69989546103D79329FCC3D74880F33BBE803CB")),
	        c2m163v1n, c2m163v1h,
	        Hex.decode("D2COFB15760860DEF1EEF4D696E6768756151754"));
	}
}
