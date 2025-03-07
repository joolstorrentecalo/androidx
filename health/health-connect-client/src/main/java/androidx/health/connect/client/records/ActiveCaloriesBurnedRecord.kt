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

import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.metadata.Metadata
import java.time.Instant
import java.time.ZoneOffset

/**
 * Captures the estimated active energy burned by the user (in kilocalories), excluding basal
 * metabolic rate (BMR). Each record represents the total kilocalories burned over a time interval,
 * so both the start and end times should be set.
 */
public class ActiveCaloriesBurnedRecord(
    /** Energy in kilocalories. Required field. Valid range: 0-1000000. */
    public val energyKcal: Double,
    override val startTime: Instant,
    override val startZoneOffset: ZoneOffset?,
    override val endTime: Instant,
    override val endZoneOffset: ZoneOffset?,
    override val metadata: Metadata = Metadata.EMPTY,
) : IntervalRecord {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ActiveCaloriesBurnedRecord) return false

        if (energyKcal != other.energyKcal) return false
        if (startTime != other.startTime) return false
        if (startZoneOffset != other.startZoneOffset) return false
        if (endTime != other.endTime) return false
        if (endZoneOffset != other.endZoneOffset) return false
        if (metadata != other.metadata) return false

        return true
    }

    override fun hashCode(): Int {
        var result = 0
        result = 31 * result + energyKcal.hashCode()
        result = 31 * result + (startZoneOffset?.hashCode() ?: 0)
        result = 31 * result + endTime.hashCode()
        result = 31 * result + (endZoneOffset?.hashCode() ?: 0)
        result = 31 * result + metadata.hashCode()
        return result
    }

    companion object {
        private const val TYPE_NAME = "ActiveCaloriesBurned"
        private const val ENERGY_FIELD_NAME = "energy"

        /**
         * Metric identifier to retrieve total active calories burned from
         * [androidx.health.connect.client.aggregate.AggregationResult].
         */
        @JvmField
        val ACTIVE_CALORIES_TOTAL: AggregateMetric<Double> =
            AggregateMetric.doubleMetric(
                TYPE_NAME,
                AggregateMetric.AggregationType.TOTAL,
                ENERGY_FIELD_NAME
            )
    }
}
