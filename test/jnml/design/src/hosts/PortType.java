package hosts;
import com.tailf.nml.*;
import com.tailf.inm.*;

public class PortType extends Uint16 {
    public PortType(String value) throws INMException {
	super(value);
	check();
    }
    
    public PortType(Integer value) throws INMException {
	super(value);
	check();
    }
    
    public void check() throws INMException {
	if (value < 1024 || value > 65535)
	    throw new INMException(INMException.BAD_VALUE, this);
    }
}
