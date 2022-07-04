# eazy-ssh-jsch
Eazy SSH implementation over JSch

## Adding dependency
### Maven
```xml
<!-- https://mvnrepository.com/artifact/io.github.bytes-chaser/eazy-ssh-jsch -->
<dependency>
    <groupId>io.github.bytes-chaser</groupId>
    <artifactId>eazy-ssh-jsch</artifactId>
    <version>1.0.0</version>
</dependency>
```
### Gradle
```gradle
// https://mvnrepository.com/artifact/io.github.bytes-chaser/eazy-ssh-jsch
implementation 'io.github.bytes-chaser:eazy-ssh-jsch:1.0.0'
```
### Usage 
Use the [core project "usage" section](https://github.com/bytes-chaser/eazy-ssh-core/blob/main/README.md#usage) for full usage reference.
The only step that may vary is [setting the context SSH implementation](https://github.com/bytes-chaser/eazy-ssh-core/blob/main/README.md#set-it-up)
```java
        // New context with defaults
        ESSHContext context = new ESSHContextImpl();
        context.setHost(host);
        context.setUser(user);
        context.setPass(pass);
        
        // registers client and parser in the context
        ESSHContext context = context
            .register(SimpleClient.class)
            .parser(MemoryInfo.class, new MemoryInfoParser())
            .create(Jsch::new)
```
