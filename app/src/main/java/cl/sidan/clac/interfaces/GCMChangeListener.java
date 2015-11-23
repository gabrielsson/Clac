package cl.sidan.clac.interfaces;

public interface GCMChangeListener {
    /*
     * Callback method for GCM
     */
    void onGCMChange(boolean success, String msg);
}
