package at.yrs4j.api;

import at.yrs4j.wrapper.interfaces.YDoc;
import at.yrs4j.yrslib.YrsStickyIndex;
import com.sun.jna.Pointer;

/** Static helpers for JSON ←→ YrsStickyIndex conversions. */
public final class StickyJson {
    private static final YrsLibNativeInterface lib = Yrs4J.YRS_INSTANCE;   // your loader

    /** Parse JSON produced by y-codemirror into a native sticky index. */
    public static YrsStickyIndex fromJson(String json) {
        return lib.ysticky_index_from_json(json);
    }

    /** Convert a sticky index back into the JSON structure CodeMirror expects. */
    public static String toJson(YrsStickyIndex idx) {
        Pointer p = lib.ysticky_index_to_json(idx);
        if (p == null) return null;
        String out = p.getString(0);
        lib.ystring_destroy(p);          // free native string
        return out;
    }

    private StickyJson() {}              // static-only

    public static final class AbsolutePos {
        public final int  index;
        public final byte assoc;
        AbsolutePos(int idx, byte as) { this.index = idx; this.assoc = as; }
    }

    /** Kotlin-friendly one-liner:  StickyIndex → (index, assoc) */
    public static AbsolutePos toAbsolute(YrsStickyIndex sticky, YDoc doc) {
        Pointer p = lib.y_absolute_from_sticky_index(sticky, doc.getWrappedObject());
        if (p == null)
            return null;                       // could not be resolved (item deleted?)
        int  index = p.getInt(0);              // uint32
        byte assoc = p.getByte(4);             // int8
        lib.y_absolute_position_destroy(p);
        return new AbsolutePos(index, assoc);
    }
}
