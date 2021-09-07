package org.example;

public enum Catalog {

    MOTHERBOARDS("Материнские платы"),
    INTEL1200("Intel Socket 1200"),
    CASES("Корпуса"),
    AEROCOOL("AEROCOOL");

    public String nameCatalog;

    Catalog(String nameCatalog) {
        this.nameCatalog = nameCatalog;
    }
}
