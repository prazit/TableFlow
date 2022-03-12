# TODO LIST

----

:heavy_check_mark: = completed

:x: = cancelled

:white_check_mark: = in progress

|        Done        | Task                                                         | Remark                                                       |
| :----------------: | ------------------------------------------------------------ | ------------------------------------------------------------ |
| :heavy_check_mark: | Action of Line create more problems, need to recheck/design Line Action is needed or not.<br /><br />question:<br />+ 1:where and when the Add/RemoveLine action is created.<br />+ 2:why need the AddLine action. | answer:<br />+ 1:every call of step.addLine/removeLine within command will create Add/RemoveLine action<br />+ 2: for Undo<br /><br /><br />solved:<br />+ need 2 modes for Add/RemoveLine, one for User Action and one for Internal-Command.<br />+ User Action need the Action for Undo<br />+ Internal Command don't need it and expect the Action History has no change between command process. |


----

-- end of document --