package com.bgvofir.productcatalog.data

// Result of an async call. Screens render each case, so `when` over it is exhaustive.
sealed interface DataState<out T> {
    data object Loading : DataState<Nothing>

    data class Success<T>(val data: T) : DataState<T>

    data class Error(val type: ErrorType, val cause: Throwable? = null) : DataState<Nothing>
}

// The UI turns these into user-facing strings, so no text lives in the data layer.
enum class ErrorType {
    NETWORK,
    NOT_FOUND,
    SERVER,
}
