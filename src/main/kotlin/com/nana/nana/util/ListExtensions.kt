package com.nana.nana.util

fun <T> List<T>.secondOrNull(): T? {
    return if (count() > 1) this[1] else null
}