package com.tflow.data;

import com.tflow.UTBase;
import com.tflow.kafka.EnvironmentConfigs;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.*;
import com.tflow.system.Environment;
import com.tflow.system.Properties;
import com.tflow.zookeeper.ZKConfigNode;
import com.tflow.zookeeper.ZKConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataManagerUT extends UTBase {

    Properties configs;
    ZKConfiguration zkConfiguration;
    Environment environment;
    EnvironmentConfigs environmentConfigs;
    DataManager dataManager;
    ProjectUser projectUser;

    @BeforeEach
    public void setup() {

        configs = new Properties();
        configs.put("consumer.bootstrap.servers", "DESKTOP-K1PAMA3:9092");
        configs.put("consumer.group.id", "tflow");
        configs.put("consumer.enable.auto.commit", "true");
        configs.put("consumer.auto.commit.interval.ms", "1000");
        configs.put("consumer.session.timeout.ms", "30000");

        configs.put("producer.bootstrap.servers", "DESKTOP-K1PAMA3:9092");
        configs.put("producer.acks", "all");
        configs.put("producer.retries", "0");
        configs.put("producer.batch.size", "16384");
        configs.put("producer.linger.ms", "1");
        configs.put("producer.buffer.memory", "33554432");

        configs.put("zookeeper.host", "localhost:2181");
        configs.put("zookeeper.connect.timeout.second", "15");
        configs.put("zookeeper.session.timeout.ms", "18000");

        try {
            zkConfiguration = new ZKConfiguration(configs);
            environment = Environment.valueOf(zkConfiguration.getString(ZKConfigNode.ENVIRONMENT));
            environmentConfigs = EnvironmentConfigs.valueOf(environment.name());
            dataManager = new DataManager(environment, "initial", zkConfiguration);
        } catch (Exception ex) {
            println("ERROR: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(-1);
        }

        projectUser = new ProjectUser();
        projectUser.setId("P0");
        projectUser.setUserId(0);
        projectUser.setClientId(0);

    }

    @Test
    public void groupListData() {

        GroupData groupData = new GroupData(0, "Default Group", new ArrayList<>(Arrays.asList(
                new ProjectItemData("P0", "#0"),
                new ProjectItemData("P1", "#1")
        )));
        dataManager.addData(ProjectFileType.GROUP, groupData, projectUser, groupData.getId());

        groupData = new GroupData(1, "Trade Finance", new ArrayList<>(Arrays.asList(
                new ProjectItemData("P2", "#2"),
                new ProjectItemData("P3", "#3")
        )));
        dataManager.addData(ProjectFileType.GROUP, groupData, projectUser, groupData.getId());

        groupData = new GroupData(2, "Treasury", new ArrayList<>(Arrays.asList(
                new ProjectItemData("P4", "#4"),
                new ProjectItemData("P5", "#5")
        )));
        dataManager.addData(ProjectFileType.GROUP, groupData, projectUser, groupData.getId());

        groupData = new GroupData(3, "Data Warehouse", new ArrayList<>(Arrays.asList(
                new ProjectItemData("P6", "#6"),
                new ProjectItemData("P7", "#7")
        )));
        dataManager.addData(ProjectFileType.GROUP, groupData, projectUser, groupData.getId());

        groupData = new GroupData(environmentConfigs.getTemplateGroupId(), "Project Template", new ArrayList<>(Arrays.asList(
                new ProjectItemData("T1", "Template No.1"),
                new ProjectItemData("T2", "Template No.2")
        )));
        dataManager.addData(ProjectFileType.GROUP, groupData, projectUser, groupData.getId());

        List<GroupItemData> groupList = new ArrayList<>();
        groupList.add(new GroupItemData(0, "Default Group"));
        groupList.add(new GroupItemData(1, "Trade Finance"));
        groupList.add(new GroupItemData(2, "Treasury"));
        groupList.add(new GroupItemData(3, "Data Warehouse"));

        GroupListData groupListData = new GroupListData();
        groupListData.setGroupList(groupList);
        groupListData.setLastProjectId(7);
        dataManager.addData(ProjectFileType.GROUP_LIST, groupListData, projectUser);

        dataManager.waitAllTasks();
    }

}
