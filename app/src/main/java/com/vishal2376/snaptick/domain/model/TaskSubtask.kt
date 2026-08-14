package com.vishal2376.snaptick.domain.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

data class TaskSubtask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val completed: Boolean = false,
)

object TaskSubtaskCodec {
    private val gson = Gson()
    private val listType = object : TypeToken<List<TaskSubtask>>() {}.type

    fun decode(json: String): List<TaskSubtask> = runCatching {
        if (json.isBlank()) emptyList() else gson.fromJson<List<TaskSubtask>>(json, listType).orEmpty()
    }.getOrDefault(emptyList())

    fun encode(items: List<TaskSubtask>): String = gson.toJson(items)
}
