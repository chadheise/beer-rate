package com.manorllc.beerRate.model;

public class Beer {

    private String name;
    private BeerCategory category;
    private String style;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BeerCategory getCategory() {
        return category;
    }

    public void setCategory(BeerCategory category) {
        this.category = category;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

}
