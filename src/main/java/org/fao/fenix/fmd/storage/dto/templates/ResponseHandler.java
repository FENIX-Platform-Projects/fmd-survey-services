package org.fao.fenix.fmd.storage.dto.templates;

import org.fao.fenix.fmd.storage.dto.JSONdto;
import javassist.util.proxy.MethodHandler;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public abstract class ResponseHandler extends JSONdto implements MethodHandler {

    private static final Set<String> returnedDTOChecked = new HashSet<>();
    private static final Set<String> returnCollectionOfDTO = new HashSet<>();
    private static final Map<String, Class<? extends ResponseHandler>> returnedDTO = new HashMap<>();


    private Object source;
    private Class sourceClass;

    public ResponseHandler() {}
    public ResponseHandler(Object source) {
        this.source = source;
        sourceClass = source.getClass();
    }

    @Override
    public void setDefaults() {

    }

    @Override
    public Object invoke(Object self, Method m, Method processed, Object[] args) throws Throwable {
        //Retrieve informations from cache
        String key = this.getClass().getName()+'.'+m.getName();
        Class<? extends ResponseHandler> returnHandlerClass = returnedDTO.get(key);
        boolean collection = returnCollectionOfDTO.contains(key);

        //Update cache if necessary
        if (!returnedDTOChecked.contains(key)) {
            Type returnType = m.getGenericReturnType();
            Class returnClass = m.getReturnType();
            if (collection = Collection.class.isAssignableFrom(returnClass)) {
                Class elementsClass = (Class) ((ParameterizedType) returnType).getActualTypeArguments()[0];
                if (JSONdto.class.isAssignableFrom(elementsClass))
                    returnedDTO.put(key, returnHandlerClass = (Class<? extends ResponseHandler>)elementsClass);
                returnCollectionOfDTO.add(key);
            } else {
                if (JSONdto.class.isAssignableFrom(returnClass))
                    returnedDTO.put(key, returnHandlerClass = (Class<? extends ResponseHandler>)returnClass);
            }
            returnedDTOChecked.add(key);
        }

        //Call original bean method
        Object sourceReturn = null;
        try {
            sourceReturn = sourceClass.getMethod(m.getName()).invoke(source);
        } catch (NoSuchMethodException ex) {
            sourceReturn = processed.invoke(self);
        }
        //Return response
        if (returnHandlerClass!=null) //Override response if needed
            if (collection)
                return ResponseBeanFactory.getInstances((Collection)sourceReturn, returnHandlerClass, false);
            else
                return ResponseBeanFactory.getInstance(sourceReturn, returnHandlerClass, false);
        else //Return original response
            return sourceReturn;
    }
}
