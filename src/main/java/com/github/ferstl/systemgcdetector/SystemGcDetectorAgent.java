package com.github.ferstl.systemgcdetector;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.Arrays;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;


public final class SystemGcDetectorAgent {

  private SystemGcDetectorAgent() {}

  public static void agentmain(String agentArgs, Instrumentation inst) {
    inst.addTransformer(SystemGcDetectorAgent::transformGcMethods, true);
    try {
      // Transforming classes from the java package would crash the JVM
      Class<?>[] retransformableClasses = Arrays.stream(inst.getAllLoadedClasses())
          .filter(inst::isModifiableClass)
          .filter(c -> !c.getName().startsWith("java."))
          .toArray(Class[]::new);
      inst.retransformClasses(retransformableClasses);

    } catch (UnmodifiableClassException e) {
      System.err.println("Unmodifiable class");
      e.printStackTrace();
    }
  }

  public static void premain(String agentArgs, Instrumentation inst) {
    if (!inst.isRetransformClassesSupported()) {
      System.err.println("class retransformation not supported");
      return;
    }

    inst.addTransformer(SystemGcDetectorAgent::transformGcMethods, true);

    try {
      inst.retransformClasses(System.class, Runtime.class);
    } catch (UnmodifiableClassException e) {
      throw new RuntimeException(e);
    }
  }

  static byte[] transformGcMethods(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
    if ("java/lang/System".equals(className)) {
      ClassPool classPool = ClassPool.getDefault();
      try {
        CtClass systemClass = classPool.get("java.lang.System");
        CtMethod gc = systemClass.getDeclaredMethod("gc");
        gc.setBody("{ System.out.println(\"System.gc() called\\n\"); Thread.dumpStack(); }");
        byte[] bytecode = systemClass.toBytecode();
        systemClass.detach();
        return bytecode;
      } catch (NotFoundException | CannotCompileException | IOException e) {
        throw new RuntimeException(e);
      }
    } else if ("java/lang/Runtime".equals(className)) {
      ClassPool classPool = ClassPool.getDefault();
      try {
        CtClass runtimeClass = classPool.get("java.lang.Runtime");
        CtMethod gc = runtimeClass.getDeclaredMethod("gc");
        // remove the native flag
        gc.setModifiers(Modifier.PUBLIC);
        gc.setBody("{ System.out.println(\"Runtime.gc() called\\n\"); Thread.dumpStack(); }");
        byte[] bytecode = runtimeClass.toBytecode();
        runtimeClass.detach();
        return bytecode;
      } catch (NotFoundException | CannotCompileException | IOException e) {
        throw new RuntimeException(e);
      }
    }

    return classfileBuffer;
  }

}
