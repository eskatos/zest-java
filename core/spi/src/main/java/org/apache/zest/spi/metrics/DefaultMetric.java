/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.zest.spi.metrics;

import org.apache.zest.api.metrics.MetricsCounter;
import org.apache.zest.api.metrics.MetricsGauge;
import org.apache.zest.api.metrics.MetricsHealthCheck;
import org.apache.zest.api.metrics.MetricsHistogram;
import org.apache.zest.api.metrics.MetricsMeter;
import org.apache.zest.api.metrics.MetricsTimer;

/**
 * Default Metric implementing all supported Metrics as a null object.
 */
public final class DefaultMetric
    implements MetricsGauge, MetricsCounter, MetricsHistogram, MetricsHealthCheck, MetricsMeter, MetricsTimer
{
    public static final DefaultMetric NULL = new DefaultMetric();

    @Override
    public void increment()
    {
    }

    @Override
    public void increment( int steps )
    {
    }

    @Override
    public void decrement()
    {
    }

    @Override
    public void decrement( int steps )
    {
    }

    @Override
    public Context start()
    {
        return () -> {};
    }

    @Override
    public Object value()
    {
        return null;
    }

    @Override
    public void update( long newValue )
    {
    }

    @Override
    public Result check()
        throws Exception
    {
        return new Result( true, "No checks", null );
    }

    @Override
    public void mark()
    {
    }

    @Override
    public void mark( int numberOfEvents )
    {
    }
}
