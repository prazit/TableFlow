package com.tflow.controller;

import com.tflow.model.editor.Workspace;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.TestAction;
import com.tflow.system.constant.Theme;
import com.tflow.util.DateTimeUtil;
import com.tflow.util.FacesUtil;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ViewScoped
@Named("editorCtl")
public class EditorController extends Controller {

    @Inject
    private Workspace workspace;

    @PostConstruct
    public void onCreation() {

    }

    public void lightTheme() {
        workspace.getUser().setTheme(Theme.LIGHT);
        FacesUtil.redirect("/editor.xhtml");
    }

    public void darkTheme() {
        workspace.getUser().setTheme(Theme.DARK);
        FacesUtil.redirect("/editor.xhtml");
    }

    public void testReadSerialize() {
        List<Action> actionList = null;
        try {
            FileInputStream fileIn = new FileInputStream("/Apps/TFlow/TestAction.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            actionList = (List<Action>) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException c) {
            System.out.println("List<ActionBase> class not found");
            c.printStackTrace();
        }

        for (Action actionBase : actionList) {
            log.info("action = {}", actionBase.toString());
        }
    }

    public void testWriteSerialize() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("param1", "String");
        paramMap.put("param2", "Integer");
        paramMap.put("param3", "Date");
        paramMap.put("param4", "Decimal");
        paramMap.put("param11", "Text");
        paramMap.put("param12", 35000);
        paramMap.put("param13", DateTimeUtil.now());
        paramMap.put("param14", 35000.00053);

        List<Action> actionList = new ArrayList<>();
        actionList.add(new TestAction(paramMap));

        try {
            FileOutputStream fileOut = new FileOutputStream("/Apps/TFlow/TestAction.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(actionList);
            out.close();
            fileOut.close();
            log.info("Serialized data is saved in /Apps/TFlow/TestAction.ser");
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

}
