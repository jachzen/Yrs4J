module yrs4j.bindings {
    requires com.sun.jna;
    requires kotlin.stdlib;
    requires annotations;
    exports at.yrs4j.api;
    exports at.yrs4j.wrapper;
    exports at.yrs4j.utils;
    exports at.yrs4j.yrslib;
    exports at.yrs4j.wrapper.interfaces;
}