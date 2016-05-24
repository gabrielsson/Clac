package cl.sidan.clac.access.interfaces;



import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by max.gabrielsson on 2014-10-14.
 */
public interface SidanAccess {
    /**
     * Reads all comming Arrs
     * @return list of arr
     */
    public List<Arr> readArr(String date);

    /**
     * Reads take number of articles starting from skip
     * The articles are from the famous newspaper nätblaskan
     * @param skip
     * @param take
     * @return list of articles
     */
    public List<Article> readArticles(int skip, int take);

    /**
     * reads take number of entries starting from skip
     * @param skip
     * @param take
     * @return list of entry
     */
    public List<Entry> readEntries(int skip, int take);

    /**
     * search take number of entries starting from skip
     * @param searchString a string containing search sentence.
     * @param skip
     * @param take
     *
     * @return list of entry
     */
    public List<Entry> searchEntries(String searchString, int skip, int take);
    /**
     * Reads [take] number of entries from the id (inclusive, more than or equal to id)
      * @param take
     * @param id
     * @return list of entry
     */
    public List<Entry> readEntriesFromId(int take, Integer id);

    /**
     * Reads [take] number of entries until the id ( less than id)
     * @param take
     * @param id
     * @return list of entry
     */
    public List<Entry> readEntriesToId(int take, Integer id);

    /**
     * Reads one entry
     * @param id
     * @return the entry corresponding to the id.
     */
    public Entry readEntry(Integer id);

    /**
     * Appends a row to the end of the message
     * @param id
     * @param message
     */
    public void editEntry(Integer id, String message);

    /**
     * deletes entry corresponding to the id
     * @param id
     */
    public void deleteEntry(Integer id);

    /**
     * Creates a new entry
     * @param message
     * @param latitude
     * @param longitude
     * @param enheter
     * @param status     *
     * @param sideKicks
     * @return whether or not the call was successfull.
     */
    public boolean createEntry(String message, BigDecimal latitude, BigDecimal longitude, Integer enheter, Integer status, String host, Boolean secret, String base64Image, String fileName, List<User> sideKicks);

    /**
     * Creates a like on entry corresponding to the id
     * @param id
     * @param uniqueIdentifier is used to make sure the user can only like entry once. Typically the host.
     */
    public void createLike(Integer id, String uniqueIdentifier);

    /**
     * Create or update a new Arr
     * @param id will be -1 for a new arr
     * @param namn
     * @param plats
     * @param datum
     */
    public boolean createOrUpdateArr(Integer id, String namn, String plats, String datum);

    /**
     * Register a number to an Arr
     * @param id
     */
    public void registerArr(Integer id);

    /**
     * Unregister a number to an Arr
     * @param id
     */
    public void unregisterArr(Integer id);

    /**
     * Lurpassa on an Arr
     * @param id
     */
    public void lurpassaOnArr(Integer id);

    /**
     * Remove lurpassa from Arr
     * @param id
     */
    public void lurpassaOffArr(Integer id);

    /**
     * Vote on a poll
     * @param id
     * @param votedOnYay
     */
    public boolean votePoll(Integer id, Integer votedOnYay);

    /**
     * Get the latest Poll
     */
    public Poll readPoll();

    /**
     * Creates or updates a GCM registration id for a user
     * @param regId
     * @param deviceId from Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)
     */
    public void registerGCM(String regId, String deviceId);

    /**
     * Disables the regId for the current user
     * @param regId
     */
    public void unregisterGCM(String regId);

    /**
     * Disables the regId for the current user
     * @param csvSigs is a comma separated list with signatures
     * @return Map with <regId, signature>
     */
    public Map<String,String> getGCMRecipients(String csvSigs);

    /**
     * Retreives the reg id for the device if it's active
     * @param deviceId
     * @return The regId or null
     */
    String getGCMRegIdFromDeviceId(String deviceId);

    /**
     * Just tries access to WS
     * @return success
     */

    public boolean authenticateUser();


    List<User> readMembers(boolean onlyValidMembers);

    boolean updatePassword(String forSignature, String password, String admin);

    UpdateInfo checkForUpdates();

    /**
     * Read the current top lists of a given type
     * @param type
     * @return List of top users
     */
    List<Stats> readStats(String type);

    public boolean ignoreNumber(String number);
}
