package org.fao.fenix.fmd.storage.dto.templates;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ResponseBeanFactory {

    public static ThreadLocal<String> template = new ThreadLocal<>();

    private static Map<Class,Class> proxiedClasses = new HashMap<>();

    public static <T extends ResponseHandler> Object getInstance(Object source, Class<T> destinationClass, boolean useTemplate) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
        if (source==null)
            return null;

        String templateName = useTemplate ? template.get() : null;
        Class templateClass = templateName!=null ? Class.forName("com.daje.hoolifan.storage.dto.templates."+templateName+'.'+destinationClass.getSimpleName()) : destinationClass;
        Class proxyClass = proxiedClasses.get(templateClass);

        if (proxyClass==null) {
            ProxyFactory proxyFactory = new ProxyFactory();
            proxyFactory.setSuperclass(templateClass);
            proxyFactory.setUseCache(false);
            proxyFactory.setFilter(new MethodFilter() {
                @Override
                public boolean isHandled(Method method) {
                    return true;
                }
            });
            proxiedClasses.put(templateClass, proxyClass = proxyFactory.createClass());
        }

        T instance = (T)proxyClass.newInstance();
        ((Proxy)instance).setHandler((ResponseHandler)templateClass.getConstructor(Object.class).newInstance(source));
        return instance;
    }

    public static <T extends ResponseHandler> Collection getInstances(Collection<?> sourceCollection, Class<T> destinationClass, boolean useTemplate) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
        Collection instances = null;
        if (sourceCollection!=null) {
            instances = new ArrayList<>(sourceCollection.size());
            for (Object source : sourceCollection)
                instances.add(getInstance(source, destinationClass, useTemplate));
        }
        return instances;
    }
}
