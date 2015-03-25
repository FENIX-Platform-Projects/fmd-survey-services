package org.fao.fenix.fmd.tools.orient;

import javax.servlet.ServletRequest;
import java.util.*;

public class Order extends LinkedHashMap<String,Order.Sort> {
    enum Sort {
        ASC("ascendent","ascending","standard","default","asc","std","def","a"),DESC("descendent","descending","inverse","desc","d","alternative","alternate","opposite");
        private Set<String> names = new HashSet<>();
        private Sort(String ... names) {
            this.names.addAll(Arrays.asList(names));
        }

        static Sort getByName(String name) {
            if (name!=null && name.trim().length()>0) {
                name = name.toLowerCase();
                for (Sort sort : Sort.values())
                    if (sort.names.contains(name))
                        return sort;
            }
            return null;
        }
    }


    public Order(String ordering) {
        init(ordering);
    }
    public Order(ServletRequest request) {
        init(request.getParameter("sort_by"));
    }
    private void init(String orderingParameter) {
        if (orderingParameter!=null)
            for (String order : orderingParameter.split(",")){
                String[] orderEntry = order.split(":");
                put(orderEntry[0], orderEntry.length>1 ? Sort.getByName(orderEntry[1]) : Sort.ASC);
            }
    }


    public String toSQL() {
        StringBuilder buffer = new StringBuilder();
        for (Map.Entry<String,Order.Sort> orderEntry : entrySet())
            buffer.append(", ").append(orderEntry.getKey()).append(' ').append(orderEntry.getValue().name());
        return size()>0 ? " order by"+buffer.substring(1) : "";
    }
}
