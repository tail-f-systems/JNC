package quagga;
import java.util.*;
import com.tailf.nml.*;
import com.tailf.inm.*;

public class Defaults extends Container {
    public Defaults() {
	super(System.NAMESPACE, "defaults");
    }
    
    public Defaults clone() {
	return (Defaults)clone(new Defaults());
    }
    
    /* Leaf: "encryption-algo" [optional (has default value)] */
    
    public EncryptionAlgoType getEncryptionAlgoValue() throws NMLException {
	EncryptionAlgoType value =
	    (EncryptionAlgoType)getValue("encryption-algo");
	return value == null ? new EncryptionAlgoType("des") : value;
    }
    
    public boolean isEncryptionAlgoDefault() throws NMLException {
	return isLeafDefault("encryption-algo");
    }
    
    public void setEncryptionAlgoValue(EncryptionAlgoType encryptionAlgoValue)
      throws NMLException {
	setLeafValue(System.NAMESPACE, "encryption-algo", encryptionAlgoValue,
		     (ArrayList)System.schema.get("defaults"));
    }
    
    public void setEncryptionAlgoValue(String encryptionAlgoValue)
      throws NMLException {
	setLeafValue(System.NAMESPACE, "encryption-algo",
		     new EncryptionAlgoType(encryptionAlgoValue),
		     (ArrayList)System.schema.get("defaults"));
    }
    
    public void unsetEncryptionAlgoValue() throws NMLException {
	delete("encryption-algo");
    }
    
    public void markEncryptionAlgoReplace() throws NMLException {
	markLeafReplace("encryption-algo");
    }
    
    public void markEncryptionAlgoMerge() throws NMLException {
	markLeafMerge("encryption-algo");
    }
    
    public void markEncryptionAlgoCreate() throws NMLException {
	markLeafCreate("encryption-algo");
    }
    
    /* Leaf: "hash-algo" [optional (has default value)] */
    
    public HashAlgoType getHashAlgoValue() throws NMLException {
	HashAlgoType value = (HashAlgoType)getValue("hash-algo");
	return value == null ? new HashAlgoType("md5") : value;
    }
    
    public boolean isHashAlgoDefault() throws NMLException {
	return isLeafDefault("hash-algo");
    }
    
    public void setHashAlgoValue(HashAlgoType encryptionAlgoValue)
      throws NMLException {
	setLeafValue(System.NAMESPACE, "hash-algo", encryptionAlgoValue,
		     (ArrayList)System.schema.get("defaults"));
    }
    
    public void setHashAlgoValue(String encryptionAlgoValue)
      throws NMLException {
	setLeafValue(System.NAMESPACE, "hash-algo",
		     new HashAlgoType(encryptionAlgoValue),
		     (ArrayList)System.schema.get("defaults"));
    }
    
    public void unsetHashAlgoValue() throws NMLException {
	delete("hash-algo");
    }
    
    public void markHashAlgoReplace() throws NMLException {
	markLeafReplace("hash-algo");
    }
    
    public void markHashAlgoMerge() throws NMLException {
	markLeafMerge("hash-algo");
    }
    
    public void markHashAlgoCreate() throws NMLException {
	markLeafCreate("hash-algo");
    }
}
