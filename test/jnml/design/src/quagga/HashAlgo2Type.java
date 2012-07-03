package quagga;
import com.tailf.nml.*;
import com.tailf.inm.*;
import java.io.Serializable;

public class HashAlgo2Type implements Serializable  {
    private String value;    
    
    public HashAlgo2Type(String value) throws NMLException {	
	this.value = value;
	check();
    }
    
    public void check() throws INMException {
	if (!(value.equals("default") ||
	      value.equals("md5") ||
	      value.equals("sha1") ||
	      value.equals("sha256")))
	    throw new INMException(INMException.BAD_VALUE, this);
    }
    
    public String toString() {
	return value;
    }
}
