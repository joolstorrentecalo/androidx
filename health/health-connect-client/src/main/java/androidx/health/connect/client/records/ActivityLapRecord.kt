/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.health.connect.client.records

import androidx.health.connect.client.metadata.Metadata
import java.time.Instant
import java.time.ZoneOffset

/**
 * Captures the time of a lap within an activity. A lap is explicitly marked segment within an
 * activity session (such as pool length while swimming or a track lap while running). Each data
 * point represents the start / stop time for an event.
 */
public class ActivityLapRecord(
    /** Length of the lap, in meters. Optional field. Valid range: 0-1000000. */
    public val lengthMeters: Double = 0.0,
    override val startTime: Instant,
    override val startZoneOffset: ZoneOffset?,
    override val endTime: Instant,
    override val endZoneOffset: ZoneOffset?,
    override val metadata: Metadata = Metadata.EMPTY,
) : IntervalRecord {
    init {
        requireNonNegative(value = lengthMeters, name = "lengthMeters")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ActivityLapRecord) return false

        if (lengthMeters != other.lengthMeters) return false
        if (startTime != other.startTime) return false
        if (startZoneOffset != other.startZoneOffset) return false
        if (endTime != other.endTime) return false
        if (endZoneOffset != other.endZoneOffset) return false
        if (metadata != other.metadata) return false

        return true
    }

    override fun hashCode(): Int {
        var result = 0
        result = 31 * result + lengthMeters.hashCode()
        result = 31 * result + (startZoneOffset?.hashCode() ?: 0)
        result = 31 * result + endTime.hashCode()
        result = 31 * result + (endZoneOffset?.hashCode() ?: 0)
        result = 31 * result + metadata.hashCode()
        return result
    }
}
