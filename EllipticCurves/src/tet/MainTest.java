package tet;
import org.bouncycastle.asn1.x9.X9ECParameters;

public class MainTest {

	public static void main(String[] args) {
		AppECI aec = new AppEllipticCurve();
	
		String[] namedCurves = {
			"c2pnb163v1",
			"c2tnb239v1",
			"B-283",
			"sect283k1",
			"sect113r1"
		};
		
		X9ECParameters params = null;
		try {
			params = aec.newEC(namedCurves[4]);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			System.out.println("Nombre de la curva no existente");
		} catch (CurveNotLoaded e) {
			e.printStackTrace();
			System.out.println("La curva no se ha cargado");
		}
		
	}
	
}
