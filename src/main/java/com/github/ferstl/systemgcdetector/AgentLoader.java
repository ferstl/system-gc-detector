package com.github.ferstl.systemgcdetector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.sun.tools.attach.VirtualMachine;


public final class AgentLoader {

  private AgentLoader() {}

  public static void main(String[] args) {
    try {
      addToolsJarToClasspath();
      String pid = getPid(args);
      String agentPath = getAgentPath();

      attachAgent(pid, agentPath);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void addToolsJarToClasspath() {
    Path toolsPath = Paths.get(System.getProperty("java.home", "."), "..", "lib", "tools.jar");
    if (!Files.exists(toolsPath)) {
      throw new IllegalStateException("Path to tools.jar not found: " + toolsPath);
    }

    try {
      ClassLoader classLoader = ClassLoader.getSystemClassLoader();
      Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
      method.setAccessible(true);
      method.invoke(classLoader, new Object[]{toolsPath.toUri().toURL()});
    } catch (Exception e) {
      throw new IllegalStateException("Unable to add tools.jar to classpath", e);
    }
  }

  private static String getPid(String[] args) {
    if (args.length == 0) {
      // System.console() is null on windows :-(
      System.out.println("Enter PID:");
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in, Charset.defaultCharset()));

      try {
        return br.readLine();
      } catch (IOException e) {
        throw new IllegalStateException("Unable to read console", e);
      }
    }

    return args[0];
  }

  private static String getAgentPath() {
    try {
      return Paths.get(AgentLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
    } catch (URISyntaxException e) {
      throw new IllegalStateException("Unable to get agent location", e);
    }
  }

  private static void attachAgent(String pid, String agentPath) {
    VirtualMachine vm;
    try {
      vm = VirtualMachine.attach(pid);
      vm.loadAgent(agentPath);
      System.out.println("Agent attached");

      try {
        vm.detach();
      } catch (IOException e) {
        throw new IllegalStateException("Unable to detach", e);
      }

    } catch (Exception e) {
      throw new IllegalStateException("Unable to attach", e);
    }

  }
}
