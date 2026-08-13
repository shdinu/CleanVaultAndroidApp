package com.example.cleanvault.data.repository;

import com.example.cleanvault.data.local.NoteDao;
import com.example.cleanvault.data.local.SecureStorageManager;
import com.example.cleanvault.data.remote.NoteApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import kotlinx.coroutines.CoroutineDispatcher;

@ScopeMetadata
@QualifierMetadata
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
public final class NoteRepositoryImpl_Factory implements Factory<NoteRepositoryImpl> {
  private final Provider<NoteApi> apiProvider;

  private final Provider<NoteDao> daoProvider;

  private final Provider<SecureStorageManager> secureStorageProvider;

  private final Provider<CoroutineDispatcher> ioDispatcherProvider;

  public NoteRepositoryImpl_Factory(Provider<NoteApi> apiProvider, Provider<NoteDao> daoProvider,
      Provider<SecureStorageManager> secureStorageProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    this.apiProvider = apiProvider;
    this.daoProvider = daoProvider;
    this.secureStorageProvider = secureStorageProvider;
    this.ioDispatcherProvider = ioDispatcherProvider;
  }

  @Override
  public NoteRepositoryImpl get() {
    return newInstance(apiProvider.get(), daoProvider.get(), secureStorageProvider.get(), ioDispatcherProvider.get());
  }

  public static NoteRepositoryImpl_Factory create(Provider<NoteApi> apiProvider,
      Provider<NoteDao> daoProvider, Provider<SecureStorageManager> secureStorageProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    return new NoteRepositoryImpl_Factory(apiProvider, daoProvider, secureStorageProvider, ioDispatcherProvider);
  }

  public static NoteRepositoryImpl newInstance(NoteApi api, NoteDao dao,
      SecureStorageManager secureStorage, CoroutineDispatcher ioDispatcher) {
    return new NoteRepositoryImpl(api, dao, secureStorage, ioDispatcher);
  }
}
