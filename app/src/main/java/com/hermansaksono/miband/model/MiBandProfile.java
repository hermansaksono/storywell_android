package com.hermansaksono.miband.model;

/**
 * Created by hermansaksono on 6/25/18.
 */

public class MiBandProfile {
    private String name = "MI Band 2";
    private String address = null;

    public MiBandProfile(String address) {
        this.address = address;
    }

    public String getName() {
        return this.name;
    }

    public String getAddress() {
        return this.address;
    }
}
