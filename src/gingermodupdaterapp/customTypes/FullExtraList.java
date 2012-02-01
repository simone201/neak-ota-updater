package gingermodupdaterapp.customTypes;

import gingermodupdaterapp.misc.Log;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;

public class FullExtraList implements Serializable {
    private static final long serialVersionUID = -2577705903002871714L;

    private static final String TAG = "FullExtraList";

    private final LinkedList<ExtraList> Extras;

    public FullExtraList() {
        Extras = new LinkedList<ExtraList>();
    }

    public LinkedList<ExtraList> returnFullExtraList() {
        Collections.sort(Extras);
        return Extras;
    }

    public void addExtraToList(ExtraList t) {
        Extras.add(t);
    }

    public boolean removeExtraFromList(ExtraList t) {
        try {
            Extras.remove(Extras.indexOf(t));
            return true;
        }
        catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "Exception on Deleting Extra from List", e);
            return false;
        }
    }

    public int getExtraCount() {
        return Extras.size();
    }
}
