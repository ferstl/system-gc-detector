package com.github.ferstl.systemgcdetector;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;


public class SystemGcDetectorAgent {

  public static void premain(String agentArgs, Instrumentation inst) {
    inst.addTransformer(new SystemGcRewriter(), true);

    try {
      inst.retransformClasses(System.class);
    } catch (UnmodifiableClassException e) {
      throw new RuntimeException(e);
    }

  }

  private static class SystemGcRewriter implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
      if("java/lang/System".equals(className)) {
        ClassPool classPool = ClassPool.getDefault();
        try {
          CtClass systemClass = classPool.get("java.lang.System");
          CtMethod gc = systemClass.getDeclaredMethod("gc");
          gc.insertBefore("System.out.println(\"System.gc() called\\n\"); Thread.dumpStack();");
          byte[] bytecode = systemClass.toBytecode();
          systemClass.detach();
          return bytecode;
        } catch (NotFoundException | CannotCompileException | IOException e) {
          throw new RuntimeException(e);
        }
      }

      return classfileBuffer;
    }
  }
}
