package com.tailf.jnc;

/**
 * The Tagpath class is used to represent the name of individual schema nodes.
 * Each SchemaNode is identified by a Tagpath.
 */
public class Tagpath {
    public String[] p;

    public Tagpath(int size) {
        p = new String[size];
    }

    public Tagpath(String[] tp) {
        p = tp;
    }

    public Tagpath(String s) {
        final String[] tags = s.split("/");
        p = new String[tags.length];
        System.arraycopy(tags, 0, p, 0, tags.length);
    }

    @Override
    public String toString() {
        return String.join("/", p);
    }

    @Override
    public int hashCode() {
        int h = 0;
        for (String element : p) {
            h += element.hashCode();
        }
        return h;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Tagpath) {
            final Tagpath tp = (Tagpath) o;
            if (tp.p.length == p.length) {
                for (int i = 0; i < tp.p.length; i++) {
                    if (!tp.p[i].equals(p[i])) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

}
