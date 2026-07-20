package com.apicatalog.trust.semantic;

import com.apicatalog.trust.Document;
import com.apicatalog.trust.payload.PayloadGenerator;

public interface GraphUpdater extends Document.Updater {
    
    PayloadGenerator createPayload();
    
}
