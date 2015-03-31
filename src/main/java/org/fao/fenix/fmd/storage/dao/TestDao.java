package org.fao.fenix.fmd.storage.dao;

import org.fao.fenix.fmd.storage.dto.full.Test;
import org.fao.fenix.fmd.tools.orient.OrientDao;

import javax.ws.rs.core.NoContentException;
import java.util.Collection;
import java.util.LinkedList;

public class TestDao extends OrientDao {



    public Test getTestBean(String rid) throws Exception {
        return loadBean(rid, Test.class);
    }

    public Collection<Test> getTestBeans() throws Exception {
        Collection<Object> params = new LinkedList<>();
        String query = "select from Test"+getQueryWhereCondition(params, new String[]{}, new Object[]{}, null);

        return select(Test.class, query, params.toArray());
    }



    public Test deleteTestBean(String rid) throws Exception {
        Test championship = getTestBean(rid);
        if (championship==null)
            throw new NoContentException(rid);
        championship = getConnection().detach(championship);
        deleteBean(rid);
        return championship;
    }

}
