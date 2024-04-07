package com.awokens.movingborder.Manager;

import java.util.ArrayList;
import java.util.List;

public class WikiManager {
    private String title;
    private String author;
    private List<String> pages;

    public WikiManager() {
        this.pages = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<String> getPages() {
        return pages;
    }

    public void setPages(List<String> pages) {
        this.pages = pages;
    }
}
