package com.tflow.thread;

import com.tflow.UTBase;
import com.tflow.util.DateTimeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockingQueueUT extends UTBase {

    BlockingQueue blockingQueue;
    int id;

    void printThreads() {
        printThreads(null);
    }

    void printThreads(String string) {
        List<String> nameList = new ArrayList<>();
        Map<Thread, StackTraceElement[]> threadMap = Thread.getAllStackTraces();
        for (Thread thread : threadMap.keySet()) {
            nameList.add(thread.getName());
        }
        nameList.sort(String::compareTo);
        println("Threads:" + (string == null ? " " : string + ": ") + Arrays.toString(nameList.toArray()));
    }

    @BeforeEach
    void setUp() {
        blockingQueue = new ArrayBlockingQueue(2);
        id = 0;
    }

    public void put() {
        String msg = "Command #" + (++id);
        println("PUT: " + msg);

        try {
            blockingQueue.put(msg);
        } catch (InterruptedException ex) {
            println("InterruptedException on PUT: " + ex.getMessage());
        }
    }

    public void take() {
        Thread thread = new Thread("TAKE") {
            @Override
            public void run() {
                String msg = null;
                try {
                    msg = (String) blockingQueue.take();
                } catch (InterruptedException ex) {
                    println("InterruptedException on TAKE: " + ex.getMessage());
                }
                println("TAKE: " + msg);
            }
        };
        thread.start();
    }

    public void poll() {
        String msg = (String) blockingQueue.poll();
        println("POLL: " + msg);
    }

    @Test
    public void test() {
        synchronized (Thread.currentThread()) {
            println("-- blocking queue --");
            indent();
        }

        printThreads();
        take();
        take();
        printThreads("after take-1");
        put();
        put();

        printThreads("before take-2");
        take();
        printThreads("after take-2");

        put();

        synchronized (Thread.currentThread()) {
            indent(-1);
            println("-- ended --");
        }
    }
}
