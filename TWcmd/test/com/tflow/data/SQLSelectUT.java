package com.tflow.data;

import com.tflow.UTBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class SQLSelectUT extends UTBase {

    private Logger log;

    @BeforeEach
    public void startup() {
        log = LoggerFactory.getLogger(getClass());
    }

    @Test
    public void splitSQL() {
        String sql = "" +
                "    select tableA.a,tableB.b alias b,tableC.c,tableA.d,tableA.e " +
                "   from tableA, tableB, tableC " +
                "   where    tableA.x = 0 " +
                "   order    by tableA.a, b";
        StringBuilder select = new StringBuilder();
        StringBuilder from = new StringBuilder();
        StringBuilder where = new StringBuilder();
        StringBuilder orderBy = new StringBuilder();
        splitSQLPart(sql, select, from, where, orderBy);
        log.debug("AddQuery: select = {}", select);
        log.debug("AddQuery: from = {}", from);
        log.debug("AddQuery: where = {}", where);
        log.debug("AddQuery: orderBy = {}", orderBy);
    }

    private void splitSQLPart(String sql, StringBuilder select, StringBuilder from, StringBuilder where, StringBuilder orderBy) {
        sql = sql.toUpperCase().replaceAll("[\\s]+", " ");

        /*indexes*/
        int selectIndex = sql.indexOf("SELECT");
        int fromIndex = sql.indexOf("FROM");
        int whereIndex = sql.indexOf("WHERE");
        int orderIndex = sql.indexOf("ORDER BY");
        println("selectIndex = {}", selectIndex);
        println("fromIndex = {}", fromIndex);
        println("whereIndex = {}", whereIndex);
        println("orderIndex = {}", orderIndex);

        select.append(sql.substring(selectIndex + 6, fromIndex));

        int endIndex = whereIndex > 0 ? whereIndex : (orderIndex > 0 ? orderIndex : -1);
        from.append(endIndex > 0 ? sql.substring(fromIndex + 4, endIndex) : sql.substring(fromIndex + 4));

        if (whereIndex > 0) {
            where.append((orderIndex > 0) ? sql.substring(whereIndex + 5, orderIndex) : sql.substring(whereIndex + 5));
        }

        orderBy.append((orderIndex > 0) ? sql.substring(orderIndex + 8) : "");

        /*TODO: future feature: need support GROUP and HAVING*/
    }

}
