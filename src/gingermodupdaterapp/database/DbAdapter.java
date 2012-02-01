package gingermodupdaterapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import gingermodupdaterapp.customTypes.FullExtraList;
import gingermodupdaterapp.customTypes.Screenshot;
import gingermodupdaterapp.customTypes.ExtraList;
import gingermodupdaterapp.customization.Customization;
import gingermodupdaterapp.misc.Log;
import gingermodupdaterapp.utils.StringUtils;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

public class DbAdapter {
    private static final String TAG = "DbAdapter";

    private static Boolean showDebugOutput;

    private static final int DATABASE_VERSION = 1;
    //Extralist
    private static final String DATABASE_TABLE_EXTRALIST = "ExtraList";
    private static final String KEY_EXTRALIST_ID = "id";
    private static final String INDEX_EXTRALIST_ID = "uidx_extralist_id";
    public static final int COLUMN_EXTRALIST_ID = 0;
    private static final String KEY_EXTRALIST_NAME = "name";
    private static final String INDEX_EXTRALIST_NAME = "idx_extralist_name";
    public static final int COLUMN_EXTRALIST_NAME = 1;
    private static final String KEY_EXTRALIST_URI = "uri";
    private static final String INDEX_EXTRALIST_URI = "idx_extralist_uri";
    public static final int COLUMN_EXTRALIST_URI = 2;
    private static final String KEY_EXTRALIST_ENABLED = "enabled";
    private static final String INDEX_EXTRALIST_ENABLED = "idx_extralist_enabled";
    public static final int COLUMN_EXTRALIST_ENABLED = 3;
    private static final String KEY_EXTRALIST_FEATURED = "featured";
    private static final String INDEX_EXTRALIST_FEATURED = "idx_extralist_featured";
    public static final int COLUMN_EXTRALIST_FEATURED = 4;
    //Screenshots
    private static final String DATABASE_TABLE_SCREENSHOT = "Screenshot";
    private static final String EXTRALIST_ID_FOREIGNKEYCONSTRAINT = "fk_extralist_id";
    private static final String TRIGGER_EXTRALIST_ID_INSERT = "fki_extralist_id";
    private static final String TRIGGER_EXTRALIST_ID_UPDATE = "fku_extralist_id";
    private static final String TRIGGER_EXTRALIST_ID_DELETE = "fkd_extralist_id";
    private static final String KEY_SCREENSHOT_ID = "id";
    private static final String INDEX_SCREENSHOT_ID = "uidx_screenshot_id";
    private static final int COLUMN_SCREENSHOT_ID = 0;
    private static final String KEY_SCREENSHOT_EXTRALIST_ID = "extralist_id";
    private static final String INDEX_SCREENSHOT_EXTRALIST_ID = "idx_screenshot_extralist_id";
    private static final int COLUMN_SCREENSHOT_EXTRALIST_ID = 1;
    private static final String KEY_SCREENSHOT_URI = "uri";
    private static final String INDEX_SCREENSHOT_URI = "idx_screenshot_uri";
    private static final int COLUMN_SCREENSHOT_URI = 2;
    private static final String KEY_SCREENSHOT_MODIFYDATE = "modifydate";
    private static final String INDEX_SCREENSHOT_MODIFYDATE = "idx_screenshot_modifydate";
    private static final int COLUMN_SCREENSHOT_MODIFYDATE = 3;
    private static final String KEY_SCREENSHOT_SCREENSHOT = "screenshot";
    private static final String INDEX_SCREENSHOT_SCREENSHOT = "idx_screenshot_screenshot";
    private static final int COLUMN_SCREENSHOT_SCREENSHOT = 4;

    private static final String[] COLUMNS_SCREENSHOT = new String[] {
        KEY_SCREENSHOT_ID,
        KEY_SCREENSHOT_EXTRALIST_ID,
        KEY_SCREENSHOT_URI,
        KEY_SCREENSHOT_MODIFYDATE,
        KEY_SCREENSHOT_SCREENSHOT
    };

    private static final String[] COLUMNS_EXTRALIST = new String[] {
    	KEY_EXTRALIST_ID,
    	KEY_EXTRALIST_NAME,
    	KEY_EXTRALIST_URI,
    	KEY_EXTRALIST_ENABLED,
    	KEY_EXTRALIST_FEATURED
    };

    // SQL Statements to create a new database.
    private static final String DATABASE_CREATE_EXTRALIST =
            "create table " +
                    DATABASE_TABLE_EXTRALIST +
                    " (" +
                    KEY_EXTRALIST_ID + " integer primary key autoincrement, " +
                    KEY_EXTRALIST_NAME + " text not null, " +
                    KEY_EXTRALIST_URI + " text not null, " +
                    KEY_EXTRALIST_ENABLED + " integer default 0, " +
                    KEY_EXTRALIST_FEATURED + " integer default 0);";

    private static final String DATABASE_CREATE_SCREENSHOTS =
            "create table " +
                    DATABASE_TABLE_SCREENSHOT +
                    " (" +
                    KEY_SCREENSHOT_ID + " integer primary key autoincrement, " +
                    KEY_SCREENSHOT_EXTRALIST_ID + " integer not null" +
                    " CONSTRAINT " + EXTRALIST_ID_FOREIGNKEYCONSTRAINT + " REFERENCES " + DATABASE_TABLE_EXTRALIST + "(" + KEY_EXTRALIST_ID + ") ON DELETE CASCADE, " +
                    KEY_SCREENSHOT_URI + " text not null, " +
                    KEY_SCREENSHOT_MODIFYDATE + " date not null, " +
                    KEY_SCREENSHOT_SCREENSHOT + " blob);";

    //Trigger for foreign Key Constraints (i hate sqlite, hail to ORACLE!)
    private static final String TRIGGER_EXTRALISTID_INSERT =
            "CREATE TRIGGER " + TRIGGER_EXTRALIST_ID_INSERT +
                    " BEFORE INSERT ON " + DATABASE_TABLE_SCREENSHOT +
                    " FOR EACH ROW BEGIN" +
                    " SELECT CASE" +
                    " WHEN ((new." + KEY_SCREENSHOT_EXTRALIST_ID + " IS NOT NULL)" +
                    " AND ((SELECT " + KEY_EXTRALIST_ID + " FROM " + DATABASE_TABLE_EXTRALIST +
                    " WHERE " + KEY_EXTRALIST_ID + " = new." + KEY_SCREENSHOT_EXTRALIST_ID + ") IS NULL))" +
                    " THEN RAISE(ABORT, 'insert on table " + DATABASE_TABLE_SCREENSHOT +
                    " violates foreign key constraint " + EXTRALIST_ID_FOREIGNKEYCONSTRAINT + "')" +
                    " END;" +
                    " END;";

    private static final String TRIGGER_EXTRALISTID_UPDATE =
            "CREATE TRIGGER " + TRIGGER_EXTRALIST_ID_UPDATE +
                    " BEFORE UPDATE ON " + DATABASE_TABLE_SCREENSHOT +
                    " FOR EACH ROW BEGIN" +
                    " SELECT CASE" +
                    " WHEN ((SELECT " + KEY_EXTRALIST_ID + " FROM " + DATABASE_TABLE_EXTRALIST +
                    " WHERE " + KEY_EXTRALIST_ID + " = new." + KEY_SCREENSHOT_EXTRALIST_ID + ") IS NULL)" +
                    " THEN RAISE(ABORT, 'update on table " + DATABASE_TABLE_SCREENSHOT +
                    " violates foreign key constraint " + EXTRALIST_ID_FOREIGNKEYCONSTRAINT + "')" +
                    " END;" +
                    " END;";

    //Delete cached Screenshots, when ExtraList is removed
    private static final String TRIGGER_EXTRALISTID_DELETE =
            "CREATE TRIGGER " + TRIGGER_EXTRALIST_ID_DELETE +
                    " BEFORE DELETE ON " + DATABASE_TABLE_EXTRALIST +
                    " FOR EACH ROW BEGIN" +
                    " DELETE FROM " + DATABASE_TABLE_SCREENSHOT +
                    " WHERE " + KEY_SCREENSHOT_EXTRALIST_ID + " = old." + KEY_EXTRALIST_ID + ";" +
                    " END;";

    //Indeces ExtraList
    private static final String INDEX_EXTRALIST_EXTRALIST_ID =
            "CREATE UNIQUE INDEX IF NOT EXISTS " + INDEX_EXTRALIST_ID +
                    " ON " + DATABASE_TABLE_EXTRALIST + "(" + KEY_EXTRALIST_ID + ");";

    private static final String INDEX_EXTRALIST_EXTRALIST_NAME =
            "CREATE INDEX IF NOT EXISTS " + INDEX_EXTRALIST_NAME +
                    " ON " + DATABASE_TABLE_EXTRALIST + "(" + KEY_EXTRALIST_NAME + ");";

    private static final String INDEX_EXTRALIST_EXTRALIST_URI =
            "CREATE INDEX IF NOT EXISTS " + INDEX_EXTRALIST_URI +
                    " ON " + DATABASE_TABLE_EXTRALIST + "(" + KEY_EXTRALIST_URI + ");";

    private static final String INDEX_EXTRALIST_EXTRALIST_ENABLED =
            "CREATE INDEX IF NOT EXISTS " + INDEX_EXTRALIST_ENABLED +
                    " ON " + DATABASE_TABLE_EXTRALIST + "(" + KEY_EXTRALIST_ENABLED + ");";

    private static final String INDEX_EXTRALIST_EXTRALIST_FEATURED =
            "CREATE INDEX IF NOT EXISTS " + INDEX_EXTRALIST_FEATURED +
                    " ON " + DATABASE_TABLE_EXTRALIST + "(" + KEY_EXTRALIST_FEATURED + ");";

    //Indeces Screenshots
    private static final String INDEX_SCREENSHOT_SCREENSHOT_ID =
            "CREATE UNIQUE INDEX IF NOT EXISTS " + INDEX_SCREENSHOT_ID +
                    " ON " + DATABASE_TABLE_SCREENSHOT + "(" + KEY_SCREENSHOT_ID + ");";

    private static final String INDEX_SCREENSHOT_SCREENSHOT_EXTRALIST_ID =
            "CREATE INDEX IF NOT EXISTS " + INDEX_SCREENSHOT_EXTRALIST_ID +
                    " ON " + DATABASE_TABLE_SCREENSHOT + "(" + KEY_SCREENSHOT_EXTRALIST_ID + ");";

    private static final String INDEX_SCREENSHOT_SCREENSHOT_URI =
            "CREATE INDEX IF NOT EXISTS " + INDEX_SCREENSHOT_URI +
                    " ON " + DATABASE_TABLE_SCREENSHOT + "(" + KEY_SCREENSHOT_URI + ");";

    private static final String INDEX_SCREENSHOT_SCREENSHOT_MODIFYDATE =
            "CREATE INDEX IF NOT EXISTS " + INDEX_SCREENSHOT_MODIFYDATE +
                    " ON " + DATABASE_TABLE_SCREENSHOT + "(" + KEY_SCREENSHOT_MODIFYDATE + ");";

    private static final String INDEX_SCREENSHOT_SCREENSHOT_SCREENSHOT =
            "CREATE INDEX IF NOT EXISTS " + INDEX_SCREENSHOT_SCREENSHOT +
                    " ON " + DATABASE_TABLE_SCREENSHOT + "(" + KEY_SCREENSHOT_SCREENSHOT + ");";

    private final Context context;
    private final DatabaseHelper helper;
    private SQLiteDatabase db;

    public DbAdapter(Context _context, Boolean _showDebugOutput) {
        context = _context;
        helper = new DatabaseHelper(context);
    	showDebugOutput = _showDebugOutput;
    }

    public DbAdapter open() throws SQLException{
    	db = helper.getWritableDatabase();
        return this;
    }

    public void close() {
        helper.close();
    }

    // Insert a new Extra
    public long insertExtra(ExtraList _extra) {
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_EXTRALIST_NAME, _extra.name);
        newValues.put(KEY_EXTRALIST_URI, _extra.url.toString());
        newValues.put(KEY_EXTRALIST_ENABLED, _extra.enabled ? 1 : 0);
        newValues.put(KEY_EXTRALIST_FEATURED, _extra.featured ? 1 : 0);
        return db.insert(DATABASE_TABLE_EXTRALIST, null, newValues);
    }

    // Remove a extra based on its index
    public boolean removeExtra(long _rowIndex) {
        return db.delete(DATABASE_TABLE_EXTRALIST, KEY_EXTRALIST_ID + "= ?",
        		new String[]{Long.toString(_rowIndex)}) > 0;
    }

    public long getExtraCount() {
    	return DatabaseUtils.queryNumEntries(db, DATABASE_TABLE_EXTRALIST);
    }

    // Removes all extras
    public boolean removeAllExtras() {
        return db.delete(DATABASE_TABLE_EXTRALIST, null, null) > 0;
    }

    // Removes all extras
    public boolean removeAllFeaturedExtras() {
        return db.delete(DATABASE_TABLE_EXTRALIST, KEY_EXTRALIST_FEATURED + "= ?",
        		new String[]{"1"}) > 0;
    }

    // Disable all Extras
    public boolean disableAllExtras() {
        ContentValues newValue = new ContentValues();
        newValue.put(KEY_EXTRALIST_ENABLED, 0);
        return db.update(DATABASE_TABLE_EXTRALIST, newValue, null, null) > 0;
    }

    // Disable all Featured Extras
    public boolean disableAllFeaturedExtras() {
        ContentValues newValue = new ContentValues();
        newValue.put(KEY_EXTRALIST_ENABLED, 0);
        return db.update(DATABASE_TABLE_EXTRALIST, newValue, KEY_EXTRALIST_FEATURED + "= ?",
        		new String[]{"1"}) > 0;
    }

    // Enable all Extras
    public boolean enableAllExtras() {
        ContentValues newValue = new ContentValues();
        newValue.put(KEY_EXTRALIST_ENABLED, 1);
        return db.update(DATABASE_TABLE_EXTRALIST, newValue, null, null) > 0;
    }

    // enable all Featured Extras
    public boolean enableAllFeaturedExtras() {
        ContentValues newValue = new ContentValues();
        newValue.put(KEY_EXTRALIST_ENABLED, 1);
        return db.update(DATABASE_TABLE_EXTRALIST, newValue,
        		KEY_EXTRALIST_FEATURED + "= ?", new String[]{"1"}) > 0;
    }

    // Update a Extra
    public boolean updateExtra(long _rowIndex, ExtraList _extra) {
        ContentValues newValue = new ContentValues();
        newValue.put(KEY_EXTRALIST_NAME, _extra.name);
        newValue.put(KEY_EXTRALIST_URI, _extra.url.toString());
        newValue.put(KEY_EXTRALIST_ENABLED, _extra.enabled ? 1 : 0);
        newValue.put(KEY_EXTRALIST_FEATURED, _extra.featured ? 1 : 0);
        return db.update(DATABASE_TABLE_EXTRALIST, newValue,
        		KEY_EXTRALIST_ID + "= ?", new String[]{Long.toString(_rowIndex)}) > 0;
    }

    public Cursor getAllExtrasCursor() {
        return db.query(DATABASE_TABLE_EXTRALIST, COLUMNS_EXTRALIST,
        		null, null, null, null, KEY_EXTRALIST_NAME);
    }

    public ExtraList getExtraItem(long _rowIndex) throws SQLException {
        Cursor cursor = db.query(true, DATABASE_TABLE_EXTRALIST,
        		COLUMNS_EXTRALIST, KEY_EXTRALIST_ID + "= ?",
        		new String[]{Long.toString(_rowIndex)}, null, null, null, null);
        if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
            cursor.close();
            throw new SQLException("No Extra item found for row: " + _rowIndex);
        }
        String name = cursor.getString(COLUMN_EXTRALIST_NAME);
        String uri = cursor.getString(COLUMN_EXTRALIST_URI);
        int enabled = cursor.getInt(COLUMN_EXTRALIST_ENABLED);
        int featured = cursor.getInt(COLUMN_EXTRALIST_FEATURED);
        int Key = cursor.getInt(COLUMN_EXTRALIST_ID);
        ExtraList result = new ExtraList();
        result.name = name;
        result.url = URI.create(uri);
        result.PrimaryKey = Key;
        result.enabled = enabled == 1;
        result.featured = featured == 1;
        cursor.close();
        return result;
    }

    public void UpdateFeaturedExtras(FullExtraList t) {
        FullExtraList retValue = new FullExtraList();
        //Get the enabled state of the current Featured Extras
        for (ExtraList tl : t.returnFullExtraList()) {
            Cursor result = db.query(true, DATABASE_TABLE_EXTRALIST,
            		COLUMNS_EXTRALIST, KEY_EXTRALIST_NAME + "= ? and "
            		+ KEY_EXTRALIST_FEATURED + "= ?",
            		new String[]{tl.name, "1"}, null, null, null, null);
            if (!result.moveToFirst()) {
                if (showDebugOutput) Log.d(TAG, "Extra " + tl.name + " not found in your List");
                retValue.addExtraToList(tl);
                continue;
            }
            tl.enabled = result.getInt(COLUMN_EXTRALIST_ENABLED) != 0;
            retValue.addExtraToList(tl);
            result.close();
        }
        //Delete all featured Extras
        db.delete(DATABASE_TABLE_EXTRALIST, KEY_EXTRALIST_FEATURED + "= ?",
        		new String[]{"1"});
        if (showDebugOutput) Log.d(TAG, "Deleted all old Featured Extra Servers");
        //Add all Featured Extras again
        for (ExtraList tl2 : retValue.returnFullExtraList()) {
            insertExtra(tl2);
        }
        if (showDebugOutput) Log.d(TAG, "Updated Featured Extra Servers");
    }

    //SCREENSHOTS

    // Remove a Screenshot based on its index
    public boolean removeScreenshot(long _rowIndex) {
        return db.delete(DATABASE_TABLE_SCREENSHOT, KEY_SCREENSHOT_ID + "= ?",
        		new String[]{Long.toString(_rowIndex)}) > 0;
    }

    // Remove all Screenshots for given Extra except the ones in the parameter
    public boolean removeScreenshotExcept(int ForeignKey, String[] primaryKeysNotToRemove) {
        if (primaryKeysNotToRemove == null || primaryKeysNotToRemove.length == 0)
            return false;
        String temp = StringUtils.arrayToString(primaryKeysNotToRemove, ",");
        return db.delete(DATABASE_TABLE_SCREENSHOT, KEY_SCREENSHOT_EXTRALIST_ID + "= ? AND "
                + KEY_SCREENSHOT_ID + " not in (?)",
                new String[]{Integer.toString(ForeignKey), temp}) > 0;
    }

    // Remove a Screenshot based on its FeaturedExtraIndex
    public boolean removeAllScreenshotsForExtra(long FeaturedExtraId) {
        return db.delete(DATABASE_TABLE_SCREENSHOT,
        		KEY_SCREENSHOT_EXTRALIST_ID + "= ?",
        		new String[]{Long.toString(FeaturedExtraId)}) > 0;
    }

    // Insert a new Screenshot
    public long insertScreenshot(Screenshot _screenshot) {

        ContentValues newValues = new ContentValues();
        newValues.put(KEY_SCREENSHOT_EXTRALIST_ID, _screenshot.ForeignExtraListKey);
        newValues.put(KEY_SCREENSHOT_URI, _screenshot.url.toString());
        newValues.put(KEY_SCREENSHOT_MODIFYDATE, _screenshot.getModifyDate());
        newValues.put(KEY_SCREENSHOT_SCREENSHOT, _screenshot.getPictureAsByteArray());
        return db.insert(DATABASE_TABLE_SCREENSHOT, null, newValues);
    }

    //Get all Screenshots for a Extra
    public List<Screenshot> getAllScreenshotsForExtra(long _extraIndex) throws SQLException {
        Cursor cursor = db.query(true, DATABASE_TABLE_SCREENSHOT,
        		COLUMNS_SCREENSHOT, KEY_SCREENSHOT_EXTRALIST_ID + "= ?",
        		new String[]{Long.toString(_extraIndex)}, null, null, null, null);
        if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        List<Screenshot> result = new LinkedList<Screenshot>();

        cursor.moveToFirst();
        do {
            Screenshot item = new Screenshot();
            item.PrimaryKey = cursor.getInt(COLUMN_SCREENSHOT_ID);
            item.ForeignExtraListKey = cursor.getInt(COLUMN_SCREENSHOT_EXTRALIST_ID);
            item.url = URI.create(cursor.getString(COLUMN_SCREENSHOT_URI));
            item.setModifyDate(cursor.getString(COLUMN_SCREENSHOT_MODIFYDATE));
            item.setBitmapFromByteArray(cursor.getBlob(COLUMN_SCREENSHOT_SCREENSHOT));
            result.add(item);
        } while (cursor.moveToNext());
        cursor.close();
        return result;
    }

    //Get single Screenshots by Id
    public Screenshot getScreenshotById(long _index) throws SQLException {
        Cursor cursor = db.query(true, DATABASE_TABLE_SCREENSHOT,
        		COLUMNS_SCREENSHOT, KEY_SCREENSHOT_ID + "= ?",
        		new String[]{Long.toString(_index)}, null, null, null, null);
        if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
            cursor.close();
            throw new SQLException("No Screenshot found for Key: " + _index);
        }

        Screenshot result = new Screenshot();
        result.PrimaryKey = cursor.getInt(COLUMN_SCREENSHOT_ID);
        result.ForeignExtraListKey = cursor.getInt(COLUMN_SCREENSHOT_EXTRALIST_ID);
        result.url = URI.create(cursor.getString(COLUMN_SCREENSHOT_URI));
        result.setModifyDate(cursor.getString(COLUMN_SCREENSHOT_MODIFYDATE));
        result.setBitmapFromByteArray(cursor.getBlob(COLUMN_SCREENSHOT_SCREENSHOT));
        cursor.close();
        return result;
    }

    //Checks if a Screenshot already exists. Cause the Primary Key is Stored in the Screenshot Object
    //The contains() Method will not work cause theres no Primary Key on Download
    //Will return a Screenshotobject with only the PrimaryKey if found, otherwise -1,the Modifydate and the blob
    public Screenshot ScreenshotExists(int ForeignKey, String Url) throws SQLException {
        Screenshot retValue = new Screenshot();
        Cursor cursor = db.query(true, DATABASE_TABLE_SCREENSHOT,
        		COLUMNS_SCREENSHOT, KEY_SCREENSHOT_EXTRALIST_ID + "= ? AND "
        		+ KEY_SCREENSHOT_URI + "= ?",
                new String[]{Integer.toString(ForeignKey), Url}, null, null, null, null);
        if ((cursor.getCount() != 0) && cursor.moveToFirst()) {
            retValue.PrimaryKey = cursor.getInt(COLUMN_SCREENSHOT_ID);
            retValue.ForeignExtraListKey = cursor.getInt(COLUMN_SCREENSHOT_EXTRALIST_ID);
            retValue.url = URI.create(cursor.getString(COLUMN_SCREENSHOT_URI));
            retValue.setModifyDate(cursor.getString(COLUMN_SCREENSHOT_MODIFYDATE));
            retValue.setBitmapFromByteArray(cursor.getBlob(COLUMN_SCREENSHOT_SCREENSHOT));
        }
        cursor.close();
        return retValue;
    }

    // Update a Screenshot
    public boolean updateScreenshot(long _rowIndex, Screenshot _screenshot) {
        ContentValues newValue = new ContentValues();
        newValue.put(KEY_SCREENSHOT_EXTRALIST_ID, _screenshot.ForeignExtraListKey);
        newValue.put(KEY_SCREENSHOT_URI, _screenshot.url.toString());
        newValue.put(KEY_SCREENSHOT_MODIFYDATE, _screenshot.getModifyDate());
        newValue.put(KEY_SCREENSHOT_SCREENSHOT, _screenshot.getPictureAsByteArray());
        return db.update(DATABASE_TABLE_SCREENSHOT, newValue,
        		KEY_SCREENSHOT_ID + "= ?", new String[]{Long.toString(_rowIndex)}) > 0;
    }

    // Delete All Screenshots
    public void deleteAllScreenshot() {
        db.execSQL("DELETE FROM " + DATABASE_TABLE_SCREENSHOT + ";");
    }

    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context)
        {
            super(context, Customization.DATABASE_FILE, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            if (showDebugOutput) Log.d(TAG, "Create Database");
            db.execSQL(DATABASE_CREATE_EXTRALIST);
            db.execSQL(DATABASE_CREATE_SCREENSHOTS);

            db.execSQL(INDEX_EXTRALIST_EXTRALIST_ID);
            db.execSQL(INDEX_EXTRALIST_EXTRALIST_NAME);
            db.execSQL(INDEX_EXTRALIST_EXTRALIST_URI);
            db.execSQL(INDEX_EXTRALIST_EXTRALIST_ENABLED);
            db.execSQL(INDEX_EXTRALIST_EXTRALIST_FEATURED);
            db.execSQL(INDEX_SCREENSHOT_SCREENSHOT_ID);
            db.execSQL(INDEX_SCREENSHOT_SCREENSHOT_EXTRALIST_ID);
            db.execSQL(INDEX_SCREENSHOT_SCREENSHOT_URI);
            db.execSQL(INDEX_SCREENSHOT_SCREENSHOT_MODIFYDATE);
            db.execSQL(INDEX_SCREENSHOT_SCREENSHOT_SCREENSHOT);

            db.execSQL(TRIGGER_EXTRALISTID_INSERT);
            db.execSQL(TRIGGER_EXTRALISTID_UPDATE);
            db.execSQL(TRIGGER_EXTRALISTID_DELETE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion)
        {
        	if (showDebugOutput)
                Log.d(TAG, "Upgrading from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
            //Drop the old tables and triggers
            if (showDebugOutput) Log.d(TAG, "Dropping old Database");
            db.execSQL("DROP TRIGGER IF EXISTS " + TRIGGER_EXTRALIST_ID_INSERT);
            db.execSQL("DROP TRIGGER IF EXISTS " + TRIGGER_EXTRALIST_ID_UPDATE);
            db.execSQL("DROP TRIGGER IF EXISTS " + TRIGGER_EXTRALIST_ID_DELETE);

            db.execSQL("DROP INDEX IF EXISTS " + INDEX_EXTRALIST_ID);
            db.execSQL("DROP INDEX IF EXISTS " + INDEX_EXTRALIST_NAME);
            db.execSQL("DROP INDEX IF EXISTS " + INDEX_EXTRALIST_URI);
            db.execSQL("DROP INDEX IF EXISTS " + INDEX_EXTRALIST_ENABLED);
            db.execSQL("DROP INDEX IF EXISTS " + INDEX_EXTRALIST_FEATURED);
            db.execSQL("DROP INDEX IF EXISTS " + INDEX_SCREENSHOT_ID);
            db.execSQL("DROP INDEX IF EXISTS " + INDEX_SCREENSHOT_EXTRALIST_ID);
            db.execSQL("DROP INDEX IF EXISTS " + INDEX_SCREENSHOT_URI);
            db.execSQL("DROP INDEX IF EXISTS " + INDEX_SCREENSHOT_MODIFYDATE);
            db.execSQL("DROP INDEX IF EXISTS " + INDEX_SCREENSHOT_SCREENSHOT);

            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_SCREENSHOT);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_EXTRALIST);
            onCreate(db);
        }
    }
}
