package com.example.cleanvault.presentation.notes;

import com.example.cleanvault.domain.repository.NoteRepository;
import com.example.cleanvault.domain.usecase.GetNotesUseCase;
import com.example.cleanvault.domain.usecase.SaveSecretNoteUseCase;
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
public final class NotesViewModel_Factory implements Factory<NotesViewModel> {
  private final Provider<GetNotesUseCase> getNotesUseCaseProvider;

  private final Provider<SaveSecretNoteUseCase> saveSecretNoteUseCaseProvider;

  private final Provider<NoteRepository> repositoryProvider;

  public NotesViewModel_Factory(Provider<GetNotesUseCase> getNotesUseCaseProvider,
      Provider<SaveSecretNoteUseCase> saveSecretNoteUseCaseProvider,
      Provider<NoteRepository> repositoryProvider) {
    this.getNotesUseCaseProvider = getNotesUseCaseProvider;
    this.saveSecretNoteUseCaseProvider = saveSecretNoteUseCaseProvider;
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public NotesViewModel get() {
    return newInstance(getNotesUseCaseProvider.get(), saveSecretNoteUseCaseProvider.get(), repositoryProvider.get());
  }

  public static NotesViewModel_Factory create(Provider<GetNotesUseCase> getNotesUseCaseProvider,
      Provider<SaveSecretNoteUseCase> saveSecretNoteUseCaseProvider,
      Provider<NoteRepository> repositoryProvider) {
    return new NotesViewModel_Factory(getNotesUseCaseProvider, saveSecretNoteUseCaseProvider, repositoryProvider);
  }

  public static NotesViewModel newInstance(GetNotesUseCase getNotesUseCase,
      SaveSecretNoteUseCase saveSecretNoteUseCase, NoteRepository repository) {
    return new NotesViewModel(getNotesUseCase, saveSecretNoteUseCase, repository);
  }
}
