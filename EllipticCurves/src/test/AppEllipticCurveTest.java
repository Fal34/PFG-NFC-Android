package test;

import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.math.BigInteger;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import exceptions.CurveNotLoaded;
import main.AppEllipticCurve;

public class AppEllipticCurveTest {
	
    // Attr
    private BigInteger k,k2;
    private ECPoint.F2m p1,p2,p3;
    private AppEllipticCurve aec;

	@Rule
	  public final ExpectedException exception = ExpectedException.none();
	
    @Before
    public void setUp() throws Exception {
    	this.aec = new AppEllipticCurve();
    }

    @Test
    public void loadCurveTest(){
    	String curveName = "c2pnb163v1", curveNameFalse="false curve name"; // Curve names to use
    	try {
			this.aec.loadEC(curveName, null);
			
			// Reflection params access
	    	Field f = aec.getClass().getDeclaredField("ecurve"); 
	    	f.setAccessible(true);
	    	ECCurve.F2m createdParams = (ECCurve.F2m) f.get(aec); 
	    	// c2pnb163v1 true order
	    	BigInteger order = new BigInteger("5846006549323611672814741626226392056573832638401");
	    	Assert.assertTrue(createdParams.getOrder().equals(order)); //check equals
	    	
	    	// Test loadEC with key
	    	k = new BigInteger("1370679717587290936099132961293636524819910709962");
	    	this.aec.loadEC(curveName, k);
	    	
	    	// Reflection k access
	    	f = aec.getClass().getDeclaredField("k");
	    	f.setAccessible(true);
	    	// Gets loaded k 
	    	BigInteger createdK = (BigInteger) f.get(aec);
	    	Assert.assertTrue(createdK.equals(k)); // check equals
	    	
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception occured");
		}
    	
    	// Exception assertion
    	try {
			this.aec.loadEC(curveNameFalse, null);
			fail("Expected exception was not occured.");
		} catch (IllegalArgumentException e) {
			assert true;
		}
    	
    }
    
    @Test
    public void newKeyTest(){
    	k = this.aec.newKey();
    	k2 = this.aec.newKey();
    	
    	Assert.assertNotEquals(k, k2);    	
    	Assert.assertTrue(k.bitLength() == AppEllipticCurve.DEFAULT_KEY_SIZE);
    }
    
    @Test
    public void getRandomPointTest(){
    	this.aec = new AppEllipticCurve();
    	
    	// Except assertion
    	try{
    		p1 = this.aec.getRandomPoint();
    	}catch(CurveNotLoaded e){
    		assert true;
    	}
    	
    	// Get random point assert
    	this.aec.loadEC("c2pnb163v1", null);
    	try{
    		p1 = this.aec.getRandomPoint();
    		Assert.assertEquals(p1.getClass(), ECPoint.F2m.class);
    	}catch(CurveNotLoaded e){
    		fail("Exception not expected occured");
    		e.printStackTrace();
    	}
    }
    
    @Test
    public void addPoint(){
    	int n = 1;
    	this.aec.loadEC("c2pnb163v1", null);
    	
    	// Exception assert
    	try {
			p1 = this.aec.getRandomPoint();
			p2 = this.aec.addPoint(p1, n);
			p3 = this.aec.addPointsCustom(p1,p1);
			// Check if p1+p1 = 2*p1
			Assert.assertEquals(p2,p3);
			
			p2 = this.aec.addPoint(p1, 2);
			p3 = this.aec.addPoint(p1, 3);
			ECPoint.F2m p3b = this.aec.addPointsCustom(p2, p1);
			
			// Check if (p1+p1)+p1 = 3*p1
			Assert.assertEquals(p3b,p3);
		} catch (CurveNotLoaded e) {
			e.printStackTrace();
			fail("Exception not expected occured");
		}
    }
    
    @Test
    public void encodeAndDecodeTest(){
    	String val = "";
    	this.aec = new AppEllipticCurve();
    	// Except assertion
    	try {
			p1 = this.aec.decode(val);
			fail("Expected exception not occured");
		} catch (CurveNotLoaded e) {
			e.printStackTrace();
			assert true;
		}
    	// Except assertion
    	try {
			val = this.aec.encode(p1);
			fail("Expected exception not occured");
		} catch (CurveNotLoaded e) {
			e.printStackTrace();
			assert true;
		}
    	
    	// Test methods with a curve loaded
    	k = new BigInteger("1370679717587290936099132961293636524819910709962");
    	this.aec.loadEC("c2pnb163v1", k);
    	try {
    		p1 = this.aec.getRandomPoint();
    		val = this.aec.encode(p1);
			p2 = this.aec.decode(val);
			// Assert points from encoded value
			Assert.assertEquals(p1,p2);
		} catch (CurveNotLoaded e) {
			e.printStackTrace();
			fail("Exception not expected occured");
		}
    }

}
