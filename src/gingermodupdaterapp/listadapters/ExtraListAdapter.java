package gingermodupdaterapp.listadapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import gingermodupdaterapp.customTypes.ExtraList;
import gingermodupdaterapp.misc.Constants;
import gingermodupdaterapp.ui.R;

import java.util.List;

public class ExtraListAdapter<T> extends ArrayAdapter<T> {
    private final LayoutInflater _inflater;

    public ExtraListAdapter(Context context, List<T> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);
        _inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ExtraListViewWrapper wrapper;
        if (row == null) {
            row = _inflater.inflate(R.layout.itemtemplate_extralist, null);
            wrapper = new ExtraListViewWrapper(row);
            row.setTag(wrapper);
        } else {
            wrapper = (ExtraListViewWrapper) row.getTag();
        }

        ExtraList info = (ExtraList) this.getItem(position);
        wrapper.getExtraNameView().setText(info.name);
        wrapper.getExtraUriView().setText(info.url.toString());
        if (!info.enabled) {
            wrapper.getImageDrawable().mutate().setAlpha(Constants.EXTRA_LIST_ITEM_DISABLED_ALPHA);
            wrapper.getExtraNameView().setTextColor(Color.GRAY);
            wrapper.getExtraUriView().setTextColor(Color.GRAY);
        }
        //We need an else if otherwise the disabled extras will also be yellow
        else if (info.featured) {
            //Mark featured Extras
            wrapper.getExtraNameView().setTextColor(Color.YELLOW);
            wrapper.getExtraUriView().setTextColor(Color.YELLOW);
        } else {
            wrapper.getExtraNameView().setTextColor(Color.WHITE);
            wrapper.getExtraUriView().setTextColor(Color.WHITE);
        }
        return row;
    }
}

//Class that Holds the Ids, so we have not to call findViewById each time which costs a lot of ressources
class ExtraListViewWrapper {
    private final View base;
    private TextView ExtraListName = null;
    private TextView ExtraListUri = null;
    private ImageView image = null;
    private Drawable imageDrawable = null;

    public ExtraListViewWrapper(View base) {
        this.base = base;
    }

    public TextView getExtraNameView() {
        if (ExtraListName == null) {
            ExtraListName = (TextView) base.findViewById(R.id.txtExtraName);
        }
        return ExtraListName;
    }

    public TextView getExtraUriView() {
        if (ExtraListUri == null) {
            ExtraListUri = (TextView) base.findViewById(R.id.txtExtraUri);
        }
        return ExtraListUri;
    }

    void getImage() {
        if (image == null) {
            image = (ImageView) base.findViewById(R.id.ExtraListImage);
        }
    }

    public Drawable getImageDrawable() {
        if (image == null)
            getImage();
        if (imageDrawable == null) {
            imageDrawable = image.getDrawable();
        }
        return imageDrawable;
    }
}
