package gingermodupdaterapp.featuredExtras;

import gingermodupdaterapp.customTypes.FullExtraList;
import gingermodupdaterapp.customTypes.ExtraList;
import gingermodupdaterapp.misc.Constants;
import gingermodupdaterapp.misc.Log;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.net.URI;

class FeaturedExtrasHandler extends DefaultHandler {
    private static final String TAG = "FeaturedExtrasHandler";

    private FullExtraList fullExtraList;
    private ExtraList currentExtra;
    private boolean error = false;

    public FullExtraList getParsedData() {
        return this.fullExtraList;
    }

    @Override
    public void startDocument() throws SAXException {
        this.fullExtraList = new FullExtraList();
    }

    @Override
    public void endDocument() throws SAXException {
        // Nothing to do
    }

    /**
     * Gets be called on opening tags like:
     * <tag>
     * Can provide attribute(s), when xml was like:
     * <tag attribute="attributeValue">
     */
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        if (localName.equalsIgnoreCase(Constants.FEATURED_EXTRAS_TAG)) {
            //New Extra. Start a new Object
            currentExtra = new ExtraList();
            currentExtra.featured = true;
            currentExtra.enabled = true;
            if (atts.getValue(Constants.FEATURES_EXTRAS_TAG_NAME) == null)
                error = true;
            else
                currentExtra.name = atts.getValue(Constants.FEATURES_EXTRAS_TAG_NAME).trim();
            if (atts.getValue(Constants.FEATURES_EXTRAS_TAG_URI) == null)
                error = true;
            else
                currentExtra.url = URI.create(atts.getValue(Constants.FEATURES_EXTRAS_TAG_URI).trim());
        }
    }

    /**
     * Gets be called on closing tags like:
     * </tag>
     */
    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        if (localName.equalsIgnoreCase(Constants.FEATURED_EXTRAS_TAG)) {
            if (!error)
                fullExtraList.addExtraToList(currentExtra);
            else
                Log.e(TAG, "There was an error in the XML File. A value was NULL");
            currentExtra = null;
		}
	}
}
