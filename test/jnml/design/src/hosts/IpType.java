package hosts;
import com.tailf.nml.*;
import com.tailf.inm.*;

public class IpType {
    private String value;    
    
    public IpType(String value) throws NMLException {	
	this.value = value;
	check();
    }
    
    public void check() throws INMException {
	if (value.length() < 7 || value.length() > 15)
	    throw new INMException(INMException.BAD_VALUE, this);

	if (!value.matches("(([0-1]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.){3}([0-1]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])(%[\\p{N}\\p{L}]+)?"))
	    throw new INMException(INMException.BAD_VALUE, this);
    }
    
    public String toString() {
	return value;
    }
}
