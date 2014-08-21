package com.github.stephanenicolas.injectview

import com.github.stephanenicolas.morpheus.AbstractMorpheusPlugin;
import javassist.build.IClassTransformer;
import org.gradle.api.Project;

/**
 * @author SNI
 */
public class InjectViewPlugin extends AbstractMorpheusPlugin {

  @Override
  public IClassTransformer[] getTransformers(Project project) {
    return new InjectViewProcessor();
  }

  @Override
  protected void configure(Project project) {
    project.dependencies {
      provided 'com.github.stephanenicolas.injectview:injectview-annotations:1.0.0-SNAPSHOT'
    }
  }

  @Override
  protected Class getPluginExtension() {
    InjectViewPluginExtension
  }

  @Override
  protected String getExtension() {
    "injectview"
  }
}
