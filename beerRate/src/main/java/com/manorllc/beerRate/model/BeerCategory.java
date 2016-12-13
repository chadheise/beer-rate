package com.manorllc.beerRate.model;

public enum BeerCategory {

    LAGER_PILSNER("Lager & Pilsner "), BELGIAN("Belgian"), SOUR("Sours, Fruits, & Flavors"), WHEAT("Wheat"), PALE_ALE(
            "Pale Ales"), DARK("Stouts, Porters, & Dark Ales");

    private final String fullName;

    private BeerCategory(final String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }

    public static BeerCategory getByFullName(final String fullName) {
        for (BeerCategory category : BeerCategory.values()) {
            if (category.fullName.equals(fullName)) {
                return category;
            }
        }

        throw new IllegalArgumentException(String.format("No beer category exists for %s", fullName));

    }

}
