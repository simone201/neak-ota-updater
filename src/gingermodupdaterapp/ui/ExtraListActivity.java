package gingermodupdaterapp.ui;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.*;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import gingermodupdaterapp.customTypes.FullExtraList;
import gingermodupdaterapp.customTypes.ExtraList;
import gingermodupdaterapp.database.DbAdapter;
import gingermodupdaterapp.featuredExtras.FeaturedExtras;
import gingermodupdaterapp.listadapters.ExtraListAdapter;
import gingermodupdaterapp.misc.Constants;
import gingermodupdaterapp.misc.Log;
import gingermodupdaterapp.utils.Preferences;

import java.net.URI;
import java.util.LinkedList;

public class ExtraListActivity extends ListActivity {
    private static final String TAG = "ExtraListActivity";

    private Boolean showDebugOutput = false;

    private DbAdapter extraListDb;
    private Cursor extraListCursor;
    private ListView lv;
    private Resources res;
    private TextView tv;
    private FullExtraList FeaturedExtras = null;
    private Thread FeaturedExtrasThread;
    private ProgressDialog FeaturedExtrasProgressDialog;
    public static Handler FeaturedExtrasProgressHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showDebugOutput = new Preferences(this).displayDebugOutput();
        extraListDb = new DbAdapter(this, showDebugOutput);
        if (showDebugOutput) Log.d(TAG, "Opening Database");
        extraListDb.open();
        setContentView(R.layout.extralist);
        tv = (TextView) findViewById(R.id.extra_list_info);
        getExtraList();
        lv = getListView();
        registerForContextMenu(lv);
        res = getResources();
    }

    private void getExtraList() {
        extraListCursor = extraListDb.getAllExtrasCursor();
        startManagingCursor(extraListCursor);
        updateExtraList();
    }

    private void updateExtraList() {
        extraListCursor.requery();
        FullExtraList fullExtraList = new FullExtraList();
        if (extraListCursor.moveToFirst()) {
            do {
                String name = extraListCursor.getString(DbAdapter.COLUMN_EXTRALIST_NAME);
                String uri = extraListCursor.getString(DbAdapter.COLUMN_EXTRALIST_URI);
                int pk = extraListCursor.getInt(DbAdapter.COLUMN_EXTRALIST_ID);
                int enabled = extraListCursor.getInt(DbAdapter.COLUMN_EXTRALIST_ENABLED);
                int featured = extraListCursor.getInt(DbAdapter.COLUMN_EXTRALIST_FEATURED);
                ExtraList newItem = new ExtraList();
                newItem.name = name;
                newItem.url = URI.create(uri);
                newItem.enabled = enabled == 1;
                newItem.featured = featured == 1;
                newItem.PrimaryKey = pk;
                fullExtraList.addExtraToList(newItem);
            }
            while (extraListCursor.moveToNext());
        }
        LinkedList<ExtraList> fullExtraListList = fullExtraList.returnFullExtraList();
        ExtraListAdapter<ExtraList> AdapterExtraList = new ExtraListAdapter<ExtraList>(
                this,
                fullExtraListList);
        setListAdapter(AdapterExtraList);
        if (fullExtraList.getExtraCount() > 0)
            tv.setText(R.string.extra_list_long_press);
        else
            tv.setText(R.string.extra_list_no_extras);
        extraListCursor.deactivate();
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
        super.onListItemClick(parent, v, position, id);
        if (showDebugOutput) Log.d(TAG, "Item clicked. Postition: " + id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, Constants.MENU_EXTRA_LIST_UPDATE_FEATURED, Menu.NONE, R.string.menu_update_featured);
        menu.add(Menu.NONE, Constants.MENU_EXTRA_LIST_ADD, Menu.NONE, R.string.menu_add_extra);
        SubMenu deleteMenu = menu.addSubMenu(R.string.extra_submenu_delete);
        deleteMenu.setIcon(android.R.drawable.ic_menu_more);
        deleteMenu.add(Menu.NONE, Constants.MENU_EXTRA_DELETE_ALL, Menu.NONE, R.string.menu_delete_all_extras);
        deleteMenu.add(Menu.NONE, Constants.MENU_EXTRA_DELETE_ALL_FEATURED, Menu.NONE, R.string.menu_delete_all_featured_extras);
        SubMenu disableMenu = menu.addSubMenu(R.string.extra_submenu_disable);
        disableMenu.setIcon(android.R.drawable.ic_menu_more);
        disableMenu.add(Menu.NONE, Constants.MENU_EXTRA_DISABLE_ALL, Menu.NONE, R.string.menu_disable_all_extras);
        disableMenu.add(Menu.NONE, Constants.MENU_EXTRA_DISABLE_ALL_FEATURED, Menu.NONE, R.string.menu_disable_all_featured_extras);
        SubMenu enableMenu = menu.addSubMenu(R.string.extra_submenu_enable);
        enableMenu.setIcon(android.R.drawable.ic_menu_more);
        enableMenu.add(Menu.NONE, Constants.MENU_EXTRA_ENABLE_ALL, Menu.NONE, R.string.menu_enable_all_extras);
        enableMenu.add(Menu.NONE, Constants.MENU_EXTRA_ENABLE_ALL_FEATURED, Menu.NONE, R.string.menu_enable_all_featured_extras);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
        ExtraList tl;

        switch (item.getItemId()) {
            case Constants.MENU_EXTRA_LIST_ADD:
                createNewExtraList(false, "", "", true, 0, false);
                return true;
            case Constants.MENU_EXTRA_LIST_UPDATE_FEATURED:
                new AlertDialog.Builder(ExtraListActivity.this)
                        .setTitle(R.string.featured_extras_dialog_title)
                        .setMessage(R.string.featured_extras_dialog_summary)
                        .setPositiveButton(R.string.featured_extras_dialog_pos, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                updateFeaturedExtras();
                            }
                        })
                        .setNegativeButton(R.string.featured_extras_dialog_neg, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                return true;
            case Constants.MENU_EXTRA_LIST_CONTEXT_EDIT:
                if (showDebugOutput) Log.d(TAG, "Edit clicked");
                tl = ((ExtraList) lv.getAdapter().getItem(menuInfo.position));
                createNewExtraList(true, tl.name, tl.url.toString(), tl.enabled, tl.PrimaryKey, tl.featured);
                break;
            case Constants.MENU_EXTRA_LIST_CONTEXT_DELETE:
                Log.d(TAG, "Delete clicked");
                tl = ((ExtraList) lv.getAdapter().getItem(menuInfo.position));
                DeleteExtra(tl.PrimaryKey);
                break;
            case Constants.MENU_EXTRA_LIST_CONTEXT_DISABLE:
                Log.d(TAG, "Selected to disable Extra Server");
                tl = ((ExtraList) lv.getAdapter().getItem(menuInfo.position));
                tl.enabled = false;
                extraListDb.updateExtra(tl.PrimaryKey, tl);
                updateExtraList();
                break;
            case Constants.MENU_EXTRA_LIST_CONTEXT_ENABLE:
                Log.d(TAG, "Selected to enable Extra Server");
                tl = ((ExtraList) lv.getAdapter().getItem(menuInfo.position));
                tl.enabled = true;
                extraListDb.updateExtra(tl.PrimaryKey, tl);
                updateExtraList();
                break;
            case Constants.MENU_EXTRA_DELETE_ALL:
                Log.d(TAG, "Selected to delete all Extra Servers");
                extraListDb.removeAllExtras();
                updateExtraList();
                break;
            case Constants.MENU_EXTRA_DELETE_ALL_FEATURED:
                Log.d(TAG, "Selected to delete all Featured Extra Servers");
                extraListDb.removeAllFeaturedExtras();
                updateExtraList();
                break;
            case Constants.MENU_EXTRA_DISABLE_ALL:
                Log.d(TAG, "Selected to disable all Extra Servers");
                extraListDb.disableAllExtras();
                updateExtraList();
                break;
            case Constants.MENU_EXTRA_DISABLE_ALL_FEATURED:
                Log.d(TAG, "Selected to disable all Featured Extra Servers");
                extraListDb.disableAllFeaturedExtras();
                updateExtraList();
                break;
            case Constants.MENU_EXTRA_ENABLE_ALL:
                Log.d(TAG, "Selected to enable all Extra Servers");
                extraListDb.enableAllExtras();
                updateExtraList();
                break;
            case Constants.MENU_EXTRA_ENABLE_ALL_FEATURED:
                Log.d(TAG, "Selected to enable all Featured Extra Servers");
                extraListDb.enableAllFeaturedExtras();
                updateExtraList();
                break;
            default:
                Log.d(TAG, "Unknown Menu ID:" + item.getItemId());
                break;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onDestroy() {
        // Close the database
        if (showDebugOutput) Log.d(TAG, "Closing Database");
        extraListCursor.close();
        extraListDb.close();
        super.onDestroy();
    }

    private void createNewExtraList(final boolean _update, String _name, String _uri, boolean _enabled, final int _primaryKey, boolean _featured) {
        Intent i = new Intent(ExtraListActivity.this, ExtraListNewActivity.class);
        i.putExtra(Constants.EXTRA_LIST_NEW_NAME, _name);
        i.putExtra(Constants.EXTRA_LIST_NEW_URI, _uri);
        i.putExtra(Constants.EXTRA_LIST_NEW_ENABLED, _enabled);
        i.putExtra(Constants.EXTRA_LIST_NEW_PRIMARYKEY, _primaryKey);
        i.putExtra(Constants.EXTRA_LIST_NEW_UPDATE, _update);
        i.putExtra(Constants.EXTRA_LIST_NEW_FEATURED, _featured);
        startActivityForResult(i, ExtraListNewActivity.REQUEST_CODE);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle(res.getString(R.string.p_extra_list_context_menu_header));
        menu.add(Menu.NONE, Constants.MENU_EXTRA_LIST_CONTEXT_EDIT, Menu.NONE, R.string.menu_edit_extra);
        menu.add(Menu.NONE, Constants.MENU_EXTRA_LIST_CONTEXT_DELETE, Menu.NONE, R.string.menu_delete_extra);
        ExtraList tl = ((ExtraList) lv.getAdapter().getItem(((AdapterContextMenuInfo) menuInfo).position));
        if (tl.enabled)
            menu.add(Menu.NONE, Constants.MENU_EXTRA_LIST_CONTEXT_DISABLE, Menu.NONE, R.string.menu_disable_extra);
        else
            menu.add(Menu.NONE, Constants.MENU_EXTRA_LIST_CONTEXT_ENABLE, Menu.NONE, R.string.menu_enable_extra);
    }

    private void DeleteExtra(int position) {
        if (showDebugOutput) Log.d(TAG, "Remove Extra Postition: " + position);
        if (extraListDb.removeExtra(position))
            if (showDebugOutput) Log.d(TAG, "Success");
            else {
                Log.e(TAG, "Fail");
                Toast.makeText(this, R.string.extra_list_delete_error, Toast.LENGTH_LONG).show();
            }
        updateExtraList();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (showDebugOutput) Log.d(TAG, "RequestCode: " + requestCode + " ResultCode: " + resultCode);
        switch (requestCode) {
            case ExtraListNewActivity.REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Bundle b = intent.getExtras();
                    ExtraList tl = new ExtraList();
                    tl.name = b.getString(Constants.EXTRA_LIST_NEW_NAME);
                    tl.url = URI.create(b.getString(Constants.EXTRA_LIST_NEW_URI));
                    tl.enabled = b.getBoolean(Constants.EXTRA_LIST_NEW_ENABLED);
                    tl.featured = b.getBoolean(Constants.EXTRA_LIST_NEW_FEATURED);
                    if (b.getBoolean(Constants.EXTRA_LIST_NEW_UPDATE))
                        tl.PrimaryKey = b.getInt(Constants.EXTRA_LIST_NEW_PRIMARYKEY);
                    if (!b.getBoolean(Constants.EXTRA_LIST_NEW_UPDATE))
                        extraListDb.insertExtra(tl);
                    else
                        extraListDb.updateExtra(b.getInt(Constants.EXTRA_LIST_NEW_PRIMARYKEY), tl);
                    updateExtraList();
                }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void updateFeaturedExtras() {
        if (showDebugOutput) Log.d(TAG, "Called Update Featured Extras");
        FeaturedExtrasProgressHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (showDebugOutput) Log.d(TAG, "recieved Message");
                if (FeaturedExtrasProgressDialog != null)
                    FeaturedExtrasProgressDialog.dismiss();
                if (msg.obj instanceof String) {
                    Toast.makeText(ExtraListActivity.this, (CharSequence) msg.obj, Toast.LENGTH_LONG).show();
                    FeaturedExtras = null;
                    ExtraListActivity.this.FeaturedExtrasThread.interrupt();
                    FeaturedExtrasProgressDialog.dismiss();
                } else if (msg.obj instanceof FullExtraList) {
                    FeaturedExtras = (FullExtraList) msg.obj;
                    ExtraListActivity.this.FeaturedExtrasThread.interrupt();
                    FeaturedExtrasProgressDialog.dismiss();
                    if (FeaturedExtras != null && FeaturedExtras.getExtraCount() > 0) {
                        extraListDb.UpdateFeaturedExtras(FeaturedExtras);
                        updateExtraList();
                        Toast.makeText(ExtraListActivity.this, R.string.featured_extras_finished_toast, Toast.LENGTH_LONG).show();
                    }
                }
            }
        };
        FeaturedExtrasProgressDialog = ProgressDialog.show(this, res.getString(R.string.featured_extras_progress_title), res.getString(R.string.featured_extras_progress_body), true);
        FeaturedExtrasThread = new Thread(new FeaturedExtras(new Preferences(this).getFeaturedExtrasURL()));
        FeaturedExtrasThread.start();
    }
}
