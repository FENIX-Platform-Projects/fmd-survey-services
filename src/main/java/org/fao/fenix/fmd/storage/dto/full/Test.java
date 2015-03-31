package org.fao.fenix.fmd.storage.dto.full;

import org.fao.fenix.fmd.storage.dto.JSONdto;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

public class Test extends JSONdto implements Serializable {
    
    private @JsonProperty String title;
    private @JsonProperty Boolean active;

    @Override
    public void setDefaults() {
        if (active==null)
            active = Boolean.FALSE;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
