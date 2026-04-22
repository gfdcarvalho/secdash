package com.isel.ps.secdash.utils

import io.github.cdimascio.dotenv.dotenv

val dotenv = dotenv { ignoreIfMissing = true }

fun env(key: String): String = dotenv[key] ?: error("Missing environment variable: $key")