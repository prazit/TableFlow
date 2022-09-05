# ISSUES

----

:heavy_check_mark: = completed

:x: = cancelled

:white_check_mark: = in progress

:zero: = not important (fix/not fix at the end of work)

| fixed              | booked           | removed          | issue                                                                                                                                                                       | remark                                                                                                                                                                                                                          |
|:------------------:|:----------------:|:----------------:| --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| :heavy_check_mark: | 2022-01-08       | 2022-01-11       | Data Structure: more problem can solve by object unique ID.<br /> need to add ID for all selectable object<br /> + DataFile<br /> + Column<br /> + ColumnFx<br /> + TableFx |                                                                                                                                                                                                                                 |
| :heavy_check_mark: | 2022-01-08       |                  | Need to search for TODO with marked ISSUE and fix them all.                                                                                                                 | :arrow_backward:                                                                                                                                                                                                                |
| :heavy_check_mark: | 2022-01-08       |                  | UI: Leader Lines: need to disable reposition when window deactivated/activated.                                                                                             |                                                                                                                                                                                                                                 |
| :zero:             | 2022-01-16       |                  | [**not important**] ScrollPanel Size is not responsive, may be need to use div tag instead with CSS Scrollbar (overflow)                                                    | see .horizontal-scroll-panel and .vertical-scroll-panel                                                                                                                                                                         |
| :zero:             | 2022-01-16       |                  | [**not important**] Need to move showPropertyList and showStepList variables from Step to Project.                                                                          |                                                                                                                                                                                                                                 |
| 2021-12-12         | 2021-12-11 13:17 | 2021-12-12       | UI: need to remove browser scroll bar both horizontal and vertical.                                                                                                         | find extra width 8px in the css-verse.                                                                                                                                                                                          |
| :heavy_check_mark: | 2021-12-14 14:30 | 2022-07-29 10:00 | Logger: lockback still not work correctly, no logs are printed out for debug.                                                                                               | learn about jboss exlusion                                                                                                                                                                                                      |
| :car: :car: :car:  | 2022-09-05       |                  | Kafka need to try Specific-Configs for Topics to disable CLEANER process                                                                                                    | Workaround for all topics except the project-write topic, project-write need all data stay alive that required by another instance to replay from the first message for backup-site.<br/>+ reset all logs in kafka-logs folder. |

## 

#### KAFKA BIG ISSUE on Standalone

> Cleaner process will always be down when the period (time period/size period) is end

```log
[2565-09-05 22:15:46,502] ERROR Error while deleting segments for project-data-0 in dir C:\Apps\kafka\logs (kafka.server.LogDirFailureChannel)
java.nio.file.FileSystemException: C:\Apps\kafka\logs\project-data-0\00000000000000000000.timeindex -> C:\Apps\kafka\logs\project-data-0\00000000000000000000.timeindex.deleted: The proce
ss cannot access the file because it is being used by another process.

        at java.base/sun.nio.fs.WindowsException.translateToIOException(WindowsException.java:92)
        at java.base/sun.nio.fs.WindowsException.rethrowAsIOException(WindowsException.java:103)
        at java.base/sun.nio.fs.WindowsFileCopy.move(WindowsFileCopy.java:395)
        at java.base/sun.nio.fs.WindowsFileSystemProvider.move(WindowsFileSystemProvider.java:288)
        at java.base/java.nio.file.Files.move(Files.java:1421)
        at org.apache.kafka.common.utils.Utils.atomicMoveWithFallback(Utils.java:934)
        at kafka.log.AbstractIndex.renameTo(AbstractIndex.scala:210)
        at kafka.log.LazyIndex$IndexValue.renameTo(LazyIndex.scala:155)
        at kafka.log.LazyIndex.$anonfun$renameTo$1(LazyIndex.scala:79)
        at kafka.log.LazyIndex.renameTo(LazyIndex.scala:79)
        at kafka.log.LogSegment.changeFileSuffixes(LogSegment.scala:493)
        at kafka.log.LocalLog$.$anonfun$deleteSegmentFiles$1(LocalLog.scala:940)
        at kafka.log.LocalLog$.$anonfun$deleteSegmentFiles$1$adapted(LocalLog.scala:940)
        at scala.collection.immutable.List.foreach(List.scala:333)
        at kafka.log.LocalLog$.deleteSegmentFiles(LocalLog.scala:940)
        at kafka.log.LocalLog.removeAndDeleteSegments(LocalLog.scala:310)
        at kafka.log.UnifiedLog.$anonfun$deleteSegments$2(UnifiedLog.scala:1358)
        at kafka.log.UnifiedLog.deleteSegments(UnifiedLog.scala:1707)
        at kafka.log.UnifiedLog.deleteRetentionMsBreachedSegments(UnifiedLog.scala:1342)
        at kafka.log.UnifiedLog.deleteOldSegments(UnifiedLog.scala:1377)
        at kafka.log.LogManager.$anonfun$cleanupLogs$3(LogManager.scala:1169)
        at kafka.log.LogManager.$anonfun$cleanupLogs$3$adapted(LogManager.scala:1166)
        at scala.collection.immutable.List.foreach(List.scala:333)
        at kafka.log.LogManager.cleanupLogs(LogManager.scala:1166)
        at kafka.log.LogManager.$anonfun$startupWithConfigOverrides$2(LogManager.scala:462)
        at kafka.utils.KafkaScheduler.$anonfun$schedule$2(KafkaScheduler.scala:116)
        at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:515)
        at java.base/java.util.concurrent.FutureTask.runAndReset(FutureTask.java:305)
        at java.base/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:305)
        at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128)
        at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628)
        at java.base/java.lang.Thread.run(Thread.java:834)
        Suppressed: java.nio.file.FileSystemException: C:\Apps\kafka\logs\project-data-0\00000000000000000000.timeindex -> C:\Apps\kafka\logs\project-data-0\00000000000000000000.timeinde
x.deleted: The process cannot access the file because it is being used by another process.

                at java.base/sun.nio.fs.WindowsException.translateToIOException(WindowsException.java:92)
                at java.base/sun.nio.fs.WindowsException.rethrowAsIOException(WindowsException.java:103)
                at java.base/sun.nio.fs.WindowsFileCopy.move(WindowsFileCopy.java:309)
                at java.base/sun.nio.fs.WindowsFileSystemProvider.move(WindowsFileSystemProvider.java:288)
                at java.base/java.nio.file.Files.move(Files.java:1421)
                at org.apache.kafka.common.utils.Utils.atomicMoveWithFallback(Utils.java:931)
                ... 26 more
[2565-09-05 22:15:46,505] WARN [ReplicaManager broker=0] Stopping serving replicas in dir C:\Apps\kafka\logs (kafka.server.ReplicaManager)
[2565-09-05 22:15:46,505] ERROR Uncaught exception in scheduled task 'kafka-log-retention' (kafka.utils.KafkaScheduler)
org.apache.kafka.common.errors.KafkaStorageException: Error while deleting segments for project-data-0 in dir C:\Apps\kafka\logs
Caused by: java.nio.file.FileSystemException: C:\Apps\kafka\logs\project-data-0\00000000000000000000.timeindex -> C:\Apps\kafka\logs\project-data-0\00000000000000000000.timeindex.deleted
: The process cannot access the file because it is being used by another process.

        at java.base/sun.nio.fs.WindowsException.translateToIOException(WindowsException.java:92)
        at java.base/sun.nio.fs.WindowsException.rethrowAsIOException(WindowsException.java:103)
        at java.base/sun.nio.fs.WindowsFileCopy.move(WindowsFileCopy.java:395)
        at java.base/sun.nio.fs.WindowsFileSystemProvider.move(WindowsFileSystemProvider.java:288)
        at java.base/java.nio.file.Files.move(Files.java:1421)
        at org.apache.kafka.common.utils.Utils.atomicMoveWithFallback(Utils.java:934)
        at kafka.log.AbstractIndex.renameTo(AbstractIndex.scala:210)
        at kafka.log.LazyIndex$IndexValue.renameTo(LazyIndex.scala:155)
        at kafka.log.LazyIndex.$anonfun$renameTo$1(LazyIndex.scala:79)
        at kafka.log.LazyIndex.renameTo(LazyIndex.scala:79)
        at kafka.log.LogSegment.changeFileSuffixes(LogSegment.scala:493)
        at kafka.log.LocalLog$.$anonfun$deleteSegmentFiles$1(LocalLog.scala:940)
        at kafka.log.LocalLog$.$anonfun$deleteSegmentFiles$1$adapted(LocalLog.scala:940)
        at scala.collection.immutable.List.foreach(List.scala:333)
        at kafka.log.LocalLog$.deleteSegmentFiles(LocalLog.scala:940)
        at kafka.log.LocalLog.removeAndDeleteSegments(LocalLog.scala:310)
        at kafka.log.UnifiedLog.$anonfun$deleteSegments$2(UnifiedLog.scala:1358)
        at kafka.log.UnifiedLog.deleteSegments(UnifiedLog.scala:1707)
        at kafka.log.UnifiedLog.deleteRetentionMsBreachedSegments(UnifiedLog.scala:1342)
        at kafka.log.UnifiedLog.deleteOldSegments(UnifiedLog.scala:1377)
        at kafka.log.LogManager.$anonfun$cleanupLogs$3(LogManager.scala:1169)
        at kafka.log.LogManager.$anonfun$cleanupLogs$3$adapted(LogManager.scala:1166)
        at scala.collection.immutable.List.foreach(List.scala:333)
        at kafka.log.LogManager.cleanupLogs(LogManager.scala:1166)
        at kafka.log.LogManager.$anonfun$startupWithConfigOverrides$2(LogManager.scala:462)
        at kafka.utils.KafkaScheduler.$anonfun$schedule$2(KafkaScheduler.scala:116)
        at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:515)
        at java.base/java.util.concurrent.FutureTask.runAndReset(FutureTask.java:305)
        at java.base/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:305)
        at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128)
        at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628)
        at java.base/java.lang.Thread.run(Thread.java:834)
        Suppressed: java.nio.file.FileSystemException: C:\Apps\kafka\logs\project-data-0\00000000000000000000.timeindex -> C:\Apps\kafka\logs\project-data-0\00000000000000000000.timeinde
x.deleted: The process cannot access the file because it is being used by another process.

                at java.base/sun.nio.fs.WindowsException.translateToIOException(WindowsException.java:92)
                at java.base/sun.nio.fs.WindowsException.rethrowAsIOException(WindowsException.java:103)
                at java.base/sun.nio.fs.WindowsFileCopy.move(WindowsFileCopy.java:309)
                at java.base/sun.nio.fs.WindowsFileSystemProvider.move(WindowsFileSystemProvider.java:288)
                at java.base/java.nio.file.Files.move(Files.java:1421)
                at org.apache.kafka.common.utils.Utils.atomicMoveWithFallback(Utils.java:931)
                ... 26 more
[2565-09-05 22:15:46,507] INFO [ReplicaFetcherManager on broker 0] Removed fetcher for partitions HashSet(__consumer_offsets-22, __consumer_offsets-30, project-write-0, __consumer_offset
s-35, __consumer_offsets-37, project-read-0, __consumer_offsets-38, __consumer_offsets-13, __consumer_offsets-8, __consumer_offsets-21, __consumer_offsets-4, __consumer_offsets-27, __con
sumer_offsets-7, analytics-0, __consumer_offsets-9, __consumer_offsets-46, quickstart-events-0, build-0, __consumer_offsets-25, __consumer_offsets-41, __consumer_offsets-33, __consumer_o
ffsets-23, __consumer_offsets-49, project-build-0, __consumer_offsets-47, __consumer_offsets-16, __consumer_offsets-28, __consumer_offsets-31, __consumer_offsets-36, __consumer_offsets-4
2, __consumer_offsets-3, __consumer_offsets-18, __consumer_offsets-15, __consumer_offsets-24, __consumer_offsets-17, heartbeat-0, __consumer_offsets-48, __consumer_offsets-19, __consumer
_offsets-11, __consumer_offsets-2, __consumer_offsets-43, __consumer_offsets-6, project-data-0, __consumer_offsets-14, __consumer_offsets-20, __consumer_offsets-0, __consumer_offsets-44,
 __consumer_offsets-39, __consumer_offsets-12, __consumer_offsets-45, __consumer_offsets-1, __consumer_offsets-5, __consumer_offsets-26, __consumer_offsets-29, __consumer_offsets-34, __c
onsumer_offsets-10, __consumer_offsets-32, __consumer_offsets-40) (kafka.server.ReplicaFetcherManager)
[2565-09-05 22:15:46,507] INFO [ReplicaAlterLogDirsManager on broker 0] Removed fetcher for partitions HashSet(__consumer_offsets-22, __consumer_offsets-30, project-write-0, __consumer_o
ffsets-35, __consumer_offsets-37, project-read-0, __consumer_offsets-38, __consumer_offsets-13, __consumer_offsets-8, __consumer_offsets-21, __consumer_offsets-4, __consumer_offsets-27,
__consumer_offsets-7, analytics-0, __consumer_offsets-9, __consumer_offsets-46, quickstart-events-0, build-0, __consumer_offsets-25, __consumer_offsets-41, __consumer_offsets-33, __consu
mer_offsets-23, __consumer_offsets-49, project-build-0, __consumer_offsets-47, __consumer_offsets-16, __consumer_offsets-28, __consumer_offsets-31, __consumer_offsets-36, __consumer_offs
ets-42, __consumer_offsets-3, __consumer_offsets-18, __consumer_offsets-15, __consumer_offsets-24, __consumer_offsets-17, heartbeat-0, __consumer_offsets-48, __consumer_offsets-19, __con
sumer_offsets-11, __consumer_offsets-2, __consumer_offsets-43, __consumer_offsets-6, project-data-0, __consumer_offsets-14, __consumer_offsets-20, __consumer_offsets-0, __consumer_offset
s-44, __consumer_offsets-39, __consumer_offsets-12, __consumer_offsets-45, __consumer_offsets-1, __consumer_offsets-5, __consumer_offsets-26, __consumer_offsets-29, __consumer_offsets-34
, __consumer_offsets-10, __consumer_offsets-32, __consumer_offsets-40) (kafka.server.ReplicaAlterLogDirsManager)
[2565-09-05 22:15:46,526] WARN [ReplicaManager broker=0] Broker 0 stopped fetcher for partitions __consumer_offsets-22,__consumer_offsets-30,project-write-0,__consumer_offsets-35,__consu
mer_offsets-37,project-read-0,__consumer_offsets-38,__consumer_offsets-13,__consumer_offsets-8,__consumer_offsets-21,__consumer_offsets-4,__consumer_offsets-27,__consumer_offsets-7,analy
tics-0,__consumer_offsets-9,__consumer_offsets-46,quickstart-events-0,build-0,__consumer_offsets-25,__consumer_offsets-41,__consumer_offsets-33,__consumer_offsets-23,__consumer_offsets-4
9,project-build-0,__consumer_offsets-47,__consumer_offsets-16,__consumer_offsets-28,__consumer_offsets-31,__consumer_offsets-36,__consumer_offsets-42,__consumer_offsets-3,__consumer_offs
ets-18,__consumer_offsets-15,__consumer_offsets-24,__consumer_offsets-17,heartbeat-0,__consumer_offsets-48,__consumer_offsets-19,__consumer_offsets-11,__consumer_offsets-2,__consumer_off
sets-43,__consumer_offsets-6,project-data-0,__consumer_offsets-14,__consumer_offsets-20,__consumer_offsets-0,__consumer_offsets-44,__consumer_offsets-39,__consumer_offsets-12,__consumer_
offsets-45,__consumer_offsets-1,__consumer_offsets-5,__consumer_offsets-26,__consumer_offsets-29,__consumer_offsets-34,__consumer_offsets-10,__consumer_offsets-32,__consumer_offsets-40 a
nd stopped moving logs for partitions  because they are in the failed log directory C:\Apps\kafka\logs. (kafka.server.ReplicaManager)
[2565-09-05 22:15:46,527] WARN Stopping serving logs in dir C:\Apps\kafka\logs (kafka.log.LogManager)
[2565-09-05 22:15:46,530] ERROR Shutdown broker because all log dirs in C:\Apps\kafka\logs have failed (kafka.log.LogManager)

C:\kafka>

```



## KNOWN ISSUE

| log        | issue                                                                   | remark |
| ---------- | ----------------------------------------------------------------------- | ------ |
| 2022-07-30 | property panel width is unchangable, see flowchart.xhtml for workaround |        |
|            |                                                                         |        |
|            |                                                                         |        |

----

-- end of document --
