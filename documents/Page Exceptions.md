# PAGE EXCEPTIONS

how to handle UI exceptions.

----



## PAGE WITHOUT SUB-PAGE

Example: Group Page, Library Page.



### EXCEPTION ON POSTSRUCT

All exceptions that occured before page is rendered.

+ use **Error-Page** specified in web.xml



### EXCEPTION ON ACTION-LISTENER

All exceptions that occured after page is rendered.

+ use Primeface **ExceptionHandler** tag



----

## PAGE WITH SUB-PAGE

Example: Editor Page.



### EXCEPTION ON POSTSRUCT of PARENT-PAGE

All exceptions that occured before page is rendered.

+ use Error-Page specified in web.xml

### 

### EXCEPTION ON ACTION-LISTENER

All exceptions that occured after page is rendered.

+ use Primeface ExceptionHandler tag



----

-- end of document --
