package com.dhkim.data.user.di

import com.dhkim.data.user.repository.UserRepositoryImpl
import com.dhkim.domain.user.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class UserModule {

    @Binds
    abstract fun bindUserRepository(userRepositoryImpl: UserRepositoryImpl): UserRepository
}