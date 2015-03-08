package com.github.ferstl.systemgcdetector;

import org.junit.Test;

public class SystemGcDetectorAgentTest {

  @Test
  public void systemGc() {
    System.gc();
  }

  @Test
  public void runtimeGc() {
    Runtime.getRuntime().gc();
  }

}
