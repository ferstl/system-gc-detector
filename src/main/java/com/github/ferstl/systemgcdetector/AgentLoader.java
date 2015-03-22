package com.github.ferstl.systemgcdetector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import com.sun.tools.attach.VirtualMachine;


public final class AgentLoader {

  private AgentLoader() {}

  public static void main(String[] args) {
    String pid = getPid(args);

    try {
      VirtualMachine vm = VirtualMachine.attach(pid);
      vm.loadAgent("target/agent.jar");
      vm.detach();
      System.out.println("Agent attached");
    } catch (Exception e) {
      System.err.println("Unable to attach");
      e.printStackTrace();
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
        System.err.println("Unable to read console");
        throw new IllegalStateException(e);
      }
    }

    return args[0];
  }
}
