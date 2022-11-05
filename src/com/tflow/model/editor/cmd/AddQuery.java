package com.tflow.model.editor.cmd;

import com.tflow.model.editor.BinaryFile;
import com.tflow.model.editor.DataFile;
import com.tflow.model.editor.Project;
import org.apache.kafka.common.protocol.types.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AddQuery extends Command {

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        Logger log = LoggerFactory.getLogger(getClass());

        Project project = (Project) paramMap.get(CommandParamKey.PROJECT);
        DataFile dataFile = (DataFile) paramMap.get(CommandParamKey.DATA_FILE);
        BinaryFile sqlFile = (BinaryFile) paramMap.get(CommandParamKey.BINARY_FILE);

        /* assume sql is simple select (no nested) */
        String sql = new String(sqlFile.getContent(), StandardCharsets.ISO_8859_1);
        StringBuilder select = new StringBuilder();
        StringBuilder from = new StringBuilder();
        StringBuilder where = new StringBuilder();
        StringBuilder orderBy = new StringBuilder();
        splitSQLPart(sql, select, from, where, orderBy);
        log.debug("AddQuery: select = {}", select);
        log.debug("AddQuery: from = {}", from);
        log.debug("AddQuery: where = {}", where);
        log.debug("AddQuery: orderBy = {}", where);

        /*TODO: select => columnList*/


        /*Notice: all compute-column need alias, normal-column use column-name as alias*/
        /*TODO: need to support compute-column (procedure call with parameters separated by comma)*/
        /*TODO: from => tableList*/
        /*TODO: where => filterList*/
        /*TODO: oder by => sortList*/


        // TODO: save Query Data

        // TODO: save DataFile

    }

    private void splitSQLPart(String sql, StringBuilder select, StringBuilder from, StringBuilder where, StringBuilder orderBy) {
        sql = sql.toUpperCase().replaceAll("[\\s]+", " ");

        /*indexes*/
        int selectIndex = sql.indexOf("SELECT");
        int fromIndex = sql.indexOf("FROM");
        int whereIndex = sql.indexOf("WHERE");
        int orderIndex = sql.indexOf("ORDER BY");

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
