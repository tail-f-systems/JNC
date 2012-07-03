package quagga;
import java.util.*;
import com.tailf.nml.*;
import com.tailf.inm.*;
import com.tailf.inm.confd.*;

public class Tunnel extends Container {
    public Tunnel() {
	super(System.NAMESPACE, "tunnel");
    }
    
    public Tunnel(String nameValue) throws NMLException {
	super(System.NAMESPACE, "tunnel");
	// Add key element
	Leaf name = new Leaf(System.NAMESPACE, "name");
	name.setValue(nameValue);
	insertChild(name, (ArrayList)System.schema.get("tunnel"));
    }
    
    public Tunnel clone() {
	try {
	    return (Tunnel)clone(new Tunnel(getNameValue()));
	} catch (NMLException e) {
	    return null;
	}
    }
    
    /* Leaf: "name" (key) */
    
    public String getNameValue() throws NMLException {
	return (String)getValue("name");
    }
    
    public void setNameValue(String nameValue) throws NMLException {
	setLeafValue(System.NAMESPACE, "name", nameValue,
		     (ArrayList)System.schema.get("tunnel"));
    }
    
    /* Leaf: "local-endpoint" [mandatory] */
    
    public InetAddressIPv4 getLocalEndpointValue() throws NMLException {
	return (InetAddressIPv4)getValue("local-endpoint");
    }

    public void setLocalEndpointValue(InetAddressIPv4 localEndpointValue)
      throws NMLException {
	setLeafValue(System.NAMESPACE, "local-endpoint", localEndpointValue,
		     (ArrayList)System.schema.get("tunnel"));
    }
    
    public void setLocalEndpointValue(String localEndpointValue)
      throws NMLException {
	setLeafValue(System.NAMESPACE, "local-endpoint",
		     new InetAddressIPv4(localEndpointValue),
		     (ArrayList)System.schema.get("tunnel"));
    }
    
    public void unsetLocalEndpointValue() throws NMLException {
	delete("local-endpoint");
    }
    
    public void markLocalEndpointReplace() throws NMLException {
	markLeafReplace("local-endpoint");
    }
    
    public void markLocalEndpointMerge() throws NMLException {
	markLeafMerge("local-endpoint");
    }
    
    public void markLocalEndpointCreate() throws NMLException {
	markLeafCreate("local-endpoint");
    }
    
    /* Leaf: "local-net" [mandatory] */
    
    public InetAddressIPv4 getLocalNetValue() throws NMLException {
	return (InetAddressIPv4)getValue("local-net");
    }
    
    public void setLocalNetValue(InetAddressIPv4 LocalNetValue)
      throws NMLException {
	setLeafValue(System.NAMESPACE, "local-net", LocalNetValue,
		     (ArrayList)System.schema.get("tunnel"));
    }
    
    public void setLocalNetValue(String LocalNetValue)
      throws NMLException {
	setLeafValue(System.NAMESPACE, "local-net",
		     new InetAddressIPv4(LocalNetValue),
		     (ArrayList)System.schema.get("tunnel"));
    }
    
    public void unsetLocalNetValue() throws NMLException {
	delete("local-net");
    }
    
    public void markLocalNetReplace() throws NMLException {
	markLeafReplace("local-net");
    }
    
    public void markLocalNetMerge() throws NMLException {
	markLeafMerge("local-net");
    }
    
    public void markLocalNetCreate() throws NMLException {
	markLeafCreate("local-net");
    }
    
    /* Leaf: "local-net-mask" [mandatory] */
    
    public InetAddressIPv4 getLocalNetMaskValue() throws NMLException {
	return (InetAddressIPv4)getValue("local-net-mask");
    }
    
    public void setLocalNetMaskValue(InetAddressIPv4 localNetMaskValue)
      throws NMLException {
	setLeafValue(System.NAMESPACE, "local-net-mask", localNetMaskValue,
		     (ArrayList)System.schema.get("tunnel"));
    }
    
    public void setLocalNetMaskValue(String localNetMaskValue)
      throws NMLException {
	setLeafValue(System.NAMESPACE, "local-net-mask",
		     new InetAddressIPv4(localNetMaskValue),
		     (ArrayList)System.schema.get("tunnel"));
    }
    
    public void unsetLocalNetMaskValue() throws NMLException {
	delete("local-net-mask");
    }
    
    public void markLocalNetMaskReplace() throws NMLException {
	markLeafReplace("local-net-mask");
    }
    
    public void markLocalNetMaskMerge() throws NMLException {
	markLeafMerge("local-net-mask");
    }
    
    public void markLocalNetMaskCreate() throws NMLException {
	markLeafCreate("local-net-mask");
    }
    
    /* Leaf: "remote-endpoint" [mandatory] */
    
    public InetAddressIPv4 getRemoteEndpointValue() throws NMLException {
	return (InetAddressIPv4)getValue("remote-endpoint");
    }

    public void setRemoteEndpointValue(InetAddressIPv4 remoteEndpointValue)
      throws NMLException {
	setLeafValue(System.NAMESPACE, "remote-endpoint", remoteEndpointValue,
		     (ArrayList)System.schema.get("tunnel"));
    }
    
    public void setRemoteEndpointValue(String remoteEndpointValue)
      throws NMLException {
	setLeafValue(System.NAMESPACE, "remote-endpoint",
		     new InetAddressIPv4(remoteEndpointValue),
		     (ArrayList)System.schema.get("tunnel"));
    }
    
    public void unsetRemoteEndpointValue() throws NMLException {
	delete("remote-endpoint");
    }
    
    public void markRemoteEndpointReplace() throws NMLException {
	markLeafReplace("remote-endpoint");
    }
    
    public void markRemoteEndpointMerge() throws NMLException {
	markLeafMerge("remote-endpoint");
    }
    
    public void markRemoteEndpointCreate() throws NMLException {
	markLeafCreate("remote-endpoint");
    }
    
    /* Leaf: "remote-net" [mandatory] */
    
    public InetAddressIPv4 getRemoteNetValue() throws NMLException {
	return (InetAddressIPv4)getValue("remote-net");
    }
    
    public void setRemoteNetValue(InetAddressIPv4 remoteNet)
      throws NMLException {
	setLeafValue(System.NAMESPACE, "remote-net", remoteNet,
		     (ArrayList)System.schema.get("tunnel"));
    }
    
    public void setRemoteNetValue(String remoteNet)
      throws NMLException {
	setLeafValue(System.NAMESPACE, "remote-net",
		     new InetAddressIPv4(remoteNet),
		     (ArrayList)System.schema.get("tunnel"));
    }
    
    public void unsetRemoteNetValue() throws NMLException {
	delete("remote-net");
    }
    
    public void markRemoteNetReplace() throws NMLException {
	markLeafReplace("remote-net");
    }
    
    public void markRemoteNetMerge() throws NMLException {
	markLeafMerge("remote-net");
    }
    
    public void markRemoteNetCreate() throws NMLException {
	markLeafCreate("remote-net");
    }
    
    /* Leaf: "remote-net-mask" [mandatory] */
    
    public InetAddressIPv4 getRemoteNetMaskValue() throws NMLException {
	return (InetAddressIPv4)getValue("remote-net-mask");
    }
    
    public void setRemoteNetMaskValue(InetAddressIPv4
				      remoteNetMaskValue)
      throws NMLException {
	setLeafValue(System.NAMESPACE, "remote-net-mask", remoteNetMaskValue,
		     (ArrayList)System.schema.get("tunnel"));
    }
    
    public void setRemoteNetMaskValue(String remoteNetMaskValue)
      throws NMLException {
	setLeafValue(System.NAMESPACE, "remote-net-mask",
		     new InetAddressIPv4(remoteNetMaskValue),
		     (ArrayList)System.schema.get("tunnel"));
    }
    
    public void unsetRemoteNetMaskValue() throws NMLException {
	delete("remote-net-mask");
    }
    
    public void markRemoteNetMaskReplace() throws NMLException {
	markLeafReplace("remote-net-mask");
    }
    
    public void markRemoteNetMaskMerge() throws NMLException {
	markLeafMerge("remote-net-mask");
    }
    
    public void markRemoteNetMaskCreate() throws NMLException {
	markLeafCreate("remote-net-mask");
    }

    /* Leaf: "pre-shared-key" [mandatory] */
    
    public String getPreSharedKeyValue() throws NMLException {
	return (String)getValue("pre-shared-key");
    }
    
    public void setPreSharedKeyValue(String preSharedKeyValue)
      throws NMLException {
	setLeafValue(System.NAMESPACE, "pre-shared-key", preSharedKeyValue,
		     (ArrayList)System.schema.get("tunnel"));
    }
    
    public void unsetPreSharedKeyValue() throws NMLException {
	delete("pre-shared-key");
    }
    
    public void markPreSharedKeyReplace() throws NMLException {
	markLeafReplace("pre-shared-key");
    }
    
    public void markPreSharedKeyMerge() throws NMLException {
	markLeafMerge("pre-shared-key");
    }
    
    public void markPreSharedKeyCreate() throws NMLException {
	markLeafCreate("pre-shared-key");
    }
    
    /* Leaf: "encryption-algo" [optional (has default value)] */
    
    public EncryptionAlgo2Type getEncryptionAlgoValue() throws NMLException {
	EncryptionAlgo2Type value =
	    (EncryptionAlgo2Type)getValue("encryption-algo");
	return value == null ? new EncryptionAlgo2Type("default") : value;
    }
    
    public boolean isEncryptionAlgoDefault() throws NMLException {
	return isLeafDefault("encryption-algo");
    }
    
    public void setEncryptionAlgoValue(EncryptionAlgo2Type encryptionAlgoValue)
      throws NMLException {
	setLeafValue(System.NAMESPACE, "encryption-algo", encryptionAlgoValue,
		     (ArrayList)System.schema.get("tunnel"));
    }
    
    public void setEncryptionAlgoValue(String encryptionAlgoValue)
      throws NMLException {
	setLeafValue(System.NAMESPACE, "encryption-algo",
		     new EncryptionAlgo2Type(encryptionAlgoValue),
		     (ArrayList)System.schema.get("tunnel"));
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
    
    public HashAlgo2Type getHashAlgoValue() throws NMLException {
	HashAlgo2Type value = (HashAlgo2Type)getValue("hash-algo");
	return value == null ? new HashAlgo2Type("default") : value;
    }
    
    public boolean isHashAlgoDefault() throws NMLException {
	return isLeafDefault("hash-algo");
    }
    
    public void setHashAlgoValue(HashAlgo2Type encryptionAlgoValue)
      throws NMLException {
	setLeafValue(System.NAMESPACE, "hash-algo", encryptionAlgoValue,
		     (ArrayList)System.schema.get("tunnel"));
    }
    
    public void setHashAlgoValue(String encryptionAlgoValue)
      throws NMLException {
	setLeafValue(System.NAMESPACE, "hash-algo",
		     new HashAlgo2Type(encryptionAlgoValue),
		     (ArrayList)System.schema.get("tunnel"));
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
