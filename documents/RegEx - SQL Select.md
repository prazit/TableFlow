# Regular Expression
for SQL Select
----

## find Quotes
> singleQuoted | doubleQuoted | parenthesisQuoted
```regexp
[']([^']+)[']|[\"]([^\"]+)[\"]|[\(]([^\(\)]+)[\)]
```

## find component in select
```regexp
//startWith: select   | ,
(\s*[,]|\s*[Ss][Ee][Ll][Ee][Cc][Tt])

//operator: operator
\s*[\+\-\*\/]|\s*[\|\&]{2}

//function: name(anything)| (anything)
\s*([A-Za-z][A-Za-z0-9_]*)*[\(][^\(\)]*[\)]

//constant: 'string' | "string" | number | decimal.dec
\s*['].*[']|\s*["].*["]|\s*[0-9]+([.][0-9]+)?

//name: name.name|name
\s*[A-Za-z][A-Za-z0-9_]*[.][A-Za-z][A-Za-z0-9_]*|\s*[A-Za-z][A-Za-z0-9_]*

//endWith: as  name
([Aa][Ss]\s+[A-Za-z][A-Za-z0-9_]*)

// ----

//expression: (operator) (function)
((\s*[\+\-\*\/]|\s*[\|\&]{2})*(\s*([A-Za-z][A-Za-z0-9_]*)*[\(].*[\)]))+

//expression: (operator) (function|constant)
((\s*[\+\-\*\/]|\s*[\|\&]{2})*((\s*([A-Za-z][A-Za-z0-9_]*)*[\(].*[\)])|(\s*['].*[']|\s*["].*["]|\s*[0-9]+([.][0-9]+)?)))+
```

## Complete RegEx to find column in select
```regexp
//expression: (operator) (constant|name)
((\s*[\+\-\*\/]|\s*[\|\&]{2})*((\s*['].*[']|\s*["].*["]|\s*[0-9]+([.][0-9]+)?)|(\s*[A-Za-z][A-Za-z0-9_]*[.][A-Za-z][A-Za-z0-9_]*|\s*[A-Za-z][A-Za-z0-9_]*)))+


//expression: (startWith) (function|constant|name)
((\s*[\+\-\*\/]{1}|\s*[\|\&]{2})*((\s*([A-Za-z][A-Za-z0-9_]*)*[\(].*[\)])|(\s*['].*[']|\s*["].*["]|\s*[0-9]+([.][0-9]+){0,1})|(\s*[A-Za-z][A-Za-z0-9_]*[.][A-Za-z][A-Za-z0-9_]*|\s*[A-Za-z][A-Za-z0-9_]*)))+
```