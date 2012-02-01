package gingermodupdaterapp.customization;

public class Customization {
    //The String from the build.prop before the Version
    public static final String RO_MOD_START_STRING = "Kernel Version-";
    //Minimum Supported Version (So the User has to install google apps and so before)
    public static final String MIN_SUPPORTED_VERSION_STRING = RO_MOD_START_STRING + "0.1";
    //Updateinstructions for the min supported Version
    public static final String UPDATE_INSTRUCTIONS_URL = "http://forum.xda-developers.com/showthread.php?t=1411788";
    //DB filename
    public static final String DATABASE_FILE = "cmupdater.db";
    //DownloadDirectory
    public static final String DOWNLOAD_DIR = "NEAK_Updates";
    //MUST be the first package name.
    public static final String PACKAGE_FIRST_NAME = "gingermodupdaterapp";
    //Filename for Instance save
    public static final String STORED_STATE_FILENAME = "cmupdater.state";
    //Android Board type
    public static final String BOARD = "ro.product.board";
    //Name of the Current Kernel
    public static final String SYS_PROP_MOD_VERSION = "ro.kernel.id";
    //Screenshotsupport?
    public static final Boolean Screenshotsupport = true;
}
