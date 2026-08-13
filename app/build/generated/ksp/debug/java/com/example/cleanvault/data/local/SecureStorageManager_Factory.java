package com.example.cleanvault.data.local;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class SecureStorageManager_Factory implements Factory<SecureStorageManager> {
  private final Provider<Context> contextProvider;

  public SecureStorageManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SecureStorageManager get() {
    return newInstance(contextProvider.get());
  }

  public static SecureStorageManager_Factory create(Provider<Context> contextProvider) {
    return new SecureStorageManager_Factory(contextProvider);
  }

  public static SecureStorageManager newInstance(Context context) {
    return new SecureStorageManager(context);
  }
}
