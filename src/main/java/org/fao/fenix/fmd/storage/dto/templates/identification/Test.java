package org.fao.fenix.fmd.storage.dto.templates.identification;

import org.fao.fenix.fmd.storage.dto.templates.ResponseHandler;
import org.codehaus.jackson.annotate.JsonProperty;


public class Test extends ResponseHandler {

    public Test() {}
    public Test(Object source) {
        super(source);
    }

    @JsonProperty
    public String getTitle() {
        return null;
    }
}
