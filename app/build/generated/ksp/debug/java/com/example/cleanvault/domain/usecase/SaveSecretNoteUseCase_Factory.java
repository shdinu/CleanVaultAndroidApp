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
public final class SaveSecretNoteUseCase_Factory implements Factory<SaveSecretNoteUseCase> {
  private final Provider<NoteRepository> repositoryProvider;

  public SaveSecretNoteUseCase_Factory(Provider<NoteRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public SaveSecretNoteUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static SaveSecretNoteUseCase_Factory create(Provider<NoteRepository> repositoryProvider) {
    return new SaveSecretNoteUseCase_Factory(repositoryProvider);
  }

  public static SaveSecretNoteUseCase newInstance(NoteRepository repository) {
    return new SaveSecretNoteUseCase(repository);
  }
}
