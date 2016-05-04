/**
 * Exception that arrises when a curve is not loaded to work with 
 * @author fidel
 *
 */
public class CurveNotLoaded extends Exception {
	private static final long serialVersionUID = -4659105676805805716L;

		public CurveNotLoaded() {
	        super();
	    }
	
		public CurveNotLoaded(String message) {
	        super(message);
	    }
		
		public CurveNotLoaded(Throwable cause) {
	        super(cause);
	    }

	    public CurveNotLoaded(String message, Throwable cause) {
	        super(message, cause);
	    }
}
