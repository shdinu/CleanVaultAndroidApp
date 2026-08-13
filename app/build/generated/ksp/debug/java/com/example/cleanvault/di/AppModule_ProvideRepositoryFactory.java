package com.example.cleanvault.di;

import com.example.cleanvault.data.local.NoteDao;
import com.example.cleanvault.data.local.SecureStorageManager;
import com.example.cleanvault.data.remote.NoteApi;
import com.example.cleanvault.domain.repository.NoteRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import kotlinx.coroutines.CoroutineDispatcher;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppModule_ProvideRepositoryFactory implements Factory<NoteRepository> {
  private final Provider<NoteApi> apiProvider;

  private final Provider<NoteDao> daoProvider;

  private final Provider<SecureStorageManager> secureStorageProvider;

  private final Provider<CoroutineDispatcher> dispatcherProvider;

  public AppModule_ProvideRepositoryFactory(Provider<NoteApi> apiProvider,
      Provider<NoteDao> daoProvider, Provider<SecureStorageManager> secureStorageProvider,
      Provider<CoroutineDispatcher> dispatcherProvider) {
    this.apiProvider = apiProvider;
    this.daoProvider = daoProvider;
    this.secureStorageProvider = secureStorageProvider;
    this.dispatcherProvider = dispatcherProvider;
  }

  @Override
  public NoteRepository get() {
    return provideRepository(apiProvider.get(), daoProvider.get(), secureStorageProvider.get(), dispatcherProvider.get());
  }

  public static AppModule_ProvideRepositoryFactory create(Provider<NoteApi> apiProvider,
      Provider<NoteDao> daoProvider, Provider<SecureStorageManager> secureStorageProvider,
      Provider<CoroutineDispatcher> dispatcherProvider) {
    return new AppModule_ProvideRepositoryFactory(apiProvider, daoProvider, secureStorageProvider, dispatcherProvider);
  }

  public static NoteRepository provideRepository(NoteApi api, NoteDao dao,
      SecureStorageManager secureStorage, CoroutineDispatcher dispatcher) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideRepository(api, dao, secureStorage, dispatcher));
  }
}
