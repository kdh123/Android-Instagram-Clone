package com.dhkim.network

import kotlinx.coroutines.flow.Flow

interface ConnectivityChecker {

    fun isNetworkAvailable(): Flow<Boolean>
}