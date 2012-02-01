package gingermodupdaterapp.customTypes;

import java.io.Serializable;
import java.net.URI;

public class ExtraList implements Serializable, Comparable<ExtraList> {
    private static final long serialVersionUID = 8861171977383611130L;

    public int PrimaryKey;
    public String name;
    public URI url;
    public boolean enabled;
    public boolean featured;

    public ExtraList() {
        featured = false;
        enabled = true;
    }

    public int compareTo(ExtraList another) {
        return this.name.compareToIgnoreCase(another.name);
    }
}
