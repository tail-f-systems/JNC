package quagga;
import com.tailf.nml.*;
import com.tailf.inm.*;
import java.io.Serializable;

public class EncryptionAlgo2Type implements Serializable {
    private String value;    
    
    public EncryptionAlgo2Type(String value) throws NMLException {	
	this.value = value;
	check();
    }
    
    public void check() throws INMException {
	if (!(value.equals("default") ||
	      value.equals("des") ||
	      value.equals("3des") ||
	      value.equals("blowfish") ||
	      value.equals("cast128") ||
	      value.equals("aes")))
	    throw new INMException(INMException.BAD_VALUE, this);
    }
    
    public String toString() {
	return value;
    }
}
