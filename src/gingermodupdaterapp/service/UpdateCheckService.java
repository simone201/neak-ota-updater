package gingermodupdaterapp.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.*;
import android.widget.Toast;
import gingermodupdaterapp.customTypes.FullUpdateInfo;
import gingermodupdaterapp.customTypes.ExtraInfo;
import gingermodupdaterapp.customTypes.ExtraList;
import gingermodupdaterapp.customTypes.UpdateInfo;
import gingermodupdaterapp.customization.Customization;
import gingermodupdaterapp.interfaces.IUpdateCheckService;
import gingermodupdaterapp.interfaces.IUpdateCheckServiceCallback;
import gingermodupdaterapp.misc.Constants;
import gingermodupdaterapp.misc.Log;
import gingermodupdaterapp.misc.State;
import gingermodupdaterapp.ui.MainActivity;
import gingermodupdaterapp.ui.R;
import gingermodupdaterapp.utils.Preferences;
import gingermodupdaterapp.utils.StringUtils;
import gingermodupdaterapp.utils.SysUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.LinkedList;

public class UpdateCheckService extends Service {
    private static final String TAG = "UpdateCheckService";

    private static Boolean showDebugOutput = false;

    private final RemoteCallbackList<IUpdateCheckServiceCallback> mCallbacks = new RemoteCallbackList<IUpdateCheckServiceCallback>();
    private Preferences mPreferences;
    private String systemMod;
    private String systemRom;
    private ExtraInfo extraInfos;
    private boolean showExperimentalRomUpdates;
    private boolean showAllRomUpdates;
    private boolean showExperimentalExtraUpdates;
    private boolean showAllExtraUpdates;
    private boolean WildcardUsed = false;
    private int PrimaryKeyExtra = -1;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        mPreferences = new Preferences(this);
        showDebugOutput = mPreferences.displayDebugOutput();
        systemMod = mPreferences.getBoardString();
        if (systemMod == null) {
            if (showDebugOutput)
                Log.d(TAG, "Unable to determine System's Mod version. Updater will show all available updates");
        } else {
            if (showDebugOutput) Log.d(TAG, "System's Mod version:" + systemMod);
        }
    }

    @Override
    public void onDestroy() {
        mCallbacks.kill();
        super.onDestroy();
    }

    private final IUpdateCheckService.Stub mBinder = new IUpdateCheckService.Stub() {
        public void registerCallback(IUpdateCheckServiceCallback cb) throws RemoteException {
            if (cb != null) mCallbacks.register(cb);
        }

        public void unregisterCallback(IUpdateCheckServiceCallback cb) throws RemoteException {
            if (cb != null) mCallbacks.unregister(cb);
        }

        public void checkForUpdates() throws RemoteException {
            checkForNewUpdates();
        }
    };

    private void DisplayExceptionToast(String ex) {
        ToastHandler.sendMessage(ToastHandler.obtainMessage(0, ex));
    }

    private void checkForNewUpdates() {
        FullUpdateInfo availableUpdates;
        while (true) {
            try {
                if (showDebugOutput) Log.d(TAG, "Checking for updates...");
                availableUpdates = getAvailableUpdates();
                break;
            }
            catch (IOException ex) {
                Log.e(TAG, "IOEx while checking for updates", ex);
                notificateCheckError(ex.getMessage());
                return;
            }
            catch (RuntimeException ex) {
                Log.e(TAG, "RuntimeEx while checking for updates", ex);
                notificateCheckError(ex.getMessage());
                return;
            }
        }

        mPreferences.setLastUpdateCheck(new Date());

        int updateCountRoms = availableUpdates.getRomCount();
        int updateCountIncrementalRoms = availableUpdates.getIncrementalRomCount();
        int updateCountExtras = availableUpdates.getExtraCount();
        int updateCount = availableUpdates.getUpdateCount();
        if (showDebugOutput) Log.d(TAG, updateCountRoms + " ROM update(s) found; " +
                updateCountIncrementalRoms + " incremental ROM udpate(s) found; " +
                updateCountExtras + " Extra update(s) found");

        if (updateCountRoms == 0 && updateCountExtras == 0 && updateCountIncrementalRoms == 0) {
            if (showDebugOutput) Log.d(TAG, "No updates found");
            ToastHandler.sendMessage(ToastHandler.obtainMessage(0, R.string.no_updates_found, 0));
            FinishUpdateCheck();
        } else {
            if (mPreferences.notificationsEnabled()) {
                Intent i = new Intent(this, MainActivity.class);
                PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_ONE_SHOT);

                Resources res = getResources();
                Notification notification = new Notification(R.drawable.icon_notification,
                        res.getString(R.string.not_new_updates_found_ticker),
                        System.currentTimeMillis());

                //To remove the Notification, when the User clicks on it
                notification.flags = Notification.FLAG_AUTO_CANCEL;

                String text = MessageFormat.format(res.getString(R.string.not_new_updates_found_body), updateCount);
                notification.setLatestEventInfo(this, res.getString(R.string.not_new_updates_found_title), text, contentIntent);

                Uri notificationRingtone = mPreferences.getConfiguredRingtone();
                if (mPreferences.getVibrate())
                    notification.defaults = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;
                else
                    notification.defaults = Notification.DEFAULT_LIGHTS;
                notification.sound = notificationRingtone;

                //Use a resourceId as an unique identifier
                ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(R.string.not_new_updates_found_title, notification);
            }
            FinishUpdateCheck();
        }
    }

    private void notificateCheckError(String ExceptionText) {
        DisplayExceptionToast(ExceptionText);
        if (showDebugOutput) Log.d(TAG, "Update check error");
        FinishUpdateCheck();
    }

    private FullUpdateInfo getAvailableUpdates() throws IOException {
        FullUpdateInfo retValue = new FullUpdateInfo();
        boolean romException = false;
        HttpClient romHttpClient = new DefaultHttpClient();
        HttpClient extraHttpClient = new DefaultHttpClient();
        HttpEntity romResponseEntity = null;
        HttpEntity extraResponseEntity = null;
        systemRom = SysUtils.getModVersion();
        if (Customization.Screenshotsupport) extraInfos = mPreferences.getExtraInformations();
        showExperimentalRomUpdates = mPreferences.showExperimentalRomUpdates();
        showAllRomUpdates = mPreferences.showAllRomUpdates();
        boolean ExtraUpdateUrlSet = false;
        if (Customization.Screenshotsupport) {
            showExperimentalExtraUpdates = mPreferences.showExperimentalExtraUpdates();
            showAllExtraUpdates = mPreferences.showAllExtraUpdates();
            ExtraUpdateUrlSet = mPreferences.ExtraUpdateUrlSet();

            //If Wildcard is used or no extras.extra file present set the variable
            if (extraInfos == null || extraInfos.name.equalsIgnoreCase(Constants.UPDATE_INFO_WILDCARD)) {
                if (showDebugOutput) Log.d(TAG, "Wildcard is used for Extra Updates");
                extraInfos = new ExtraInfo();
                WildcardUsed = true;
            }
        }
        //Get the actual Rom Updateserver URL
        try {
            URI RomUpdateServerUri = URI.create(mPreferences.getRomUpdateFileURL());
            HttpUriRequest romReq = new HttpGet(RomUpdateServerUri);
            romReq.addHeader("Cache-Control", "no-cache");
            HttpResponse romResponse = romHttpClient.execute(romReq);
            int romServerResponse = romResponse.getStatusLine().getStatusCode();
            if (romServerResponse != HttpStatus.SC_OK) {
                if (showDebugOutput) Log.d(TAG, "Server returned status code for ROM " + romServerResponse);
                romException = true;
            }
            if (!romException)
                romResponseEntity = romResponse.getEntity();
        }
        catch (IllegalArgumentException e) {
            if (showDebugOutput) Log.d(TAG, "Rom Update URI wrong: " + mPreferences.getRomUpdateFileURL());
            romException = true;
        }

        //Get the actual Extra Updateserver URL
        if (Customization.Screenshotsupport && ExtraUpdateUrlSet) {
            try {
                LinkedList<ExtraList> tl = mPreferences.getExtraUpdateUrls();
                for (ExtraList t : tl) {
                    if (!t.enabled) {
                        if (showDebugOutput) Log.d(TAG, "Extra " + t.name + " disabled. Continuing");
                        continue;
                    }
                    PrimaryKeyExtra = -1;
                    if (showDebugOutput) Log.d(TAG, "Trying to download ExtraInfos for " + t.url.toString());
                    URI ExtraUpdateServerUri = t.url;
                    HttpUriRequest extraReq = new HttpGet(ExtraUpdateServerUri);
                    extraReq.addHeader("Cache-Control", "no-cache");
                    try {
                        HttpResponse extraResponse = extraHttpClient.execute(extraReq);
                        int extraServerResponse = extraResponse.getStatusLine().getStatusCode();
                        if (extraServerResponse != HttpStatus.SC_OK) {
                            if (showDebugOutput)
                                Log.d(TAG, "Server returned status code for Extras " + extraServerResponse);
                            extraResponseEntity = extraResponse.getEntity();
                            continue;
                        }
                        extraResponseEntity = extraResponse.getEntity();
                    }
                    catch (IOException ex) {
                        //when theres an Exception Downloading the Extra, continue
                        DisplayExceptionToast(getResources().getString(R.string.extra_download_exception) + t.name + ": " + ex.getMessage());
                        Log.e(TAG, "There was an error downloading Extra " + t.name + ": ", ex);
                        continue;
                    }
                    //Read the Extra Infos
                    BufferedReader extraLineReader = new BufferedReader(new InputStreamReader(extraResponseEntity.getContent()), 2 * 1024);
                    StringBuffer extraBuf = new StringBuffer();
                    String extraLine;
                    while ((extraLine = extraLineReader.readLine()) != null) {
                        extraBuf.append(extraLine);
                    }
                    extraLineReader.close();

                    //Set the PrimaryKey for the Database
                    if (t.PrimaryKey > 0)
                        PrimaryKeyExtra = t.PrimaryKey;

                    LinkedList<UpdateInfo> extraUpdateInfos = parseJSON(extraBuf, RomType.Update);
                    retValue.extras.addAll(getExtraUpdates(extraUpdateInfos));
                }
            }
            catch (IllegalArgumentException e) {
                if (showDebugOutput) Log.d(TAG, "Extra Update URI wrong");
                //extraException = true;
            }
        }

        try {
            if (!romException) {
                PrimaryKeyExtra = -1;
                //Read the Rom Infos
                BufferedReader romLineReader = new BufferedReader(new InputStreamReader(romResponseEntity.getContent()), 2 * 1024);
                StringBuffer romBuf = new StringBuffer();
                String romLine;
                while ((romLine = romLineReader.readLine()) != null) {
                    romBuf.append(romLine);
                }
                romLineReader.close();

                LinkedList<UpdateInfo> romUpdateInfos = parseJSON(romBuf, RomType.Update);
                retValue.roms = getRomUpdates(romUpdateInfos);
                LinkedList<UpdateInfo> incrementalRomUpdateInfos = parseJSON(romBuf, RomType.IncrementalUpdate);
                retValue.incrementalRoms = getIncrementalRomUpdates(incrementalRomUpdateInfos);
            } else if (showDebugOutput) Log.d(TAG, "There was an Exception on Downloading the Rom JSON File");
        }
        finally {
            if (romResponseEntity != null)
                romResponseEntity.consumeContent();
            if (extraResponseEntity != null)
                extraResponseEntity.consumeContent();
        }

        FullUpdateInfo ful = FilterUpdates(retValue, State.loadState(this, showDebugOutput));
        if (!romException)
            State.saveState(this, retValue, showDebugOutput);
        return ful;
    }

    private enum RomType {
        Update, IncrementalUpdate
    }

    private LinkedList<UpdateInfo> parseJSON(StringBuffer buf, RomType type) {
        LinkedList<UpdateInfo> uis = new LinkedList<UpdateInfo>();

        JSONObject mainJSONObject;

        try {
            mainJSONObject = new JSONObject(buf.toString());
            JSONArray mirrorList = mainJSONObject.getJSONArray(Constants.JSON_MIRROR_LIST);
            if (showDebugOutput) Log.d(TAG, "Found " + mirrorList.length() + " mirrors in the JSON");

            switch (type) {
                case Update:
                    JSONArray updateList = mainJSONObject.getJSONArray(Constants.JSON_UPDATE_LIST);
                    if (showDebugOutput) Log.d(TAG, "Found " + updateList.length() + " updates in the JSON");
                    for (int i = 0, max = updateList.length(); i < max; i++) {
                        if (!updateList.isNull(i))
                            uis.add(parseUpdateJSONObject(updateList.getJSONObject(i), mirrorList));
                        else
                            Log.e(TAG, "Theres an error in your JSON File(update part). Maybe a , after the last update");
                    }
                    break;
                case IncrementalUpdate:
                    if (mainJSONObject.has(Constants.JSON_INCREMENTAL_UPDATES)) {
                        JSONArray incrementalUpdateList = mainJSONObject.getJSONArray(Constants.JSON_INCREMENTAL_UPDATES);
                        if (showDebugOutput)
                            Log.d(TAG, "Found " + incrementalUpdateList.length() + " incremental updates in the JSON");
                        //Incremental Updates. Own JSON Section for backward compatibility
                        for (int i = 0, max = incrementalUpdateList.length(); i < max; i++) {
                            if (!incrementalUpdateList.isNull(i))
                                uis.add(parseUpdateJSONObject(incrementalUpdateList.getJSONObject(i), mirrorList));
                            else
                                Log.e(TAG, "Theres an error in your JSON File(incremental part). Maybe a , after the last update");
                        }
                    } else if (showDebugOutput) Log.d(TAG, "No Incremental Update Info in the JSON");
                    break;
                default:
                    Log.e(TAG, "Wrong RomType!");
                    break;
            }
        }
        catch (JSONException e) {
            Log.e(TAG, "Error in JSON File: ", e);
        }
        return uis;
    }

    private UpdateInfo parseUpdateJSONObject(JSONObject obj, JSONArray mirrorList) {
        UpdateInfo ui = new UpdateInfo();

        try {
            if (PrimaryKeyExtra > 0)
                ui.PrimaryKey = PrimaryKeyExtra;

            String[] Boards = obj.getString(Constants.JSON_BOARD).split("\\|");
            for (String item : Boards) {
                if (item != null)
                    ui.board.add(item.trim());
            }
            ui.setType(obj.getString(Constants.JSON_TYPE).trim());
            String[] mods = obj.getString(Constants.JSON_MOD).split("\\|");
            for (String mod : mods) {
                if (mod != null)
                    ui.mod.add(mod.trim());
            }
            ui.setName(obj.getString(Constants.JSON_NAME).trim());
            ui.setVersion(obj.getString(Constants.JSON_VERSION).trim());
            ui.setDescription(obj.getString(Constants.JSON_DESCRIPTION).trim());
            ui.setBranchCode(obj.getString(Constants.JSON_BRANCH).trim());
            ui.setFileName(obj.getString(Constants.JSON_FILENAME).trim());

            //For incremental Updates
            if (obj.has(Constants.JSON_VERSION_FOR_APPLY)) {
                ui.setVersionForApply(obj.getString(Constants.JSON_VERSION_FOR_APPLY));
            }

            for (int i = 0, max = mirrorList.length(); i < max; i++) {
                try {
                    if (!mirrorList.isNull(i))
                        ui.updateMirrors.add(new URI(mirrorList.getString(i).trim()));
                    else
                        Log.e(TAG, "Theres an error in your JSON File. Maybe a , after the last mirror");
                }
                catch (URISyntaxException e) {
                    Log.e(TAG, "Unable to parse mirror url (" + mirrorList.getString(i) + ui.getFileName() + "). Ignoring this mirror", e);
                }
            }

            //Screenshots (only Extras)
            //Only if there is a Screenshot Array in the JSON
            if (obj.has(Constants.JSON_SCREENSHOTS)) {
                JSONArray screenshots = obj.getJSONArray(Constants.JSON_SCREENSHOTS);
                if (screenshots != null && screenshots.length() > 0) {
                    for (int screenshotcounter = 0; screenshotcounter < screenshots.length(); screenshotcounter++) {
                        try {
                            if (!screenshots.isNull(screenshotcounter))
                                ui.screenshots.add(new URI(screenshots.getString(screenshotcounter)));
                            else
                                Log.e(TAG, "Theres an error in your JSON File. Maybe a , after the last screenshot");
                        }
                        catch (URISyntaxException e) {
                            Log.e(TAG, "Unable to parse Screenshot url (" + screenshots.getString(screenshotcounter) + ") Extra: " + ui.getName() + ". Ignoring this Screenshot", e);
                        }
                    }
                }
            }
        }
        catch (JSONException e) {
            Log.e(TAG, "Error in JSON File: ", e);
        }
        return ui;
    }

    private boolean branchMatches(UpdateInfo ui, boolean experimentalAllowed) {
        if (ui == null) return false;

        boolean allow = false;

        if (ui.getBranchCode().equalsIgnoreCase(Constants.UPDATE_INFO_BRANCH_EXPERIMENTAL)) {
            if (experimentalAllowed)
                allow = true;
        } else {
            allow = true;
        }
        return allow;
    }

    private boolean boardMatches(UpdateInfo ui, String systemMod) {
        if (ui == null) return false;
        //If * is provided, all Boards are supported
        if (systemMod.equals(Constants.UPDATE_INFO_WILDCARD)) return true;
        for (String board : ui.board) {
            if (board.equalsIgnoreCase(systemMod) || board.equalsIgnoreCase(Constants.UPDATE_INFO_WILDCARD))
                return true;
        }
        return false;
    }

    private boolean romMatches(UpdateInfo ui, String systemRom) {
        if (ui == null) return false;
        if (systemRom.equals(Constants.UPDATE_INFO_WILDCARD)) return true;
        for (String mod : ui.mod) {
            if (mod.equalsIgnoreCase(systemRom) || mod.equalsIgnoreCase(Constants.UPDATE_INFO_WILDCARD))
                return true;
        }
        return false;
    }

    private LinkedList<UpdateInfo> getRomUpdates(LinkedList<UpdateInfo> updateInfos) {
        LinkedList<UpdateInfo> ret = new LinkedList<UpdateInfo>();
        for (int i = 0, max = updateInfos.size(); i < max; i++) {
            UpdateInfo ui = updateInfos.poll();
            if (ui.getType().equalsIgnoreCase(Constants.UPDATE_INFO_TYPE_ROM)) {
                if (boardMatches(ui, systemMod)) {
                    if (showAllRomUpdates || StringUtils.compareVersions(Customization.RO_MOD_START_STRING + ui.getVersion(), systemRom)) {
                        if (branchMatches(ui, showExperimentalRomUpdates)) {
                            if (showDebugOutput)
                                Log.d(TAG, "Adding Rom: " + ui.getName() + " Version: " + ui.getVersion() + " Filename: " + ui.getFileName());
                            ret.add(ui);
                        } else {
                            if (showDebugOutput)
                                Log.d(TAG, "Discarding Rom " + ui.getName() + " (Branch mismatch - stable/experimental)");
                        }
                    } else {
                        if (showDebugOutput) Log.d(TAG, "Discarding Rom " + ui.getName() + " (older version)");
                    }
                } else {
                    if (showDebugOutput) Log.d(TAG, "Discarding Rom " + ui.getName() + " (mod mismatch)");
                }
            } else {
                if (showDebugOutput)
                    Log.d(TAG, String.format("Discarding Rom %s Version %s (not a ROM)", ui.getName(), ui.getVersion()));
            }
        }
        return ret;
    }

    private LinkedList<UpdateInfo> getIncrementalRomUpdates(LinkedList<UpdateInfo> updateInfos) {
        LinkedList<UpdateInfo> ret = new LinkedList<UpdateInfo>();
        for (int i = 0, max = updateInfos.size(); i < max; i++) {
            UpdateInfo ui = updateInfos.poll();
            //Only Incremental Updates here. If theres a standard Update in there, remove it
            if (!ui.isIncremental()) {
                if (showDebugOutput)
                    Log.d(TAG, "Update " + ui.getName() + " is not an incremental update. Discarding it");
                continue;
            }
            //Only Use this Update, if the ForApply Version is the current running one
            if (!(Customization.RO_MOD_START_STRING + ui.getVersionForApply()).equalsIgnoreCase(systemRom)) {
                if (showDebugOutput)
                    Log.d(TAG, String.format("Incremental Update %s discarded, because the VersionForAppy (%s)" +
                            " doesn't match the current System Rom (%s).",
                            ui.getName(), Customization.RO_MOD_START_STRING + ui.getVersionForApply(), systemRom));
                continue;
            }
            if (ui.getType().equalsIgnoreCase(Constants.UPDATE_INFO_TYPE_ROM)) {
                if (boardMatches(ui, systemMod)) {
                    if (branchMatches(ui, showExperimentalRomUpdates)) {
                        if (showDebugOutput)
                            Log.d(TAG, "Adding Incremental Rom: " + ui.getName() + " Version: " + ui.getVersion() + " Filename: " + ui.getFileName());
                        ret.add(ui);
                    } else {
                        if (showDebugOutput)
                            Log.d(TAG, "Discarding Incremental Rom " + ui.getName() + " (Branch mismatch - stable/experimental)");
                    }
                } else {
                    if (showDebugOutput) Log.d(TAG, "Discarding Incremental Rom " + ui.getName() + " (mod mismatch)");
                }
            } else {
                if (showDebugOutput)
                    Log.d(TAG, String.format("Discarding Incremental Rom %s Version %s(not a ROM)", ui.getName(), ui.getVersion()));
            }
        }
        return ret;
    }

    private LinkedList<UpdateInfo> getExtraUpdates(LinkedList<UpdateInfo> updateInfos) {
        LinkedList<UpdateInfo> ret = new LinkedList<UpdateInfo>();
        for (int i = 0, max = updateInfos.size(); i < max; i++) {
            UpdateInfo ui = updateInfos.poll();

            //Extra installed and in correct format?
            if (extraInfos != null) {
                //Json object is a extra
                if (ui.getType().equalsIgnoreCase(Constants.UPDATE_INFO_TYPE_EXTRA)) {
                    //Rom matches (must also match, if there is a * in the extras.extra file, or the file does not exist)
                    if (romMatches(ui, systemRom)) {
                    	if (boardMatches(ui, systemMod)) {
	                        //Name matches or is *
	                        if (WildcardUsed || showAllExtraUpdates || (extraInfos.name != null && !extraInfos.name.equals("") && ui.getName().equalsIgnoreCase(extraInfos.name))) {
	                            //Version matches or name is *. If *, display all Versions
	                            if (WildcardUsed || showAllExtraUpdates || StringUtils.compareVersions(ui.getVersion(), extraInfos.version)) {
	                                //Branch matches
	                                if (branchMatches(ui, showExperimentalExtraUpdates)) {
	                                    if (showDebugOutput)
	                                        Log.d(TAG, "Adding Extra: " + ui.getName() + " Version: " + ui.getVersion() + " Filename: " + ui.getFileName());
	                                    ret.add(ui);
	                                } else {
	                                    if (showDebugOutput)
	                                        Log.d(TAG, String.format("Discarding Extra (branch mismatch) %s: Your Extra: %s %s; From JSON: %s %s", ui.getName(), extraInfos.name, extraInfos.version, ui.getName(), ui.getVersion()));
	                                }
	                            } else {
	                                if (showDebugOutput)
	                                    Log.d(TAG, String.format("Discarding Extra (Version mismatch) %s: Your Extra: %s %s; From JSON: %s %s", ui.getName(), extraInfos.name, extraInfos.version, ui.getName(), ui.getVersion()));
	                            }
	                        } else {
	                            if (showDebugOutput)
	                                Log.d(TAG, String.format("Discarding Extra (name mismatch) %s: Your Extra: %s %s; From JSON: %s %s", ui.getName(), extraInfos.name, extraInfos.version, ui.getName(), ui.getVersion()));
	                        }
                    	} else {
                    		if (showDebugOutput)
                                Log.d(TAG, String.format("Discarding Extra (board mismatch) %s: Your Extra: %s %s; From JSON: %s %s", ui.getName(), extraInfos.name, extraInfos.version, ui.getName(), ui.getVersion()));
                    	}
                    } else {
                        if (showDebugOutput)
                            Log.d(TAG, String.format("Discarding Extra (rom mismatch) %s: Your Extra: %s %s; From JSON: %s %s", ui.getName(), extraInfos.name, extraInfos.version, ui.getName(), ui.getVersion()));
                    }
                } else {
                    if (showDebugOutput)
                        Log.d(TAG, String.format("Discarding Update(not a Extra) %s Version %s", ui.getName(), ui.getVersion()));
                }
            } else {
                if (showDebugOutput)
                    Log.d(TAG, String.format("Discarding Extra %s Version %s. Invalid or no Extras installed", ui.getName(), ui.getVersion()));
            }
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    private static FullUpdateInfo FilterUpdates(FullUpdateInfo newList, FullUpdateInfo oldList) {
        if (showDebugOutput) Log.d(TAG, "Called FilterUpdates");
        if (showDebugOutput) Log.d(TAG, "newList Length: " + newList.getUpdateCount());
        if (showDebugOutput) Log.d(TAG, "oldList Length: " + oldList.getUpdateCount());
        FullUpdateInfo ful = new FullUpdateInfo();
        ful.roms = (LinkedList<UpdateInfo>) newList.roms.clone();
        ful.extras = (LinkedList<UpdateInfo>) newList.extras.clone();
        ful.incrementalRoms = (LinkedList<UpdateInfo>) newList.incrementalRoms.clone();
        ful.roms.removeAll(oldList.roms);
        ful.extras.removeAll(oldList.extras);
        ful.incrementalRoms.removeAll(oldList.incrementalRoms);
        if (showDebugOutput) Log.d(TAG, "fulList Length: " + ful.getUpdateCount());
        return ful;
    }

    private final Handler ToastHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.arg1 != 0)
                Toast.makeText(UpdateCheckService.this, msg.arg1, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(UpdateCheckService.this, (String) msg.obj, Toast.LENGTH_LONG).show();
        }
    };

    private void FinishUpdateCheck() {
        final int M = mCallbacks.beginBroadcast();
        for (int i = 0; i < M; i++) {
            try {
                mCallbacks.getBroadcastItem(i).UpdateCheckFinished();
            }
            catch (RemoteException e) {
                // The RemoteCallbackList will take care of removing
                // the dead object for us.
            }
        }
        mCallbacks.finishBroadcast();
    }
}
