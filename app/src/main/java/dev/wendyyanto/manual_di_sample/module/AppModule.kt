package dev.wendyyanto.manual_di_sample.module

import dev.wendyyanto.dependency_lib.annotation.Provides
import dev.wendyyanto.dependency_lib.di.InjectorModule
import dev.wendyyanto.manual_di_sample.utils.AppUtils

class AppModule : InjectorModule {

  @Provides
  fun provideAppUtils(): AppUtils {
    return AppUtils()
  }
}