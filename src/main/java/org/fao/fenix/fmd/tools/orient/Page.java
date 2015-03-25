package org.fao.fenix.fmd.tools.orient;

import javax.servlet.ServletRequest;

public class Page {
    public int skip = 0;
    public int length = -1;

    public int page = 1;
    public int perPage = -1;
    public int pages = 1;

    public Page(ServletRequest request) {
        String page = request.getParameter("page");
        String perPage = request.getParameter("per_page");
        String pages = request.getParameter("total_pages");

        init(page!=null?new Integer(page):null, perPage!=null?new Integer(perPage):null, pages!=null?new Integer(pages):null);
    }

    public Page(int skip, int length) {
        this.skip = skip;
        this.length = length;
    }

    public Page(Integer page, Integer perPage, Integer pages) {
        init(page,perPage,pages);
    }



    private void init(Integer page, Integer perPage, Integer pages) {
        if (perPage!=null && perPage>0) {
            this.perPage = perPage;
            this.page = page = page != null && page > 0 ? page : 1;
            this.pages = pages = pages != null && pages > 0 ? pages : 1;

            length = perPage*pages;
            skip = (page-1)*perPage;
        }
    }

    public String toSQL() {
        return perPage>0 ? " skip "+skip+" limit "+length : "";
    }
}
