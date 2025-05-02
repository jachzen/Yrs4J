package at.yrs4j.api;

import at.yrs4j.utils.JNAUtils;
import at.yrs4j.wrapper.interfaces.YDoc;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import org.jetbrains.annotations.Nullable;

/**
 * Thin OO façade for the native YAwarenessw* pointer.
 * <p>
 *   – no raw Pointer juggling in application code<br>
 *   – automatic native-buffer free‐ing<br>
 *   – JSON helpers for cursor, user-meta, etc.
 */
public final class YAwarenessW {

    /* ---------- FIELDS & CTOR ------------------------------------------------------ */

    private final YrsLibNativeInterface lib = Yrs4J.YRS_INSTANCE;   // your loader

    private final Pointer ptr;                          // <─ native YAwarenessw*

    /** Create a fresh awareness object bound to an existing YDoc. */
    public YAwarenessW(YDoc doc) {
        this.ptr = lib.y_awareness_new(doc.getWrappedObject());
    }

    /** Wrap an existing native pointer (rarely needed). */
    public YAwarenessW(Pointer nativePtr) {
        this.ptr = nativePtr;
    }

    /* ---------- LIFECYCLE ---------------------------------------------------------- */

    public void destroy() {
        lib.y_awareness_destroy(ptr);
    }

    /* ---------- LOCAL STATE -------------------------------------------------------- */

    /** Replace the whole local JSON blob (null ⇒ clean). */
    public void setLocalState(@Nullable String json) {
        lib.y_awareness_set_local_state(ptr, json);
    }

    /** Returns the current local JSON (or null if unset). */
    public @Nullable String getLocalState() {
        Pointer raw = lib.y_awareness_get_local_state(ptr);
        if (raw == null)
            return null;

        String out = raw.getString(0);
        lib.ystring_destroy(raw);        // same helper you already have
        return out;
    }

    /* ---------- UPDATES ------------------------------------------------------------ */

    /**
     * Encode a diff for all (or selected) clients so you can ship it via WebSocket.
     *
     * @param clientIds empty → all clients
     */
    public byte[] encodeUpdate(long... clientIds) {
        IntByReference lenRef = new IntByReference();
        Pointer buf = lib.y_awareness_encode_update(
                ptr,
                (clientIds == null || clientIds.length == 0) ? null : clientIds,
                (clientIds == null) ? 0 : clientIds.length,
                lenRef
        );
        int len = lenRef.getValue();
        byte[] out = buf.getByteArray(0, len);
        lib.ybinary_destroy(buf, len);   // free Rust-side malloc
        return out;
    }

    public void removeStates(long... clientIds) {
        if (clientIds == null || clientIds.length == 0) return;
        lib.y_awareness_remove_states(ptr, clientIds, clientIds.length);
    }

    /** Raw JSON string of <clientId → JSON>.  Null when no remote states. */
    public @Nullable String getStates() {
        Pointer p = lib.y_awareness_get_states(ptr);
        if (p == null) return null;
        String s = p.getString(0);
        lib.ystring_destroy(p);              // free native string
        return s;
    }


    /** Apply an update received from the network. */
    public void applyUpdate(byte[] bytes) {
        Memory mem = JNAUtils.memoryFromByteArray(bytes);
        lib.y_awareness_apply_update(ptr, mem, bytes.length);
    }

    public Pointer getNativePtr() { return ptr; }
}
