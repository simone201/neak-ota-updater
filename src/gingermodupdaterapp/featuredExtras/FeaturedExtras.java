package gingermodupdaterapp.featuredExtras;

import android.os.Message;
import gingermodupdaterapp.misc.Log;
import gingermodupdaterapp.ui.ExtraListActivity;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class FeaturedExtras implements Runnable {
    private static final String TAG = "FeaturedExtras";
    private final String mUrl;

    public FeaturedExtras(String featuredExtrasUrl) {
        mUrl = featuredExtrasUrl;
    }

    public void run() {
        URL url;
        InputSource i;

        Message m = ExtraListActivity.FeaturedExtrasProgressHandler.obtainMessage();
        try {
            url = new URL(mUrl);
            i = new InputSource(url.openStream());
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            FeaturedExtrasHandler fth = new FeaturedExtrasHandler();
            xr.setContentHandler(fth);
            xr.parse(i);
            m.obj = fth.getParsedData();
        }
        catch (MalformedURLException e) {
            m.obj = e.toString();
            Log.e(TAG, "Malformed URL!", e);
        }
        catch (IOException e) {
            m.obj = e.toString();
            Log.e(TAG, "Exception on opening Input Stream", e);
        }
        catch (ParserConfigurationException e) {
            m.obj = e.toString();
            Log.e(TAG, "Exception on parsing XML File", e);
        }
        catch (SAXException e) {
            m.obj = e.toString();
            Log.e(TAG, "Exception while creating SAXParser", e);
        }
        ExtraListActivity.FeaturedExtrasProgressHandler.sendMessage(m);
    }
}
