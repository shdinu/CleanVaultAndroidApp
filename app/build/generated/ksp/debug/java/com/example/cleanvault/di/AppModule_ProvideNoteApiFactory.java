package com.example.cleanvault.di;

import com.example.cleanvault.data.remote.NoteApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class AppModule_ProvideNoteApiFactory implements Factory<NoteApi> {
  @Override
  public NoteApi get() {
    return provideNoteApi();
  }

  public static AppModule_ProvideNoteApiFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static NoteApi provideNoteApi() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideNoteApi());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideNoteApiFactory INSTANCE = new AppModule_ProvideNoteApiFactory();
  }
}
