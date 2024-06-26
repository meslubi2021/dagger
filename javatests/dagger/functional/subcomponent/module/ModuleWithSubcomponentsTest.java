/*
 * Copyright (C) 2016 The Dagger Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dagger.functional.subcomponent.module;

import static com.google.common.truth.Truth.assertThat;

import dagger.Module;
import dagger.functional.subcomponent.module.UsesModuleSubcomponents.ParentIncludesSubcomponentTransitively;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link Module#subcomponents()}. */
@RunWith(JUnit4.class)
public class ModuleWithSubcomponentsTest {

  @Test
  public void subcomponentFromModuleAndFactoryMethod() {
    SubcomponentFromModuleAndFactoryMethod.ExposesBuilder parent =
        DaggerSubcomponentFromModuleAndFactoryMethod_ExposesBuilder.create();
    SubcomponentFromModuleAndFactoryMethod.Sub sub = parent.subcomponentBuilder().sub();
    assertThat(sub).isNotNull();
  }

  @Test
  public void subcomponentFromModules() {
    UsesModuleSubcomponents parent = DaggerUsesModuleSubcomponents.create();
    assertThat(parent.strings()).containsExactly("from parent");
    assertThat(parent.usesChild().strings).containsExactly("from parent", "from child");
  }

  @Test
  public void subcomponentFromModules_transitively() {
    ParentIncludesSubcomponentTransitively parent =
        DaggerUsesModuleSubcomponents_ParentIncludesSubcomponentTransitively.create();
    assertThat(parent.strings()).containsExactly("from parent");
    assertThat(parent.usesChild().strings).containsExactly("from parent", "from child");
  }
}
