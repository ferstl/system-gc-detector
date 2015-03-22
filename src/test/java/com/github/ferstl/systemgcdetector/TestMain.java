package com.github.ferstl.systemgcdetector;

import java.lang.management.ManagementFactory;


public final class TestMain {

  private TestMain() {}

  public static void main(String[] args) {

    String processName = ManagementFactory.getRuntimeMXBean().getName();
    CharSequence pid = processName.subSequence(0, processName.indexOf("@"));
    System.out.println("PID: " + pid);

    while (true) {
      callGc();
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        return;
      }
    }

  }

  private static void callGc() {
    System.err.println("Calling Systen.gc()");
    System.gc();
  }
}
