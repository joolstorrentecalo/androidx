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
package androidx.health.connect.client.impl.converters.aggregate

import androidx.health.connect.client.records.ActivitySessionRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.platform.client.proto.RequestProto
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AggregateMetricConverterTest {
    @Test
    fun aggregateMetric_toProto() {
        assertThat(StepsRecord.COUNT_TOTAL.toProto())
            .isEqualTo(
                RequestProto.AggregateMetricSpec.newBuilder()
                    .setDataTypeName("Steps")
                    .setAggregationType("total")
                    .setFieldName("count")
                    .build()
            )
        assertThat(DistanceRecord.DISTANCE_TOTAL.toProto())
            .isEqualTo(
                RequestProto.AggregateMetricSpec.newBuilder()
                    .setDataTypeName("Distance")
                    .setAggregationType("total")
                    .setFieldName("distance")
                    .build()
            )
        assertThat(ActivitySessionRecord.ACTIVE_TIME_TOTAL.toProto())
            .isEqualTo(
                RequestProto.AggregateMetricSpec.newBuilder()
                    .setDataTypeName("ActiveTime")
                    .setAggregationType("total")
                    .setFieldName("time")
                    .build()
            )
        assertThat(HeartRateRecord.MEASUREMENTS_COUNT.toProto())
            .isEqualTo(
                RequestProto.AggregateMetricSpec.newBuilder()
                    .setDataTypeName("HeartRate")
                    .setAggregationType("count")
                    .build()
            )
    }
}
