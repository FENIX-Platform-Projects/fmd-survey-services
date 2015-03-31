package org.fao.fenix.fmd.tools.orient;

import com.orientechnologies.orient.object.db.OObjectDatabasePool;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

import javax.enterprise.context.ApplicationScoped;
import java.util.Properties;

@ApplicationScoped
public class OrientClient {

    //Support Java objects out of CDI context
    private static OrientClient instance;
    public static OrientClient getSingletonInstance() {
        return instance!=null?instance:(instance=new OrientClient());
    }

    public OrientClient() {
        instance = this;
    }

    private String url,usr,psw;
    private boolean initialized;

    //Init flow
    public void init(Properties initProperties) {
        try {
            if (!initialized) {
                url = initProperties.getProperty("database.url", "remote:localhost/fenix_catalog_1.0");
                usr = initProperties.getProperty("database.usr", "admin");
                psw = initProperties.getProperty("database.psw", "admin");

                destroyConnectionPool();
                initConnectionPool(Math.max(10, Integer.parseInt(initProperties.getProperty("database.connections", "100"))));
                initialized = true;
                registerPersistentEntities();
            }
        } catch (Exception ex) {
            throw new ExceptionInInitializerError("Error in database component initialization");
        }
    }
    public void destroy() {
        destroyConnectionPool();
    }



    public void registerPersistentEntities() throws Exception {
        OObjectDatabaseTx connection = null;
        try {
            connection = objectPool.acquire(url, usr, psw);
            connection.setAutomaticSchemaGeneration(false);
            connection.getEntityManager().registerEntityClasses("org.fao.fenix.fmd.storage.dto.full");
        } finally {
            if (connection!=null)
                connection.close();
        }
    }
/*
    public void registerPersistentEntities() throws Exception {
        for (Class<?> entityClass : JSONdto.getDtoList())
            registerPersistentEntity(entityClass);
    }
    private void registerPersistentEntity(Class<?> entityClass) {
        OObjectDatabaseTx connection = null;
        try {
            connection = objectPool.acquire(url, usr, psw);
            connection.getEntityManager().registerEntityClass(entityClass);
        } finally {
            if (connection!=null)
                connection.close();
        }
    }
*/

    //Connection pool management
    private OObjectDatabasePool objectPool;
    private void initConnectionPool(int maxConnections) {
        maxConnections = Math.max(10,maxConnections);
        objectPool = new OObjectDatabasePool();
        objectPool.setup(10,maxConnections);
    }
    private void destroyConnectionPool() {
        if (objectPool!=null) {
            objectPool.close();
            objectPool = null;
        }
    }


    //Connection management
    public OObjectDatabaseTx getConnection() throws Exception {
        try {
            return objectPool.acquire(url, usr, psw);
        } catch (Exception ex) {
            initialized = false;
            throw ex;
        }
    }
}
