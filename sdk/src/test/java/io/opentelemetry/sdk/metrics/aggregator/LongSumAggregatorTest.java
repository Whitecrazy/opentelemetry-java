/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.metrics.aggregator;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Aggregator}. */
@RunWith(JUnit4.class)
public class LongSumAggregatorTest {
  @Test
  public void factoryAggregation() {
    AggregatorFactory factory = LongSumAggregator.getFactory();
    assertThat(factory.getAggregator()).isInstanceOf(Aggregator.class);
  }

  @Test
  public void multipleRecords() {
    Aggregator aggregator = LongSumAggregator.getFactory().getAggregator();
    aggregator.recordLong(12);
    aggregator.recordLong(12);
    aggregator.recordLong(12);
    aggregator.recordLong(12);
    aggregator.recordLong(12);
    assertThat(getPoint(aggregator).getValue()).isEqualTo(12 * 5);
  }

  @Test
  public void multipleRecords_WithNegatives() {
    Aggregator aggregator = LongSumAggregator.getFactory().getAggregator();
    aggregator.recordLong(12);
    aggregator.recordLong(12);
    aggregator.recordLong(-23);
    aggregator.recordLong(12);
    aggregator.recordLong(12);
    aggregator.recordLong(-11);
    assertThat(getPoint(aggregator).getValue()).isEqualTo(14);
  }

  @Test
  public void mergeAndReset_WithNegatives() {
    Aggregator aggregator = LongSumAggregator.getFactory().getAggregator();
    aggregator.recordLong(13);
    aggregator.recordLong(12);
    assertThat(getPoint(aggregator).getValue()).isEqualTo(25);
    Aggregator mergedAggregator = LongSumAggregator.getFactory().getAggregator();
    aggregator.mergeToAndReset(mergedAggregator);
    assertThat(getPoint(aggregator).getValue()).isEqualTo(0);
    assertThat(getPoint(mergedAggregator).getValue()).isEqualTo(25);
    aggregator.recordLong(12);
    aggregator.recordLong(-25);
    aggregator.mergeToAndReset(mergedAggregator);
    assertThat(getPoint(aggregator).getValue()).isEqualTo(0);
    assertThat(getPoint(mergedAggregator).getValue()).isEqualTo(12);
  }

  private static LongPoint getPoint(Aggregator aggregator) {
    Point point = aggregator.toPoint(12345, 12358, Collections.singletonMap("key", "value"));
    assertThat(point).isNotNull();
    assertThat(point.getStartEpochNanos()).isEqualTo(12345);
    assertThat(point.getEpochNanos()).isEqualTo(12358);
    assertThat(point.getLabels()).containsExactly("key", "value");
    assertThat(point).isInstanceOf(LongPoint.class);
    return (LongPoint) point;
  }
}
