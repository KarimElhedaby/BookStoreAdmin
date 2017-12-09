package com.hamza.alif.bookstore.data;

import java.io.Serializable;

/**
 * Created by karim pc on 10/27/2017.
 */

public class Publisher implements Serializable{
    private String email;
    private String name;
    private String id;
    private String address;
    private String imageUrl;

    public Publisher(String email, String id) {
        this.email = email;
        this.id = id;
    }

    public Publisher() {
    }

    public String getEmail() {
        return email;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


}
