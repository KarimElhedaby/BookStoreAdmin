package com.hamza.alif.bookstore.data;

import java.io.Serializable;


import java.io.Serializable;

public class Book implements Serializable{
    private String id;
    private String title;
    private String description;
    private String pdfUrl;
    private String imageUrl;
    private double price;
    private String publisherId;
    private String date;
    private String pdfTitle;

    public Book() {
    }

    public Book(String id, String title, String description
            , double price,String date, String pdfTitle, String publisherId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.publisherId = publisherId;
        this.date = date;
        this.pdfTitle = pdfTitle;
    }

    public String getPdfTitle() {
        return pdfTitle;
    }

    public void setPdfTitle(String pdfTitle) {
        this.pdfTitle = pdfTitle;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Book) {
            Book book = (Book) obj;
            return this.getId().equals(book.getId());
        }
        return super.equals(obj);
    }
}
