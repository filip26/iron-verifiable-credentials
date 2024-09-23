package com.apicatalog.vc.status.bitstring;

import java.util.Collection;

public interface BitstringStatusListMessages {

    Collection<String> code();

    String message(String code);

    int size();

}
