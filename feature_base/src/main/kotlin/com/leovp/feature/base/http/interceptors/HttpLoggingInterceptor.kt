package com.leovp.feature.base.http.interceptors

import android.R.attr.tag
import com.leovp.log.base.LogOutType
import com.leovp.log.base.w
import java.io.IOException
import java.util.concurrent.TimeUnit
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.internal.http.promisesBody
import okio.Buffer

/**
 * Author: Michael Leo
 * Date: 20-5-27 下午8:41
 */
class HttpLoggingInterceptor(private val logger: Logger = Logger.DEFAULT) :
    Interceptor {
    enum class Level {
        /**
         * No logs.
         */
        NONE,

        /**
         * Logs request and response lines.
         *
         *
         * Example:
         * <pre>
         * --> POST /greeting http/1.1 (3-byte body)
         *
         * <-- 200 OK (22ms, 6-byte body)
         * </pre>
         */
        @Suppress("unused")
        BASIC,

        /**
         * Logs request and response lines and their respective headers.
         *
         *
         * Example:
         * <pre>
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         * <-- END HTTP
         * </pre>
         */
        HEADERS,

        /**
         * Logs request and response lines
         * and their respective headers and bodies (if present).
         *
         *
         * Example:
         * <pre>
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         *
         * Hi?
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         *
         * Hello!
         * <-- END HTTP
         * </pre>
         */
        BODY
    }

    interface Logger {
        fun log(message: String?, outputType: LogOutType = LogOutType.COMMON)

        companion object {
            /**
             * A [Logger] defaults output appropriate for the current platform.
             */
            val DEFAULT: Logger = object : Logger {
                override fun log(message: String?, outputType: LogOutType) {
                    w {
                        tag = TAG
                        this.outputType = outputType
                        this.message = message
                    }
                }
            }
        }
    }

    /**
     * Change the level at which this interceptor logs.
     */
    @Volatile
    var level = Level.NONE

    @Suppress("LongMethod", "CyclomaticComplexMethod", "NestedBlockDepth", "TooGenericExceptionCaught")
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val level = level
        val request = chain.request()
        if (level == Level.NONE) {
            return chain.proceed(request)
        }
        val logBody = level == Level.BODY
        val logHeaders = logBody || level == Level.HEADERS
        val requestBody = request.body
        val hasRequestBody = requestBody != null
        val connection = chain.connection()
        val protocol = connection?.protocol() ?: Protocol.HTTP_1_1
        var hasBoundary = false
        logger.log("──────────────────────────────────────────────────────────")
        var requestStartMessage = "--> ${request.method} ${request.url} $protocol"
        if (!logHeaders && hasRequestBody) {
            requestStartMessage =
                "$requestStartMessage (${requestBody.contentLength()}-byte body)"
        }
        logger.log(requestStartMessage)
        if (logHeaders) {
            if (hasRequestBody) {
                // Request body headers are only present when installed as a network interceptor.
                // Force them to be included (when available) so there values are known.
                requestBody.contentType()?.let {
                    logger.log("Content-Type: $it")
                    if (it.toString().contains("boundary=")) {
                        hasBoundary = true
                    }
                }
                if (requestBody.contentLength() != -1L) {
                    logger.log("Content-Length: ${requestBody.contentLength()}")
                }
            }
            val headers = request.headers
            for (i in 0 until headers.size) {
                val name = headers.name(i)
                // Skip headers from the request body as they are explicitly logged above.
                if (!"Content-Type".equals(
                        name,
                        ignoreCase = true
                    ) && !"Content-Length".equals(name, ignoreCase = true)
                ) {
                    logger.log(
                        "$name: ${headers.value(i)}",
                        outputType = LogOutType.HTTP_HEADER
                    )
                }
            }
            if (!logBody || !hasRequestBody) {
                logger.log("--> END ${request.method}")
            } else if (bodyEncoded(request.headers)) {
                logger.log("--> END ${request.method} (encoded body omitted)")
            } else if (hasBoundary) {
                val extraLogSize = "${requestBody.contentLength()}-byte"
                logger.log("--> END ${request.method} (Found boundary $extraLogSize body omitted)")
            } else {
                val buffer = Buffer()
                requestBody.writeTo(buffer)
                var charset = DEFAULT_CHARSET
                requestBody.contentType()?.also {
                    charset = it.charset(DEFAULT_CHARSET)!!
                }
                logger.log("")
                val extraLogSize = "${requestBody.contentLength()}-byte"
                if (isPlaintext(buffer)) {
                    val content = buffer.readString(charset)
                    logger.log(content)
                    logger.log("--> END ${request.method} ($extraLogSize body)")
                } else {
                    logger.log("--> END ${request.method} (binary $extraLogSize body omitted)")
                }
            }
            logger.log("──────────────────────────────────────────────────────────")
        }
        val startNs = System.nanoTime()
        val response: Response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            logger.log("──────────────────────────────────────────────────────────")
            logger.log("<-- HTTP FAILED: $e")
            logger.log("──────────────────────────────────────────────────────────")
            throw e
        }
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
        logger.log("──────────────────────────────────────────────────────────")
        val responseBody = response.body
        val contentLength = responseBody?.contentLength() ?: -1
        val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
        val logRespPart = "${response.code} ${response.message} ${response.request.url}"
        logger.log(
            "<-- $logRespPart (${tookMs}ms${if (!logHeaders) " , $bodySize body" else ""})"
        )
        if (logHeaders) {
            val headers = response.headers
            var hasInlineFile = false
            var hasAttachment = false
            for (i in 0 until headers.size) {
                logger.log(
                    "${headers.name(i)}: ${headers.value(i)}",
                    outputType = LogOutType.HTTP_HEADER
                )
                if ("Content-Disposition".contentEquals(headers.name(i), ignoreCase = true)) {
                    if (headers.value(i).startsWith("inline; filename")) {
                        hasInlineFile = true
                    } else if (headers.value(i).startsWith("attachment; filename")) {
                        hasAttachment = true
                    }
                }
            }
            if (!logBody || !response.promisesBody()) {
                logger.log("<-- END HTTP")
            } else if (bodyEncoded(response.headers)) {
                logger.log("<-- END HTTP (encoded body omitted)")
            } else if (hasInlineFile) {
                logger.log("<-- END HTTP (inline file omitted)")
            } else if (hasAttachment) {
                logger.log("<-- END HTTP (attachment $bodySize omitted)")
            } else {
                var charset = DEFAULT_CHARSET
                val contentType = responseBody?.contentType()
                if (contentType != null) {
                    charset = contentType.charset(DEFAULT_CHARSET)!!
                }
                val source = responseBody?.source()
                source?.request(Long.MAX_VALUE)
                // Buffer the entire body.
                val buffer = source?.buffer
                if (contentLength != 0L) {
                    logger.log(" \n${buffer?.clone()?.readString(charset)}")
                }
                logger.log("<-- END HTTP (${buffer?.size}-byte body)")
            }
        }
        logger.log("──────────────────────────────────────────────────────────")
        return response
    }

    private fun bodyEncoded(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"]
        return contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true)
    }

    companion object {
        private val DEFAULT_CHARSET = Charsets.UTF_8
        private const val TAG = "HTTP"

        /**
         * Returns true if the body in question probably contains human readable text.
         * Uses a small sample of code points to detect unicode control characters commonly
         * used in binary file signatures.
         */
        fun isPlaintext(buffer: Buffer): Boolean {
            return runCatching {
                val prefix = Buffer()
                val byteCount = if (buffer.size < 64) buffer.size else 64
                buffer.copyTo(prefix, 0, byteCount)
                for (i in 0..15) {
                    if (prefix.exhausted() || i >= prefix.size) {
                        break
                    }
                    val codePoint = prefix.readUtf8CodePoint()
                    if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                        return false
                    }
                }
                true
            }.getOrDefault(false) // Truncated UTF-8 sequence.
        }
    }
}
