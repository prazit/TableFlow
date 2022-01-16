package com.tflow.model.editor.view;

import com.tflow.model.editor.PropertyType;

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

    @Override
    public String toString() {
        return "PropertyView{" +
                "type=" + type +
                ", label='" + label + '\'' +
                ", var='" + var + '\'' +
                ", varParent='" + varParent + '\'' +
                ", params=" + Arrays.toString(params) +
                ", update='" + update + '\'' +
                ", javaScript='" + javaScript + '\'' +
                '}';
    }
}
