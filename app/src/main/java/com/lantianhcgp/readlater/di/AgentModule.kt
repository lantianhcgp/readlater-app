package com.lantianhcgp.readlater.di

import com.lantianhcgp.readlater.agent.tools.AutoTagTool
import com.lantianhcgp.readlater.agent.tools.FetchContentTool
import com.lantianhcgp.readlater.agent.tools.FormatContentTool
import com.lantianhcgp.readlater.agent.tools.SummarizeTool
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Named
import javax.inject.Singleton
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object AgentModule {

    @Provides
    @Singleton
    @Named("plain")
    fun providePlainOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideFetchContentTool(@Named("plain") client: OkHttpClient): FetchContentTool {
        return FetchContentTool(client)
    }

    @Provides
    @Singleton
    fun provideSummarizeTool(): SummarizeTool = SummarizeTool()

    @Provides
    @Singleton
    fun provideAutoTagTool(): AutoTagTool = AutoTagTool()

    @Provides
    @Singleton
    fun provideFormatContentTool(): FormatContentTool = FormatContentTool()
}
