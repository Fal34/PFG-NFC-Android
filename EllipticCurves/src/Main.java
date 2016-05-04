

import java.math.BigInteger;
import java.util.Random;

import org.bouncycastle.asn1.nist.NISTNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

public class Main {

	public static void main(String[] args) {
				
		Random random = new Random();
		X9ECParameters x9 = NISTNamedCurves.getByName("P-224"); // or whatever curve you want to use
		ECPoint g = x9.getG();
		BigInteger n = x9.getN();
		int nBitLength = n.bitLength();
		BigInteger x;
		do
		{
		    x = new BigInteger(nBitLength, random);
		}
		while (x.equals(0)  || (x.compareTo(n) >= 0));
		ECPoint randomPoint = g.multiply(x);
		ECPoint suma = addPoint(randomPoint,1);
		suma = addPoint(suma,randomPoint);
		ECCurve curve = randomPoint.getCurve();
		// ####
		if(suma.equals(randomPoint)){
			System.out.println("P1 + P1  = P1 Maalll");
		}else{
			System.out.println("P1 + P1  = 2P1 !! Bien");
		}
		
		if( suma.subtract(suma.subtract(randomPoint)).equals(randomPoint) ){
			System.out.println("2P1 - p1 = P1! Bien!");
		}else{
			System.out.println("2P1 - p1 != P1 - MALLL");
		}
		
		
		if(addPoint(randomPoint,1).equals(randomPoint.twice())){
			System.out.println("2P1 = 2P1 Bien");
		}else{
			System.out.println("2P1 = 2P1 Mal");
		}
		
		System.out.println("Orden de la curva : "+ curve.getOrder());
		System.out.println("Random point : "+ randomPoint.toString());
	}
	
	public static ECPoint addPoint (ECPoint p1, int times){
		ECPoint result = p1;
		for(int i = 0; i<times; i++){
			System.out.println("Point * Point "+ i);
			result = result.add(p1);
		}
		
		if(p1.multiply(new BigInteger(Integer.toString(times+1))).equals(result)){
			System.out.println("###### MULTIPLY = SUM #######");
		}
		
		System.out.println("Orden de la curva generada "+ result.getCurve().getOrder());
		
		return result;
	}
	
	public static ECPoint addPoint (ECPoint p1, ECPoint p2){
		return p1.add(p2);
	}
}
