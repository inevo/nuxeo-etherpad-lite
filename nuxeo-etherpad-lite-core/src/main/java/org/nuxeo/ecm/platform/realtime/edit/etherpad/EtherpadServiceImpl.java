/**
 *
 */

package org.nuxeo.ecm.platform.realtime.edit.etherpad;

import java.io.IOException;
import org.etherpad_lite_client.EPLiteClient;
import org.etherpad_lite_client.EPLiteException;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.platform.realtime.edit.AbstractRealtimeEditService;
import org.nuxeo.runtime.model.ComponentContext;


/**
 * @author nfgs
 */
public class EtherpadServiceImpl extends AbstractRealtimeEditService implements EtherpadService {

    private EPLiteClient client;

    private String BASE_URL = "http://localhost:9001";
    private String API_KEY = "N4jKGUiD66wRP8eKAe1TReo2zC2LVoEa";

    @Override
    public void applicationStarted(ComponentContext context) throws Exception {

    }

    EPLiteClient getClient() {
        if (client == null) {
            client = new EPLiteClient(BASE_URL, API_KEY);
        }
        return client;
    }

    @Override
    public String createSession(String username, String title, Blob blob) throws ClientException {
        String padId = title;
        try {
            deletePad(padId);
        } catch (EPLiteException e) { }

        try {
            createPad(padId);
            setPadContent(padId, blob);
        } catch (IOException e) {
            throw ClientException.wrap(e);
        }

        return title;
    }

    @Override
    public void updateSession(String sessionId, String username, Blob blob) throws ClientException {
        try {
            setPadContent(sessionId, blob);
        } catch (IOException e) {
            throw ClientException.wrap(e);
        }
    }

    @Override
    public String getEmbeddedURL(String sessionId, String username) {
        String url = getURL(sessionId);

        url += "?showControls=true";
        url += "&showChat=true";
        url += "&showLineNumbers=false";
        url += "&useMonospaceFont=false";
        url += "&userName=" + username;
        url += "&noColors=false";
        url += "&userColor=false";
        url += "&hideQRCode=false";
        url += "&alwaysShowChat=false";

        return url;
    }

    @Override
    public String getURL(String sessionId) {
        return BASE_URL + "/p/" + sessionId;
    }

    private void deletePad(String padId) throws EPLiteException {
        getClient().deletePad(padId);
    }

    private void createPad(String padId) throws EPLiteException {
        getClient().createPad(padId);
    }

    private String getPadContent(String padId, String mimeType) throws EPLiteException {
        if (mimeType.equals("text/html")) {
            return (String) getClient().getHTML(padId).get("html");
        }

        return (String) getClient().getText(padId).get("text");
    }

    private void setPadContent(String padId, Blob blob) throws IOException {
        String mimeType = blob.getMimeType();
        String content = blob.getString();

        if (mimeType.equals("text/html")) {
            getClient().setHTML(padId, content);
        } else {
            getClient().setText(padId, content);
        }
    }


    @Override
    public Blob getSessionBlob(String sessionId, String mimeType) {
        String content = getPadContent(sessionId, mimeType);
        Blob blob = new StringBlob(content, mimeType);
        return blob;
    }

    @Override
    public void deleteSession(String sessionId) {
        deletePad(sessionId);
    }

    @Override
    public boolean existsSession(String sessionId) {
        boolean exists = true;
        try {
            getClient().getRevisionsCount(sessionId);
        } catch (Exception e) {
            exists = false;
        }
        return exists;
    }


}
