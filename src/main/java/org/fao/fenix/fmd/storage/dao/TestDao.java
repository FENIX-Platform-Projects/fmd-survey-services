package org.fao.fenix.fmd.storage.dao;

import org.fao.fenix.fmd.storage.dto.full.TestBean;
import org.fao.fenix.fmd.tools.orient.OrientDao;

import javax.ws.rs.core.NoContentException;
import java.util.Collection;
import java.util.LinkedList;

public class TestDao extends OrientDao {



    public TestBean getTestBean(String rid) throws Exception {
        return loadBean(rid, TestBean.class);
    }

    public Collection<TestBean> getTestBeans() throws Exception {
        Collection<Object> params = new LinkedList<>();
        String query = "select from Test"+getQueryWhereCondition(params, new String[]{}, new Object[]{}, null);

        return select(TestBean.class, query, params.toArray());
    }



    public TestBean deleteTestBean(String rid) throws Exception {
        TestBean championship = getTestBean(rid);
        if (championship==null)
            throw new NoContentException(rid);
        championship = getConnection().detach(championship);
        deleteBean(rid);
        return championship;
    }

}
