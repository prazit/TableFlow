# PAGE EXCEPTIONS

how to handle UI exceptions.

----



### EXCEPTION ON PAGE-LOAD

All exceptions that occured before page is rendered.

+ use **Error-Page** specified in web.xml

+ already have generic page: unexpected.xhtml



### EXCEPTION ON ACTION-LISTENER

All exceptions that occured after page is rendered include ajaxActionListener.

+ use Primeface **ajaxExceptionHandler** tag

+ already have UI-VIEW: ajaxexception.xhtml



----

-- end of document --
