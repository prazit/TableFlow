package com.tflow.data;

import com.tflow.UTBase;
import com.tflow.model.data.query.ColumnType;
import com.tflow.model.data.query.QueryFilterConnector;
import com.tflow.model.data.query.QueryFilterOperation;
import jdk.nashorn.internal.runtime.regexp.RegExp;
import jdk.nashorn.internal.runtime.regexp.RegExpFactory;
import jdk.nashorn.internal.runtime.regexp.RegExpMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLSelectUT extends UTBase {

    private Logger log;

    @BeforeEach
    public void startup() {
        log = LoggerFactory.getLogger(getClass());
    }


    private void printArray(String label, String[] array) {
        println(label);
        indent(1);
        for (String item : array) {
            println("'" + item + "'");
        }
        indent(-1);
    }

    @Test
    public void splitSQL() {
        /*String sql = "" +
                "    select " +
                "           tableA.a," +
                "           tableB.b as b," +
                "           tableC.c + ( tableA.c - tableC.a ) as c," +
                "           tableA.d as d," +
                "           tableA.e + ( tableA.f - tableA.g )," +
                "           tableE.f," +
                "           tableH.*," +
                "           tableI.*" +
                "from tableA, tableH, tableI" +
                "   inner join tableB B on B.b = SUBSTR(TRIM(tableA.b), 0, 7) " +
                "   left join tableC on (tableC.c = B.c) " +
                "   left outer join tableF on (tableF.f = tableA.f) " +
                "   right join  tableD on tableD.d = tableB.d " +
                "   right outer  join tableG on (tableG.g = tableA.g) " +
                "   full outer join tableE on (tableE.e = tableB.e) " +
                "   outer join tableJ on (tableJ.j = tableE.j) " +
                "   " +
                "where    tableA.x <> 0 " +
                "   and (tableC.c <= tableB.c OR tableC.c => tableB.c)" +
                "   and tableB.y = 0" +
                "   and tableG.y > 0" +
                "   and tableF.y < 10000" +
                "   and tableE.i not in ('a','b','c')" +
                "   and tableF.a in ('a','b','c')" +
                "   and tableC.d like '%test%'" +
                "   and tableD.d not like '%test%'" +
                "   and tableA.d != 'd'" +
                "   and tableA.f is null" +
                "   and tableB.f is not null" +
                "order    by tableA.a, b";*/

        String sql = "Select\n" +
                /*"    'BG'                                    as schm_type,\n" +
                "    BGM.BG_TYPE                             as schm_code,\n" +
                "    ascii(substr(BGM.BG_SRL_NUM, 1, 1))\n" +
                "        || ascii(substr(BGM.BG_SRL_NUM, 2, 1))\n" +
                "        || ascii(substr(BGM.BG_SRL_NUM, 3, 1))\n" +
                "        || substr(BGM.BG_SRL_NUM, 4, 77)    as deal_id,\n" +
                "    '00000000'                              as first_payment_date,\n" +
                "    to_char(bgm.bg_expiry_date, 'YYYYMMDD') as next_payment_date,\n" +
                "    to_char(bgm.bg_expiry_date, 'YYYYMMDD') as next_interest_date,\n" +
                "    0.00                                    as outstanding_accrue_interest,\n" +
                "    ' '                                     as revolving_nonrevolving_flag,\n" +
                "    some(substr(BGM.BG_SRL_NUM, 1, 1))\n" +
                "        || some(substr(BGM.BG_SRL_NUM, 2, 1))\n" +
                "        || some(substr(BGM.BG_SRL_NUM, 3, 1))\n" +
                "        || substr(BGM.BG_SRL_NUM, 4, 77),\n" +
                "\n" +*/
                "'BG' as schm_type, BGM.BG_TYPE as schm_code, ascii(substr(BGM.BG_SRL_NUM, 1, 1)) || ascii(substr(BGM.BG_SRL_NUM, 2, 1)) || ascii(substr(BGM.BG_SRL_NUM, 3, 1)) || substr(BGM.BG_SRL_NUM, 4, 77) , '00000000' as first_payment_date, to_char(bgm.bg_expiry_date, 'YYYYMMDD') as next_payment_date, to_char(bgm.bg_expiry_date, 'YYYYMMDD') as next_interest_date, 0.00 as outstanding_accrue_interest, ' ' as revolving_nonrevolving_flag" +
                "from tbaadm.bgm\n" +
                "         left join tbaadm.bgp on (bgp.bg_type = bgm.bg_type)\n" +
                "         left join tbaadm.sol on (sol.sol_id = 792)\n" +
                "\n" +
                "where bgm.entity_cre_flg = 'Y'\n" +
                "  and bgp.cont_liab_tran_flg = 'Y'\n" +
                "\n" +
                "  and (  bgm.close_date is null\n" +
                "    or ( bgm.close_date is not null\n" +
                "                       )\n" +
                "    )\n" +
                "\n" +
                "order by\n" +
                "    schm_type,\n" +
                "    schm_code,\n" +
                "    deal_id,\n" +
                "    first_payment_date,\n" +
                "    next_payment_date,\n" +
                "    next_interest_date,\n" +
                "    outstanding_accrue_interest,\n" +
                "    revolving_nonrevolving_flag,\n" +
                "    SOL.SOL_ID,\n" +
                "    BGM.BG_SRL_NUM\n";

        sql = sql.replaceAll("[\\s]+", " ");
        StringBuilder select = new StringBuilder();
        StringBuilder from = new StringBuilder();
        StringBuilder where = new StringBuilder();
        StringBuilder orderBy = new StringBuilder();
        splitSQLPart(sql, select, from, where, orderBy);
        log.debug("AddQuery: select = {}", select);
        log.debug("AddQuery: from = {}", from);
        log.debug("AddQuery: where = {}", where);
        log.debug("AddQuery: orderBy = {}", orderBy);

        String[] selectArray = splitColumn(select.toString());
        String[] fromArray = splitBy(from.toString(), "([Ff][Uu][Ll][Ll] |[Ll][Ee][Ff][Tt] |[Rr][Ii][Gg][Hh][Tt] )*([Ii][Nn][Nn][Ee][Rr] |[Oo][Uu][Tt][Ee][Rr] )*([Jj][Oo][Ii][Nn])");
        String[] whereArray = splitBy(where.toString(), "[Aa][Nn][Dd]|[Oo][Rr]");
        String[] orderByArray = splitColumn(orderBy.toString());

        printArray("Select-Array:", selectArray);
        printArray("From-Array:", fromArray);
        printArray("Where-Array:", whereArray);
        printArray("OrderBy-Array:", orderByArray);

        /*select => columnList*/
        List<String> selectedColumnList = new ArrayList<>();
        addColumnTo(selectedColumnList, selectArray);
        println("SelectedColumnList: {}", Arrays.toString(selectedColumnList.toArray()));

        /*from => tableList*/
        //List<QueryTable> tableList = query.getTableList();
        List<String> tableList = new ArrayList<>();
        addTableTo(tableList, fromArray);
        println("TableList: {}", Arrays.toString(tableList.toArray()));

        /*TODO: where => filterList*/
        List<String> filterList = new ArrayList<>();
        addFilterTo(filterList, whereArray);
        println("FilterList: {}", Arrays.toString(filterList.toArray()));

    }

    private void addFilterTo(List<String> filterList, String[] whereArray) {
        /*first condition need connector*/
        whereArray[0] = "AND " + whereArray[0];
        StringBuilder operation;
        String connector;
        int operationIndex;
        for (String where : whereArray) {
            operation = new StringBuilder();
            connector = where.substring(0, 3).trim().toUpperCase();
            operationIndex = findOperation(where, operation);
            String queryFilter = "{connector: " + connector + ", " +
                    "leftValue: " + where.substring(3, operationIndex).trim() + ", " +
                    "operation: " + operation + ", " +
                    "rightValue: " + where.substring(operationIndex + operation.length()).trim() + "}";
            filterList.add(queryFilter);
        }
    }

    private int findOperation(String where, StringBuilder operation) {
        char[] one = {'=', '>', '<', '!'};
        char[] second = {'S', 'N'};

        where = where.toUpperCase();
        char[] chars = where.toCharArray();
        char ch;
        char next;
        int operLength = 0;
        int operStart = 0;
        for (int index = 0; index < chars.length; index++) {
            ch = chars[index];
            if (match(ch, one)) {
                /*[ =, >, <, <>, !=, >=, <= ]*/
                next = chars[index + 1];
                operLength = (next == '=' || next == '>') ? 2 : 1;
                operStart = index;
                break;

            } else if (ch == 'I') {
                /*[ IS, IN, IS NOT ]*/
                next = chars[index + 1];
                if (match(next, second)) {
                    next = chars[index + 2];
                    if (next == ' ') {
                        if (where.substring(index, index + 6).equals("IS NOT")) {
                            operLength = 6;
                            operStart = index;
                            break;
                        } else {
                            /*[ IS, IN ]*/
                            operLength = 2;
                            operStart = index;
                            break;
                        }
                    }
                }

            } else if (ch == 'N') {
                if (where.substring(index, index + 6).equals("NOT IN")) {
                    operLength = 6;
                    operStart = index;
                    break;
                } else if (where.substring(index, index + 8).equals("NOT LIKE")) {
                    operLength = 8;
                    operStart = index;
                    break;
                }

            } else if (ch == 'L') {
                if (where.substring(index, index + 4).equals("LIKE")) {
                    operLength = 4;
                    operStart = index;
                    break;
                }
            }
        } // end of for

        operation.append(where, operStart, operStart + operLength);
        println("findOperation: operStart: {}, operLength: {}, operation: {}, where: {}", operStart, operLength, operation, where);
        return operStart;
    }

    private boolean match(char ch, char[] chars) {
        for (char aChar : chars) {
            if (ch == aChar) {
                return true;
            }
        }
        return false;
    }

    private int findOperationSimple(String where, StringBuilder operation) {
        String replacement = "__OPERATION__";
        String finder = where.replaceAll("[<>=]", replacement);
        boolean singleChar = finder.length() > where.length();
        if (!singleChar) finder = where.replaceAll("[<!][>=]", replacement);
        int index = finder.indexOf(replacement);
        operation.append(where, index, index + (singleChar ? 1 : 2));
        return index;
    }

    private void addTableTo(List<String> tableList, String[] fromArray) {
        for (String table : fromArray) {
            println("From-Component: '{}'", table);
            indent();
            StringBuilder tableName = new StringBuilder();
            StringBuilder tableAlias = new StringBuilder();
            StringBuilder tableJoinType = new StringBuilder();
            StringBuilder joinedTableName = new StringBuilder();
            StringBuilder joinCondition = new StringBuilder();

            String[] words = table.trim().split("[,][ ]|[ ,()=]");
            printArray("words", words);

            String upperCase = words[0].toUpperCase();
            if (upperCase.isEmpty() || !"INNER|LEFT|RIGHT|FULL|OUTER".contains(upperCase)) {
                println("Without JOIN:");
                indent();
                for (String word : words) {
                    tableList.add(word);
                    println("tableName: {}", word);
                }
                indent(-1);
            } else {
                splitTableWithJoin(table, words, tableName, tableAlias, tableJoinType, joinedTableName, joinCondition);
                tableList.add(tableName.toString());
                println("With JOIN:");
                indent();
                println("tableName: {}", tableName);
                println("tableAlias: {}", tableAlias);
                println("tableJoinType: {}", tableJoinType);
                println("joinedTableName: {}", joinedTableName);
                println("joinCondition: {}", joinCondition);
                indent(-1);
            }
            indent(-1);
        }
        tableList.sort(Comparator.comparing(String::toUpperCase));
    }

    private void addColumnTo(List<String> selectedColumnList, String[] selectArray) {
        StringBuilder queryColumn;
        ColumnType type;
        String[] values;
        String name;
        String value;
        String uppercase;
        String normalNamePattern = "([.]*[A-Z_*][A-Z0-9_]+)+";
        int compute = 0;
        int index = 0;
        for (String column : selectArray) {
            uppercase = column.toUpperCase();
            if (uppercase.replaceAll("\\s*[,]*\\s*" + normalNamePattern + "(\\s+AS\\s+[A-Z0-9_]+\\s*|\\s*)", "").isEmpty()) {
                if (Pattern.compile("[\\s]AS[\\s]").matcher(uppercase).find()) {
                    type = ColumnType.ALIAS;
                    values = column.split("[\\s][Aa][Ss][\\s]");
                    name = values[1];
                    value = values[0].startsWith(",") ? values[0].substring(1) : values[0];
                } else {
                    type = ColumnType.NORMAL;
                    values = column.split("[.]");
                    name = values[1];
                    value = column.startsWith(",") ? column.substring(1) : column;
                }
            } else if (Pattern.compile("[\\s]AS[\\s]").matcher(uppercase).find()) {
                type = ColumnType.COMPUTE;
                values = column.split("[\\s][Aa][Ss][\\s]");
                name = values[1];
                value = values[0].startsWith(",") ? values[0].substring(1) : values[0];
            } else {
                type = ColumnType.COMPUTE;
                name = "COMPUTE" + (++compute);
                value = column.startsWith(",") ? column.substring(1) : column;
            }

            queryColumn = new StringBuilder(); //new QueryColumn(index++, ProjectUtil.newUniqueId(project), name, null);
            queryColumn.append("{")
                    .append("index:").append(index++).append(", ")
                    .append("id:").append("[new-id]").append(", ")
                    .append("type:").append(type).append(", ")
                    .append("name:").append(name).append(", ")
                    .append("value:").append(value)
                    .append("}")
            ;
            selectedColumnList.add(queryColumn.toString());
        }
    }

    private void splitTableWithJoin(String table, String[] words, StringBuilder tableName, StringBuilder tableAlias, StringBuilder tableJoinType, StringBuilder joinedTableName, StringBuilder joinCondition) {
        /*this is one of JOIN Type and condition always appear after word 'ON'*/
        joinCondition.append(table.split("[Oo][Nn]")[1]);

        /*find JOIN Type and Table Name*/
        String upperCase;
        int wordCount = words.length;
        int next = 0;
        tableJoinType.append(words[0].toUpperCase());
        for (int i = 1; i < wordCount; i++) {
            upperCase = words[i].toUpperCase();
            tableJoinType.append("_").append(upperCase);
            if ("JOIN".equals(upperCase)) {
                tableName.append(words[i + 1]);
                if ("ON".equals(words[i + 2].toUpperCase())) {
                    tableAlias.append(words[i + 1]);
                } else {
                    tableAlias.append(words[i + 2]);
                }
                next = i + 3;
                break;
            }
        }

        /*find Joined Table Name*/
        String tableNameString = tableName.toString();
        String tableAliasString = tableAlias.toString();
        for (int i = next; i < wordCount; i++) {
            if (words[i].contains(".")) {
                /*this is table-name.column-name*/
                String[] tableColumn = words[i].split("[.]");
                if (!tableColumn[0].equalsIgnoreCase(tableNameString) && !tableColumn[0].equalsIgnoreCase(tableAliasString)) {
                    joinedTableName.append(tableColumn[0]);
                    break;
                }
            }
        }
    }

    private String[] splitBy(String source, String splitters) {
        String splitter = "__SPLITTER__";
        source = source.replaceAll("(" + splitters + ")", splitter + "$1");
        return source.split(splitter);
    }

    private String[] splitColumn(String source) {
        /*split compute-column need different way (Remember: compute-column always need alias, need alert at the Extract process)*/
        List<String> columnList = new ArrayList<>();
        String replacedWord = "RE__PLACED__";

        /*collect all Quoted*/
        String quotedPattern = "[']([^']+)[']|[\\\"]([^\\\"]+)[\\\"]|[\\(]([^\\(\\)]+)[\\)]";
        LinkedHashMap<String, String> quotedMap = new LinkedHashMap();
        int count = 0;
        String replaced = source;
        while (true) {
            String key = replacedWord + (++count);
            String before = replaced;
            int lengthBefore = before.length();
            replaced = before.replaceFirst(quotedPattern, key);
            if (replaced.length() != lengthBefore) {
                int index = replaced.indexOf(key);
                String value = before.substring(index, index + (lengthBefore - replaced.length() + key.length()));
                quotedMap.put(key, value);
            } else {
                break;
            }
        }
        //println("source: {}", source);
        //println("replaced: {}", replaced);
        //println("quotedMap: {}", quotedMap);

        /*selector: (operator) (constant|name)*/
        String selector = "((\\s*[\\+\\-\\*\\/]|\\s*[\\|\\&]{2})*((\\s*['].*[']|\\s*[\"].*[\"]|\\s*[0-9]+([.][0-9]+)?)|(\\s*[A-Za-z][A-Za-z0-9_]*[.][A-Za-z][A-Za-z0-9_]*|\\s*[A-Za-z][A-Za-z0-9_]*)))+";
        String item = firstItem(replaced, selector);
        while (item != null) {
            replaced = replaced.replaceFirst(selector, "");
            columnList.add(item);
            item = firstItem(replaced, selector);
        }
        //printArray("columnList", columnList.toArray(new String[columnList.size()]));

        /*restore all value by replace all key*/
        String[] result = new String[columnList.size()];
        int index = 0;
        selector = replacedWord + "[0-9]+";
        String value;
        String key;
        for (String column : columnList) {
            indent();
            key = firstItem(column, selector);
            while (key != null) {
                value = quotedMap.get(key);
                column = column.replaceFirst(selector, value);
                key = firstItem(column, selector);
            }
            result[index++] = column;
            indent(-1);
        }

        //printArray("result", result);
        return result;
    }

    /**
     * find item in source by selector
     *
     * @return first item when found, otherwise return null
     */
    private String firstItem(String source, String selector) {
        String finder = "F__I__N__D__E__R";
        int lengthBefore = source.length();
        String replaced = source.replaceFirst(selector, finder);
        if (replaced.length() != lengthBefore) {
            int index = replaced.indexOf(finder);
            return source.substring(index, index + (lengthBefore - replaced.length() + finder.length()));
        }
        return null;
    }

    private void splitSQLPart(String sql, StringBuilder select, StringBuilder from, StringBuilder where, StringBuilder orderBy) {
        String original = sql;
        sql = original.toUpperCase();

        /*indexes*/
        int selectIndex = sql.indexOf("SELECT");
        int fromIndex = sql.indexOf("FROM");
        int whereIndex = sql.indexOf("WHERE");
        int orderIndex = sql.indexOf("ORDER BY");
        println("selectIndex = {}", selectIndex);
        println("fromIndex = {}", fromIndex);
        println("whereIndex = {}", whereIndex);
        println("orderIndex = {}", orderIndex);

        /*select*/
        select.append(original.substring(selectIndex + 6, fromIndex));

        /*from*/
        int endIndex = whereIndex > 0 ? whereIndex : (orderIndex > 0 ? orderIndex : -1);
        from.append(endIndex > 0 ? original.substring(fromIndex + 4, endIndex) : original.substring(fromIndex + 4));

        /*where*/
        if (whereIndex > 0) {
            where.append((orderIndex > 0) ? original.substring(whereIndex + 5, orderIndex) : original.substring(whereIndex + 5));
        }

        /*orderBy*/
        orderBy.append((orderIndex > 0) ? original.substring(orderIndex + 8) : "");

        /*TODO: future feature: need support GROUP and HAVING*/
    }

}
