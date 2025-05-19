module cn.zjnu.demos {
    requires javafx.controls;
    requires transitive javafx.graphics;
    requires javafx.base;

    
    opens cn.zjnu.demos to javafx.graphics;
    exports cn.zjnu.demos;
}