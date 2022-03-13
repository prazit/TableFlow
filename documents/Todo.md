# TODO LIST

----

:heavy_check_mark: = completed

:x: = cancelled

:white_check_mark: = in progress

|        Done        | Task                                                         | Remark                                                       |
| :----------------: | ------------------------------------------------------------ | ------------------------------------------------------------ |
| :heavy_check_mark: | Write to files: <br />need design of file structure          | see: **File Structure.md**                                   |
| :heavy_check_mark: | need storage selection                                       | storages: (embedded or dbms) sqlite, berkeley, file or mongo, java-db<br /><br />**file system** is the best choice (fastest query time when have large amount of objects) |
| :heavy_check_mark: | need technology selection                                    | back end: **REST** or learn more about MicroService(both client browser and server), Kafka (event queue) and friends |
|       :car:        | need POC                                                     | + make sure REST work between Front-End and Back-End<br />+ make sure the Write Command Queue Manager can run on another Thread and the Thread is never die.<br />+ make sure we can run the first command without changes to the Queue until the command is in Succuss state. |
|                    | need Write Queue System, <br />no wait time for Write, <br />guarantee no fail for Write <br /> | back end: **Write Command Queue Service** is an easy to create, create it without any lib. (when failed need rerun first queue again until it success) <br />+ need low storage alert (show at the Project Selection step before open project)<br />+ need to lock all change when open Project with an storage alert<br />+ need to lock all change when open Working Project (already open by another session) and need to know who working on the Project *(this feature can use the push notification from working session to see real time change from the working session)<br />*+ all Write Command will using the Write Command Queue. **Write Command is a command to make some changes to the Step object.**<br />front end event: send write command at every changes that made to Step object. |
|                    | need **validation**(and all processes to make sure the command always success) before queued | + invalid command<br />+ invalid front-end-Token             |
|                    | need list of **possible errors** on the Write Command after queued | + storage alerted: no write command is allowed (no insufficient storage, no quota exceeded)<br />+ internal: I/O Exceptions (such NAS access failed) |
|      :anchor:      | need separated service for other commands (Non Write Command) | back end: **Project Service<br />**+ all about Project Management (Read/Write Project)<br />+ **Read Step Detail** |
|      :anchor:      |                                                              | back end: **Administrator Service**<br />+ all about user, authentication and authorization<br />+ act as URL provider/front end registration (all service URLs need to fetch from this service at start of front-end) need front-end Token to Control number of Servers |


----

-- end of document --