package org.fao.fenix.fmd.storage.dto.templates.admin;

import org.fao.fenix.fmd.storage.dto.templates.ResponseHandler;
import org.codehaus.jackson.annotate.JsonProperty;


public class TestBean extends ResponseHandler {

    public TestBean() {}
    public TestBean(Object source) {
        super(source);
    }

    @JsonProperty
    public String getTitle() {
        return null;
    }
    @JsonProperty
    public Boolean getActive() {
        return null;
    }
}
