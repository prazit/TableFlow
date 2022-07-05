package com.tflow.model.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IdListMapper {

    public List<Integer> map(Map<Integer, ? extends Object> map) {
        return new ArrayList<>(map.keySet());
    }

}
