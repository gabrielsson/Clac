package cl.sidan.clac.interfaces;

/**
 * Created by Johan on 2014-10-20.
 */
public interface VersionUpdateListener {
    void onVersionUpdateFailed();
    void onVersionUpdateSuccess(boolean isVersionLatest, String versionNumber);
}
