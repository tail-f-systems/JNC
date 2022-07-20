package com.tailf.jnc.example.intro0;

import com.tailf.jnc.IOSubscriber;

class Subscriber extends IOSubscriber {
    final String devName;
    public static final boolean rawMode = false;

    Subscriber(String devName) {
        super(rawMode);
        this.devName = devName;
    }

    public void input(String s) {
        System.out.println("RECV " + devName);
        System.out.println(s);
    }

    public void output(String s) {
        System.out.println("SEND " + devName);
        System.out.println(s);
    }
}
