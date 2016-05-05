import java.math.BigInteger;
import java.util.zip.DataFormatException;

import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.util.encoders.Hex;

public class MainTest {

	public static void main(String[] args) {
		AppECI aec = new AppEllipticCurve();
		
		BigInteger a = new BigInteger("1"),b = new BigInteger("6");
		int field = 11;
		
		ECParameterSpec params = null;
		try {
			params = aec.newEC(field, a, b);
		} catch (DataFormatException e) {
			e.printStackTrace();
			System.out.println("Fail al crear new EC");
		}
		System.out.println("Curve params seed" + params.getSeed().toString());
		
		String t = "883423532389192164791648750360308885314476597252960362792450860609699839";
		byte[] decoded = Hex.decode("020ffa963cdca8816ccc33b8642bedf905c3d358573d3f27fbbd3b3cb9aaaf");
		BigInteger deco = new BigInteger(decoded);
		
		String ec = "883423532389192164791648750360308885314476597252960362792450860609699839";
		System.out.println("T length : "+ t.length());
		System.out.println("EC length : "+ ec.length());
		System.out.println("Decoded : "+ deco.bitLength());
		
	}
	
}
