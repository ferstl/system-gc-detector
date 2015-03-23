# system-gc-detector
A Java Agent that detects calls to System.gc()

# Usage

## Start the VM with the agent

```
    java -javaagent:/path/to/agent.jar -jar ...
```

## Attach the agent to a running VM
This does only work with an installed JDK! This means that the tools.jar must be located in `../lib/tools.jar` relative to the attaching JVM.
If `<pid>` is not specified, you'll be asked to enter it.

```
    java -jar path/to/agent.jar <pid>
```