package org.fao.fenix.fmd.tools.rest;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class DefaultJsonOptions implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper;

    public DefaultJsonOptions() {
        mapper = new ObjectMapper();
        mapper.enable(Feature.INDENT_OUTPUT);
        mapper.disable(Feature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_EMPTY);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}