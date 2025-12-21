package com.azrag.heyu.util

/**
 * HeyU - Repository işlemlerinin sonucunu modellemek için kullanılan mühürlü sınıf.
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String, val cause: Exception? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
