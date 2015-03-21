package com.github.ferstl.systemgcdetector;

import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.InstructionAdapter;


public class SystemGcDetectorAgent {

  public static void premain(String agentArgs, Instrumentation inst) {
    inst.addTransformer(SystemGcDetectorAgent::transformGcMethods, true);
  }

  static byte[] transformGcMethods(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
    ClassReader reader = new ClassReader(classfileBuffer);
    ClassWriter writer = new ClassWriter(reader, 0);
    GcCallClassVisitor visitor = new GcCallClassVisitor(writer);
    reader.accept(visitor, 0);
    return writer.toByteArray();
  }


  static class GcCallClassVisitor extends ClassVisitor {

    public GcCallClassVisitor(ClassWriter writer) {
      super(Opcodes.ASM5, writer);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
      MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
      return new GcCallMethodVisitor(visitor);
    }
  }

  static class GcCallMethodVisitor extends InstructionAdapter {

    public GcCallMethodVisitor(MethodVisitor visitor) {
      super(Opcodes.ASM5, visitor);
    }

    @Override
    public void invokevirtual(String owner, String name, String desc, boolean itf) {
      super.invokevirtual(owner, name, desc, itf);
      if (isGcCall(owner, name, desc)) {
        addGcCallStackDump();
      }
    }

    @Override
    public void invokestatic(String owner, String name, String desc, boolean itf) {
      super.invokestatic(owner, name, desc, itf);
      if (isGcCall(owner, name, desc)) {
        addGcCallStackDump();
      }
    }

    private boolean isGcCall(String owner, String name, String desc) {
      return "gc".equals(name)
          && "()V".equals(desc)
          && ("java/lang/Runtime".equals(owner) || "java/lang/System".equals(owner));
    }

    private void addGcCallStackDump() {
      getstatic("java/lang/System", "err", "Ljava/io/PrintStream;");
      aconst("GC invocation detected. Stack dump:");
      invokevirtual("java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
      invokestatic("java/lang/Thread", "dumpStack", "()V", false);
    }
  }

}
