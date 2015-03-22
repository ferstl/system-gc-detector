# system-gc-detector
A Java Agent that detects calls to System.gc()

# Usage

## Start the VM with the agent

```
    java -javaagent:/path/to/agent.jar -jar ...
```

## Attach the agent to a running VM
This does only work with an installed JDK! Currently the `AgentLoader` assumes the agent.jar to located on `./target/agent.jar`.
If `<pid>` is not specified, you'll be asked to enter it.

```
    java -cp path/to/tools.jar:path/to/agent.jar com.github.ferstl.systemgcdetector.AgentLoader <pid>
```