# turkish-deasciifier

Yet another Turkish deasciifier implementation written in Scala. 

It's adapted from Emre Sevinç's Turkish Deasciifier ([source](https://github.com/emres/turkish-deasciifier)) and Deniz Yüret's Emacs Turkish Mode ([source](https://github.com/emres/turkish-mode)).
More information can be found [in this blog post](http://www.denizyuret.com/2006/11/emacs-turkish-mode.html). 
The default pattern table (descision lists) JSON is used from Fatih Kadir Akın's Node.js project ([source](https://github.com/f/deasciifier/)).

The `core` module can be used as a library itself. To use as an application project can be assembled in a JAR:
```shell script
sbt assembly
```

To run:
```shell script
java -jar app/target/scala-2.12/app-assembly-1.0.0.jar
Turkce'nin dogru yazimi
Türkçe'nin doğru yazımı
```
