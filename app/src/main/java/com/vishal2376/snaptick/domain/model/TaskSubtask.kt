package com.vishal2376.snaptick.domain.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class TaskSubtask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val completed: Boolean = false,
)

/** Compact, backwards-safe JSON codec used by Room's string column. */
object TaskSubtaskCodec {
    fun encode(items: List<TaskSubtask>): String {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("title", item.title)
                    .put("completed", item.completed),
            )
        }
        return array.toString()
    }

    fun decode(raw: String): List<TaskSubtask> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val title = item.optString("title").trim()
                    if (title.isBlank()) continue
                    add(
                        TaskSubtask(
                            id = item.optString("id").ifBlank { UUID.randomUUID().toString() },
                            title = title,
                            completed = item.optBoolean("completed", false),
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
    }
}
