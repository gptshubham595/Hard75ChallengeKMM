package com.shubham.hard75kmm

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform