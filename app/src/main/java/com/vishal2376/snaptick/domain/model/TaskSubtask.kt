package com.vishal2376.snaptick.domain.model

import java.util.UUID

/**
 * Calendar-owned subtask value object.
 *
 * It intentionally has no Android/Room dependency so it can be serialized by the
 * repository that already persists task metadata.
 */
data class TaskSubtask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val completed: Boolean = false,
)
