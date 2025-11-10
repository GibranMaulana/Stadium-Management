module org.openjfx {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires java.sql;
    requires de.jensd.fx.glyphs.fontawesome;
    requires de.jensd.fx.glyphs.commons;
    exports org.openjfx;
    exports org.openjfx.controller;
    exports org.openjfx.model;
    exports org.openjfx.service;
    exports org.openjfx.util;
}
