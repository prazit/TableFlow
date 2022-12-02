package com.tflow.model.editor.cmd;

import com.tflow.model.editor.sql.Query;
import com.tflow.model.editor.sql.QueryTable;

import java.util.List;
import java.util.Map;

public class RearrangeQueryTable extends Command {
    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        Boolean enabled = (Boolean) paramMap.get(CommandParamKey.SWITCH_ON);
        if(!enabled) return;

        Query query = (Query) paramMap.get(CommandParamKey.QUERY);

        /*TODO: Rearrange all tables in the Query*/
        List<QueryTable> tableList = query.getTableList();

    }
}
