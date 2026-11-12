module order.processing {
    requires java.sql;
    requires order.model;
    requires order.storage;
    requires javafaker;

    exports com.example.order.processing;
}
