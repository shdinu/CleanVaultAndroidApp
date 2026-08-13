package com.example.cleanvault.domain.usecase;

import com.example.cleanvault.domain.repository.NoteRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class GetNotesUseCase_Factory implements Factory<GetNotesUseCase> {
  private final Provider<NoteRepository> repositoryProvider;

  public GetNotesUseCase_Factory(Provider<NoteRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetNotesUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetNotesUseCase_Factory create(Provider<NoteRepository> repositoryProvider) {
    return new GetNotesUseCase_Factory(repositoryProvider);
  }

  public static GetNotesUseCase newInstance(NoteRepository repository) {
    return new GetNotesUseCase(repository);
  }
}
