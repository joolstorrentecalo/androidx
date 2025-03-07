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
 * Captures the BMR of a user, in kilocalories. Each record represents the number of kilocalories a
 * user would burn if at rest all day, based on their height and weight.
 */
public class BasalMetabolicRateRecord(
    /** Basal metabolic rate, in kilocalories. Required field. Valid range: 0-10000. */
    public val kcalPerDay: Double,
    override val time: Instant,
    override val zoneOffset: ZoneOffset?,
    override val metadata: Metadata = Metadata.EMPTY,
) : InstantaneousRecord {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BasalMetabolicRateRecord) return false

        if (kcalPerDay != other.kcalPerDay) return false
        if (time != other.time) return false
        if (zoneOffset != other.zoneOffset) return false
        if (metadata != other.metadata) return false

        return true
    }

    override fun hashCode(): Int {
        var result = 0
        result = 31 * result + kcalPerDay.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + (zoneOffset?.hashCode() ?: 0)
        result = 31 * result + metadata.hashCode()
        return result
    }

    companion object {
        private const val BASAL_CALORIES_TYPE_NAME = "BasalCaloriesBurned"
        private const val ENERGY_FIELD_NAME = "energy"

        /**
         * Metric identifier to retrieve the total basal calories burned from
         * [androidx.health.connect.client.aggregate.AggregationResult].
         */
        @JvmField
        val BASAL_CALORIES_TOTAL: AggregateMetric<Double> =
            AggregateMetric.doubleMetric(
                BASAL_CALORIES_TYPE_NAME,
                AggregateMetric.AggregationType.TOTAL,
                ENERGY_FIELD_NAME
            )
    }
}
