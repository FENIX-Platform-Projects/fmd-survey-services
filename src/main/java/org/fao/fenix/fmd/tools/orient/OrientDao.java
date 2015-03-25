package org.fao.fenix.fmd.tools.orient;

import com.orientechnologies.orient.core.exception.OSerializationException;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.query.OQuery;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.fao.fenix.fmd.storage.dto.JSONdto;

import javax.inject.Inject;
import javax.persistence.*;
import javax.ws.rs.core.NoContentException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public abstract class OrientDao {
    @Inject DatabaseStandards dbParameters;
    @Inject OrientClient client;

    //Connection utils
    protected OObjectDatabaseTx getConnection() {
        return dbParameters.getConnection();
    }


    //Load utils
    private static Map<String,OSQLSynchQuery> queries = new TreeMap<>();

    public static <T> OSQLSynchQuery<T> createSelect(String query, Class<T> type) {
        return new OSQLSynchQuery<>(query);
    }

    public synchronized <T> Collection<T> select(Class<T> type, String query, Object... params) throws Exception {
        return select(type, query, dbParameters.getOrderingInfo(), dbParameters.getPaginationInfo(), params);
    }
    public synchronized <T> Collection<T> select(Class<T> type, String query, Order ordering, Page paging, Object... params) throws Exception {
        if (ordering!=null)
            query += ordering.toSQL();
        if (paging!=null)
            query += paging.toSQL();

        OSQLSynchQuery queryO = queries.get(type.getSimpleName()+query);
        if (queryO==null)
            queries.put(type.getSimpleName()+query, queryO = createSelect(query,type));

        return select(queryO, params);
    }
    public synchronized <T> Collection<T> select(OQuery<T> query, Object... params) throws Exception {
        try {
            query.reset();
            if (query instanceof OSQLSynchQuery)
                ((OSQLSynchQuery) query).resetPagination();
            return (Collection<T>) getConnection().query(query, params);
        } catch (OSerializationException ex) {
            client.registerPersistentEntities();
            return select(query,params);
        }
    }

    public <T> Iterator<T> browse(Class<T> type) throws Exception {
        return getConnection().browseClass(type);
    }
    public Iterator<ODocument> browse(String className) throws Exception {
        return getConnection().browseClass(className);
    }

    public <T> T loadBean (String rid, Class<T> type) throws Exception {
        try {
            return (T)loadBean(toRID(rid));
        } catch (ClassCastException ex) {
            throw new NoContentException("Illegal type '"+type+"' for the entity "+rid);
        }
    }
    public Object loadBean (String rid) throws Exception { return loadBean(toRID(rid)); }
    public <T extends JSONdto> T loadBean (T bean) throws Exception { return (T)loadBean(bean.getORID()); }
    public Object loadBean (ORID orid) throws Exception {
        try {
            Object entity = orid != null ? getConnection().load(orid) : null;
            if (entity == null)
                throw new NoContentException(toString(orid));
            return entity;
        } catch (OSerializationException ex) {
            client.registerPersistentEntities();
            return loadBean(orid);
        }
    }


    protected enum QueryOperators {
        equals(" = ?"), different(" <> ?"), lower(" < ?"), higher(" > ?"), lowerEquals(" <= ?"), higherEquals(" >= ?"), contains (" contains ?");

        private String operator;
        QueryOperators(String operator) {
            this.operator = operator;
        }

        public String getOperator() {
            return operator;
        }
    }

    protected String getQueryWhereCondition (Collection<Object> paramsBuffer, String[] names, Object[] values, QueryOperators[] operators) {
        paramsBuffer.clear();
        StringBuilder buffer = new StringBuilder(" where ");
        for (int i=0; i<names.length; i++)
            if (values[i]!=null) {
                buffer.append(names[i]).append((operators!=null ? operators[i] : QueryOperators.equals).getOperator()).append(" and ");
                paramsBuffer.add(values[i]);
            }
        return paramsBuffer.size()>0 ? buffer.substring(0,buffer.length()-5) : "";
    }



    //Save utils
    class MethodGetSet {
        Method get, set;
        MethodGetSet(Method get, Method set) {
            this.get = get;
            this.set = set;
        }
    }
    private static final Set<Class> entityClass = new HashSet<>();
    private static final Map<Class,Collection<MethodGetSet>> standardGetSet = new HashMap<>();
    private static final Map<Class,Collection<MethodGetSet>> entityGetSet = new HashMap<>();
    private static final Map<Class,Collection<MethodGetSet>> entityCollectionGetSet = new HashMap<>();
    private static final Map<Method,Boolean> embeddedGetSet = new HashMap<>();


    public <T extends JSONdto> T newCustomEntity(T bean, boolean ... checks) {
        boolean cycleCheck = checks!=null && checks.length>0 && checks[0]; //false by default
        try {
            bean.setRID(null); //Ignore bean ORID
            return saveCustomEntity(bean, false, cycleCheck); //Save in append mode for connected entities
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public <T extends JSONdto> T saveCustomEntity(T bean, boolean ... checks) throws Exception {
        boolean overwrite = checks!=null && checks.length>0 && checks[0]; //false by default
        boolean cycleCheck = checks!=null && checks.length>1 && checks[1]; //false by default

        OObjectDatabaseTx connection = null;
        try {
            connection = getConnection();
            connection.begin();
            bean = saveCustomEntity(bean, overwrite, cycleCheck ? new HashMap<>() : null, getConnection(), false);
            connection.commit();
            return bean;
        } catch (OSerializationException e) {
            if (connection!=null)
                connection.rollback();
            client.registerPersistentEntities();
            return saveCustomEntity(bean, overwrite);
        } catch (Exception e) {
            if (connection!=null)
                connection.rollback();
            throw e;
        }
    }
    private <T extends JSONdto> T saveCustomEntity(T bean, boolean overwrite, Map<Object,Object> buffer, OObjectDatabaseTx connection, boolean embedded) throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException, NoContentException {
        //Avoid cycle and useless proxy create/load
        if (bean==null)
            return null;
        if (buffer!=null && buffer.containsKey(bean))
            return (T)buffer.get(bean);

        //Init recursion information about this entity
        Class<T> beanClass = (Class<T>) bean.getClass();
        if (!entityClass.contains(beanClass))
            initEntityRecursionInformation(beanClass);

        //Load/create proxy bean
        ORID orid = bean.getORID();
        T beanProxy = orid!=null ? (T)connection.load(orid) : connection.newInstance(beanClass);
        if (beanProxy==null)
            throw new NoContentException("Cannot find bean '"+bean.getRID()+'\'');
        if (buffer!=null)
            buffer.put(bean,beanProxy);

        //Set default values for new entities
        if (orid==null)
            bean.setDefaults();

        //Retrieve fields value and apply recursion
        boolean empty = true;
        Set<Method> nullFields = new HashSet<>();

        Collection<? extends JSONdto> collectionFieldValue;
        Object fieldValue;

        for (MethodGetSet methodGetSet : entityCollectionGetSet.get(beanClass))
            if ((collectionFieldValue = (Collection) methodGetSet.get.invoke(bean))!=null && collectionFieldValue.size()>0) {
                //Collect new proxy entities
                empty = false;
                Collection<JSONdto> proxyCollectionFieldValue = new HashSet<>();
                for (JSONdto elementValue : collectionFieldValue)
                    proxyCollectionFieldValue.add(saveCustomEntity(elementValue, overwrite, buffer, connection, embeddedGetSet.get(methodGetSet.set)));
                //In append mode add old proxy entities (duplicates are avoided by default by Java HashSet)
                if (!overwrite) {
                    Collection<? extends JSONdto> existingProxyCollectionFieldValue = (Collection)methodGetSet.get.invoke(beanProxy);
                    if (existingProxyCollectionFieldValue!=null && existingProxyCollectionFieldValue.size()>0)
                        for (Object existingValue : existingProxyCollectionFieldValue)
                            if (existingValue!=null) //Avoid removed links
                                proxyCollectionFieldValue.add((JSONdto) existingValue);
                }
                //Set new value
                methodGetSet.set.invoke(beanProxy,new LinkedList<>(proxyCollectionFieldValue));
            } else if (overwrite) //In overwrite mode maintain nullable fields for the last step
                nullFields.add(methodGetSet.set);

        for (MethodGetSet methodGetSet : entityGetSet.get(beanClass))
            if ((fieldValue=methodGetSet.get.invoke(bean)) != null) {
                empty = false;
                methodGetSet.set.invoke(beanProxy, saveCustomEntity((JSONdto) fieldValue, overwrite, buffer, connection, embeddedGetSet.get(methodGetSet.set)));
            } else if (overwrite)
                nullFields.add(methodGetSet.set);

        for (MethodGetSet methodGetSet : standardGetSet.get(beanClass))
            if ((fieldValue=methodGetSet.get.invoke(bean)) != null) {
                empty = false;
                methodGetSet.set.invoke(beanProxy, fieldValue);
            } else if (overwrite)
                nullFields.add(methodGetSet.set);

        //Set null field values of non empty bean if in overwrite mode
        if (overwrite && !empty)
            for (Method set : nullFields)
                set.invoke(beanProxy,new Object[] {null});

        //Return updated proxy bean
        if (!embedded)
            connection.save(beanProxy);
        return beanProxy;
    }


    private synchronized <T extends JSONdto> void initEntityRecursionInformation (Class<T> beanClass) throws NoSuchMethodException {
        Collection<MethodGetSet> standardGetSetCollection = new LinkedList<>();
        Collection<MethodGetSet> entityGetSetCollection = new LinkedList<>();
        Collection<MethodGetSet> entityCollectionGetSetCollection = new LinkedList<>();
        standardGetSet.put(beanClass,standardGetSetCollection);
        entityGetSet.put(beanClass,entityGetSetCollection);
        entityCollectionGetSet.put(beanClass,entityCollectionGetSetCollection);

        for (Class<? extends JSONdto> c = beanClass; !c.equals(JSONdto.class); c = (Class<? extends JSONdto>) c.getSuperclass())
            for (Method getter : c.getDeclaredMethods())
                if (getter.getName().startsWith("get") && !getter.getName().equals("getRID") && !getter.getName().equals("getORID")) {
                    Class returnClass = getter.getReturnType();
                    MethodGetSet getSet = new MethodGetSet(getter, beanClass.getMethod('s'+getter.getName().substring(1),returnClass));
                    if (Collection.class.isAssignableFrom(returnClass)) {
                        Class elementClass = (Class) ((ParameterizedType) getter.getGenericReturnType()).getActualTypeArguments()[0];
                        if (JSONdto.class.isAssignableFrom(elementClass))
                            entityCollectionGetSetCollection.add(getSet);
                        else
                            standardGetSetCollection.add(getSet);
                    } else if (JSONdto.class.isAssignableFrom(returnClass))
                        entityGetSetCollection.add(getSet);
                    else
                        standardGetSetCollection.add(getSet);
                    embeddedGetSet.put(getSet.set,getSet.set.isAnnotationPresent(Embedded.class));
                }
        entityClass.add(beanClass);
    }



    //Delete utils
    public void deleteBean (String rid) throws NoContentException {
        deleteBean(toRID(rid));
    }
    public void deleteBean (ORID orid) throws NoContentException {
        if (orid==null || getConnection().load(orid)==null)
            throw new NoContentException("Cannot find bean '"+orid+'\'');
        else
            getConnection().delete(orid);
    }


    public static String toString (ORID rid) {
        return rid!=null && rid.getClusterId()>0 ? rid.getClusterId()+"_"+rid.getClusterPosition() : null;
    }
    public static ORID toRID(String rid) {
        if (rid!=null) {
            int splitIndex = rid.indexOf('_');
            return new ORecordId(splitIndex>0 ? '#'+rid.substring(0, splitIndex)+':'+rid.substring(splitIndex+1) : rid);
        } else
            return null;
    }


    protected String getRidList(Collection<? extends JSONdto> beans) {
        if (beans!=null && beans.size()>0) {
            StringBuilder buffer = new StringBuilder();
            for (JSONdto bean : beans)
                buffer.append('\'').append(bean.getRID()).append("', ");
            return buffer.substring(0, buffer.length()-2);
        }
        return null;
    }
}
