package com.apicatalog.trust.semantic;

import com.apicatalog.trust.Document;

public interface GraphUpdater extends Document.Updater {

    GraphPayloadGenerator createPayload();
    
}
