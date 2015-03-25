package org.fao.fenix.fmd.tools.utils;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import javax.enterprise.context.ApplicationScoped;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

@ApplicationScoped
public class JSONUtils {
    private JsonFactory jsonFactory = new JsonFactory();
	private ObjectMapper mapper = new ObjectMapper();
	
	public String toJSON(Object obj) throws Exception { return mapper.writeValueAsString(obj); }

    public void toJSON(Iterator<?> objs, Writer writer) throws Exception {
        JsonGenerator generator = jsonFactory.createJsonGenerator(writer);
        while (objs.hasNext())
            mapper.writeValue(generator, objs.next());
        generator.close();
    }
    public JsonGenerator createGenerator(Writer writer) throws Exception {
        return jsonFactory.createJsonGenerator(writer);
    }
    public void toJSON(Iterator<?> objs, JsonGenerator generator) throws Exception {
        while (objs.hasNext())
            mapper.writeValue(generator, objs.next());
    }
    public void toJSON(Object obj, JsonGenerator generator) throws Exception {
        mapper.writeValue(generator, obj);
    }

	public <T> T toObject(String json, Class<T> objClass) throws Exception { return mapper.readValue(json, objClass); }
	public <T> T toObject(Reader json, Class<T> objClass) throws Exception { return mapper.readValue(json, objClass); }

	public JsonParser createParser(Reader reader) throws Exception { return jsonFactory.createJsonParser(reader); }
    public <T> Iterator<T> toObjects(JsonParser parser, Class<T> objClass) throws Exception { return mapper.readValues(parser, objClass); }
    public <T> Iterator<T> toObjects(Reader reader, Class<T> objClass) throws Exception { return mapper.readValues(jsonFactory.createJsonParser(reader), objClass); }

	public <T> T convertValue(Object obj, Class<T> objClass) throws Exception { return mapper.convertValue(obj, objClass); }

	public Map<String,Object> toMap(String json) throws Exception { return mapper.readValue(json, new TypeReference<Map<String, Object>>() {}); }
	
	public Object cloneByJSON (Object obj) throws Exception { return obj!=null ? toObject(toJSON(obj),obj.getClass()) : null; }

}
