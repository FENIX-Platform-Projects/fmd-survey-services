package org.fao.fenix.fmd.storage.dto;

//import com.daje.hoolifan.storage.dto.full.*;
import org.fao.fenix.fmd.tools.orient.OrientDao;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.object.enhancement.OObjectProxyMethodHandler;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.persistence.Version;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect({JsonMethod.NONE})
public abstract class JSONdto implements Comparable<JSONdto> {
    private ORID orid;
    @Version private Object oversion;


    @JsonProperty
    public String getRID() {
        return OrientDao.toString(getORID());
    }

    @JsonProperty
    public void setRID(String rid) {
        orid = OrientDao.toRID(rid);
    }

    public ORID getORID() {
        if (orid==null && this instanceof Proxy) {
            MethodHandler proxy = ProxyFactory.getHandler((Proxy) this);
            if (proxy instanceof OObjectProxyMethodHandler)
                return ((OObjectProxyMethodHandler) proxy).getDoc().getIdentity();
        }
        return orid;
    }

    public abstract void setDefaults();

/*
    public static Collection<Class<? extends JSONdto>> getDtoList() {
        //TODO return by reflection using class utils

        Collection<Class<? extends JSONdto>> dtoList = new LinkedList<>();

        dtoList.add(Championship.class);

        return dtoList;
    }
*/

    //Utils
    public <T> void add(T data, String property) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> thisClass = this.getClass();
        Method get = thisClass.getMethod("get" + Character.toUpperCase(property.charAt(0)) + property.substring(1));
        Method set = thisClass.getMethod("set"+Character.toUpperCase(property.charAt(0))+property.substring(1), Collection.class);

        Collection<T> list = (Collection<T>) get.invoke(this);
        if (list==null)
            list = new LinkedList<>();
        list.add(data);
        set.invoke(this, list);
    }



    //Compare


    @Override
    public int hashCode() {
        return getRID().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj!=null && obj.getClass().equals(this.getClass()) && getRID().equals(((JSONdto)obj).getRID());
    }

    @Override
    public int compareTo(JSONdto obj) {
        return obj!=null && obj.getClass().equals(this.getClass()) ? getRID().compareTo(obj.getRID()) : 1;
    }
}
