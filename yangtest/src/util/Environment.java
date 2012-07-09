package util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Environment {
    
    @SuppressWarnings("unchecked")
    public static Map<String, String> getenv() {
        try {
          Map<String, String> unomdifiable = System.getenv();
          Class<?> cu = unomdifiable.getClass();
          Field m = cu.getDeclaredField("m");
          m.setAccessible(true);
          return (Map<String, String>)m.get(unomdifiable);
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
        } catch (SecurityException se) {
            se.printStackTrace();
        } catch (NoSuchFieldException nsfe) {
            nsfe.printStackTrace();
        }
        return new HashMap<String, String>();
      }
    
    public String getVariable(String var) {
        Map<String, String> envvars = Environment.getenv();
        String value = envvars.get(var);
        if (value == null || value.isEmpty()) {
            value = "~/Dropbox/tail-f/confd";
            String errmsg = "Warning: variable %s defaulting to %s\n";
            System.err.printf(errmsg, var, value);
        }
        return value;
    }
    
}