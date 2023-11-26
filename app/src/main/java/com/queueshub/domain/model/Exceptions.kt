package com.queueshub.domain.model

import java.io.IOException


class NoMoreArticlesException(message: String): Exception(message)

class NetworkUnavailableException(message: String = "تحقق من الاتصال بالانترنت") : IOException(message)

class NetworkException(message: String): Exception(message)