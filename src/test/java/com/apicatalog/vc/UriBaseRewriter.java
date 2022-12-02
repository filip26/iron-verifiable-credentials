package com.apicatalog.vc;

import java.net.URI;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdErrorCode;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;

public final class UriBaseRewriter implements DocumentLoader {

    private final String sourceBase;
    private final String targetBase;

    private final DocumentLoader loader;

    public UriBaseRewriter(final String sourceBase, final String targetBase, final DocumentLoader loader) {
        this.sourceBase = sourceBase;
        this.targetBase = targetBase;

        this.loader = loader;
    }

    @Override
    public Document loadDocument(final URI url, final DocumentLoaderOptions options) throws JsonLdError {

        final String sourceUrl = url.toString();

        if (!sourceUrl.startsWith(sourceBase)) {
            return loader.loadDocument(url, options);
        }

        final String relativePath = sourceUrl.substring(sourceBase.length());

        final Document remoteDocument = loader.loadDocument(URI.create(targetBase + relativePath), options);

        if (remoteDocument == null) {
            throw new JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED);
        }

        if (remoteDocument.getDocumentUrl() != null && remoteDocument.getDocumentUrl().toString().startsWith(targetBase)) {

            final String remoteRelativePath = remoteDocument.getDocumentUrl().toString().substring(targetBase.length());
            remoteDocument.setDocumentUrl(URI.create(sourceBase + remoteRelativePath));

        }
        return remoteDocument;

    }
}
