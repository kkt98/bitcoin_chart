package com.kkt981019.bitcoin_chart.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    private val BASE_URL = "https://api.upbit.com/"

    @Provides
    @Singleton
    fun upBitRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideUpbitApi(retrofit: Retrofit): UpbitApi =
        retrofit.create(UpbitApi::class.java)

}