package com.tflow.model.editor.view;

import com.tflow.model.editor.PropertyType;
import com.tflow.model.editor.PropertyVar;

import java.util.Arrays;

/**
 * Store attributes of property.
 */
public class PropertyView {

    private PropertyType type;
    private String label;
    private String var;
    private String varParent;

    private String[] params;

    private String update;
    private String javaScript;

    private Object oldValue;
    private Object newValue;

    public PropertyView() {
        update = "@this";
        javaScript = "";
    }

    public boolean hasParent() {
        return varParent != null;
    }

    public PropertyType getType() {
        return type;
    }

    public void setType(PropertyType type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getVar() {
        return var;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public String getVarParent() {
        return varParent;
    }

    public void setVarParent(String varParent) {
        this.varParent = varParent;
    }

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }

    public int paramCount() {
        return params.length;
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    public String getJavaScript() {
        return javaScript;
    }

    public void setJavaScript(String javaScript) {
        this.javaScript = javaScript;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public void setOldValue(Object oldValue) {
        this.oldValue = oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    public void setNewValue(Object newValue) {
        this.newValue = newValue;
    }

    @Override
    public String toString() {
        return "{" +
                "type:" + type +
                ", label:'" + label + '\'' +
                ", var:'" + var + '\'' +
                ", varParent:'" + varParent + '\'' +
                ", update:'" + update + '\'' +
                ", javaScript:'" + javaScript + '\'' +
                ", params:" + Arrays.toString(params) +
                ", oldValue:" + oldValue +
                ", newValue:" + newValue +
                '}';
    }
}
