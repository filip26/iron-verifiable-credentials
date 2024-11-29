package com.apicatalog.vcdm.v11;

import java.util.Collection;

import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.linkedtree.orm.Context;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Provided;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.linkedtree.orm.Vocab;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Presentation;
import com.apicatalog.vc.model.DocumentError;
import com.apicatalog.vc.model.DocumentError.ErrorType;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vcdm.VcdmVerifiable;
import com.apicatalog.vcdm.VcdmVersion;

/**
 * Represents W3C VCDM 1.1 Verifiable Presentation.
 * 
 * @see <a href=
 *      "https://www.w3.org/TR/vc-data-model-1.1/#presentations">Verifiable
 *      Credentials Data Model v1.1</a>
 */
@Fragment
@Term("VerifiablePresentation")
@Vocab("https://www.w3.org/2018/credentials#")
@Context("https://www.w3.org/2018/credentials/v1")
public interface Vcdm11Presentation extends VcdmVerifiable, Presentation {

    @Provided
    @Override
    Collection<Proof> proofs();

    @Provided
    @Override
    Collection<Credential> credentials();

    @Override
    default VcdmVersion version() {
        return VcdmVersion.V11;
    }

    @Override
    default void validate() throws DocumentError {
        // @type - mandatory
        if (type() == null || type().isEmpty()) {
            throw new DocumentError(ErrorType.Missing, JsonLdKeyword.TYPE);
        }
        // credentials
        if (credentials() != null && !credentials().isEmpty()) {
            for (Credential credential : credentials()) {
                credential.validate();
            }
            // anything to present?
        } else if (id() == null && holder() == null) {
            throw new DocumentError(ErrorType.Missing, "Verifiable");
        }
    }
}
