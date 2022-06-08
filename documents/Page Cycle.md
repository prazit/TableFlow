## PAGE CYCLE

----

### Front

```less
1) Page Load {
    get System Alert Message; no Message;
    read A;
    read B;
    read C;
    read D;
}

2) Page Submitting {
    write A;
    write B;
    write C;
}

3) Page Refreshing (in case of NAS is down a long time) {
    get System Alert Message; no Message;
    read A;
    read B;
    read C; got Timeout;
    cancel read D;
    show Warning about Timeout.
    try to refresh again;
}

4) Page Refreshing (in case of NAS is gone, need to replacing NAS and recover data) {
    get System Alert Message; got NAS_DOWN;
    cancel read A,B,C,D;
    show Error about NAS_DOWN.
}

5) Page Refreshing (in normal case) {
    get System Alert Message; no Message;
    read A;
    read B;
    read C;
    read D;
    Show write A,B,C is success;
}

6) Page Refreshing (in case of QUOTA_EXCEEDED) {
    get System Alert Message; got QUOTA_EXCEEDED;
    read A;
    read B;
    read C;
    read D;
    Show warning about Read Only;
}

7) Page Refreshing (in case of project is Editing by another session) {
    get System Alert Message; no Message;
    read A; A is editing by EditorMan at IP Address.
    read B;
    read C;
    read D;
    Show warning about Read Only;
}
```

### Write Command Service 

```less
2) (guarantee no fail) {
	write A; mark A; enqueue A; return Success;
	write B; mark B; enqueue B; return Success;
	write C; mark C; enqueue C; return Success;
}

(queue service) {
    get first queue A; Invoke write A; unmark A; remove first queue A;
    get first queue B; Invoke write B; unmark B; remove first queue B;
    get first queue C; Invoke write C; got Exception; wait some time depends on the Exception;
    get first queue C; Invoke write C; got Exception; add System Alert Message about NAS is gone;
    get first queue C; Invoke write C; unmark C; remove first queue C;
}
```

### Read Command Service

```less
1,5) {
    read System Alert Message; return none;
    read A; A is unmarked; return A;
    read B; B is unmarked; return B;
    read C; C is unmarked; return C;
    read D; D is unmarked; return D;
}

3) {
    read System Alert Message; return none;
    read A; A is unmarked; return A;
    read B; B is unmarked; return B;
    read C; C is marked; waiting for C is unmarked; return C;
    read D; D is unmarked; return D;
}

4) {
    read System Alert Message; return NAS_DOWN;
}
```

----

-- end of document --