package me.bubner.zeusmonitor.timer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream


object HistorySerializer : Serializer<History> {
    override val defaultValue: History = History(emptyList())
    override suspend fun readFrom(input: InputStream): History =
        try {
            Json.decodeFromString<History>(
                input.readBytes().decodeToString()
            )
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read History", serialization)
        }

    override suspend fun writeTo(t: History, output: OutputStream) {
        output.write(
            Json.encodeToString(t)
                .encodeToByteArray()
        )
    }
}
