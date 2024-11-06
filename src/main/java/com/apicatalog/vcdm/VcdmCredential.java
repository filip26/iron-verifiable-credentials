package com.apicatalog.vcdm;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.model.CredentialSchema;
import com.apicatalog.vc.model.Evidence;
import com.apicatalog.vc.model.RefreshService;
import com.apicatalog.vc.model.TermsOfUse;
import com.apicatalog.vc.status.Status;

/**
 * A common ancestor to all W3C VCDM based verifiable credentials.
 */
@Fragment
@Term("VerifiableCredential")
public interface VcdmCredential extends VcdmVerifiable, Credential {
    
    @Term
    Collection<Evidence> evidence();

    @Term
    Collection<TermsOfUse> termsOfUse();
    
    @Term
    Collection<RefreshService> refreshService();
    
    @Term("credentialSchema")
    Collection<CredentialSchema> schema();

    @Override
    default void validate() throws DocumentError {

        // @type - mandatory
        if (type() == null || type().isEmpty()) {
            throw new DocumentError(ErrorType.Missing, JsonLdKeyword.TYPE);
        }

        // subject - mandatory
        if (subject() == null || subject().isEmpty()) {
            throw new DocumentError(ErrorType.Missing, VcdmVocab.SUBJECT);
        }

        // issuer
        if (issuer() == null) {
            throw new DocumentError(ErrorType.Missing, VcdmVocab.ISSUER);
        }
        if (issuer().id() == null) {
            throw new DocumentError(ErrorType.Missing, "IssuerId");
        }
        
        issuer().validate();

        // status
        if (status() != null) {
            for (final Status item : status()) {
                if (item.type() == null || item.type().isEmpty()) {
                    throw new DocumentError(ErrorType.Missing, "StatusType");
                }
                item.validate();
            }
        }

        if (termsOfUse() != null) {
            for (final TermsOfUse item : termsOfUse()) {
                if (item.type() == null || item.type().isEmpty()) {
                    throw new DocumentError(ErrorType.Missing, "TermsOfUseType");
                }
                item.validate();
            }
        }

        if (evidence() != null) {
            for (final Evidence item : evidence()) {
                if (item.type() == null || item.type().isEmpty()) {
                    throw new DocumentError(ErrorType.Missing, "EvidenceType");
                }
                item.validate();
            }
        }

        if (refreshService() != null) {
            for (final RefreshService item : refreshService()) {
                if (item.type() == null || item.type().isEmpty()) {
                    throw new DocumentError(ErrorType.Missing, "RefreshServiceType");
                }
                item.validate();
            }
        }

        if (schema() != null) {
            for (final CredentialSchema item : schema()) {
                if (item.type() == null || item.type().isEmpty()) {
                    throw new DocumentError(ErrorType.Missing, "CredentialSchemaType");
                }
            }
        }        
    }
}
