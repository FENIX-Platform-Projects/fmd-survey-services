package org.fao.fenix.fmd.tools.orient;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

public class DatabaseStandards {
    private static ThreadLocal<OObjectDatabaseTx> connection = new ThreadLocal<>();
    private static ThreadLocal<Page> paginationInfo = new ThreadLocal<>();
    private static ThreadLocal<Order> orderingInfo = new ThreadLocal<>();


    public OObjectDatabaseTx getConnection() {
        return connection.get();
    }

    public void setConnection(OObjectDatabaseTx c) {
        connection.set(c);
    }

    public Page getPaginationInfo() {
        return paginationInfo.get();
    }

    public void setPaginationInfo(Page p) {
        paginationInfo.set(p);
    }

    public Order getOrderingInfo() {
        return orderingInfo.get();
    }

    public void setOrderingInfo(Order o) {
        orderingInfo.set(o);
    }
}
