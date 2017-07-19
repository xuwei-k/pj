# pj

[![Build Status](https://travis-ci.org/xuwei-k/pj.svg?branch=master)](https://travis-ci.org/xuwei-k/pj)
[![pj javadoc](http://javadoc-badge.appspot.com/com.github.xuwei-k/pj_2.12.svg?label=pj%20javadoc)](http://javadoc-badge.appspot.com/com.github.xuwei-k/pj_2.12/pj/index.html?javadocio=true)
[![pj-app javadoc](http://javadoc-badge.appspot.com/com.github.xuwei-k/pj-app_2.12.svg?label=pj-app%20javadoc)](http://javadoc-badge.appspot.com/com.github.xuwei-k/pj-app_2.12/pj/index.html?javadocio=true)

Makes json all pretty-like.

## as a conscript app

### install

Installation requires [conscript][cs]

```
cs xuwei-k/pj
```

### usage

Formatting piped data with curl (discarding stderr), use empty `--` flag

```
curl 'http://api.tea.io/time' 2>/dev/null | pj --
{
  "tea_time": true
}
```
    
Formatting json from file and writing to another.

```
pj -f path/to/in.json -o path/to/out.json
```
    
Formatting inline json

```
pj -j '{"oh":"la,la","datas":[1,2,3,4],"objects":{"waka":"waka"}}'
```
    
Getting help

```
pj -h
```

## as a library

### install

```scala
libraryDependencies += "com.github.xuwei-k" %% "pj" % "0.1.1"
```

### usage

```
val raw = """{"oh":"la,la","datas":[1,2,3,4],"objects":{"waka":"waka"}}"""
println(pj.Printer(raw))
{
  "oh" : "la,la",
  "datas" : [ 1, 2, 3, 4 ],
  "objects" : {
    "waka" : "waka"
  }
}
```

Doug Tangren (softprops), Kenji Yoshida (xuwei-k) 2012 -

[cs]: https://github.com/foundweekends/conscript#readme
