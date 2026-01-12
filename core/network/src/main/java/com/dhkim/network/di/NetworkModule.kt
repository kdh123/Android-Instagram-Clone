package com.dhkim.network.di

import com.dhkim.network.ConnectivityChecker
import com.dhkim.network.ConnectivityCheckerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ConnectivityCheckerModule {

    @Binds
    @Singleton
    abstract fun bindConnectivityChecker(impl: ConnectivityCheckerImpl): ConnectivityChecker
}