package com.nick.sampleffmpeg;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import com.nick.libffmpeg.FFmpeg;

@Module(
        injects = Home.class
)
@SuppressWarnings("unused")
public class DaggerDependencyModule {

    private final Context context;

    DaggerDependencyModule(Context context) {
        this.context = context;
    }

    @Provides @Singleton
    FFmpeg provideFFmpeg() {
        return FFmpeg.getInstance(context.getApplicationContext());
    }

}
