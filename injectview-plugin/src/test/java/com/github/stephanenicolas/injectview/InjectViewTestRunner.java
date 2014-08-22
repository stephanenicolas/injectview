package com.github.stephanenicolas.injectview;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.build.JavassistBuildException;
import lombok.extern.slf4j.Slf4j;
import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.SdkConfig;
import org.robolectric.bytecode.AsmInstrumentingClassLoader;
import org.robolectric.bytecode.Setup;

/**
 * Created by administrateur on 2014-08-21.
 */
@Slf4j
public class InjectViewTestRunner extends RobolectricTestRunner {
  private InjectViewProcessor processor = new InjectViewProcessor();

  public InjectViewTestRunner(Class<?> testClass) throws InitializationError {
    super(testClass);
  }

  @Override protected ClassLoader createRobolectricClassLoader(Setup setup, SdkConfig sdkConfig) {
    URL[] urls = getJarResolver().getLocalArtifactUrls(sdkConfig.getSdkClasspathDependencies());
    return new AsmInstrumentingClassLoader(setup, urls) {
      @Override protected byte[] getByteCode(String className) throws ClassNotFoundException {
        if (className.startsWith("com.github.stephanenicolas.injectview")) {
          try {
            CtClass dummyClass = ClassPool.getDefault().get(className);
            if (processor.shouldTransform(dummyClass)) {
              log.debug("Intercepting via InjectView " + className);
              processor.applyTransformations(dummyClass);
              byte[] bytes = dummyClass.toBytecode();
              System.out.println("Size of weaved byte code :" + bytes.length);
              FileOutputStream fileOutputStream = new FileOutputStream("/tmp/" + className + ".class");
              fileOutputStream.write(bytes);
              fileOutputStream.close();
              return bytes;
            }
          } catch (Exception e) {
            throw new RuntimeException("Impossible to transform class " + className, e);
          }
        }
        return super.getByteCode(className);
      }
    };
  }
}
