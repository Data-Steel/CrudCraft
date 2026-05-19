/*
 * Copyright (c) 2026 CrudCraft contributors
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

package nl.datasteel.crudcraft.benchmarks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;


/**
 * Compares offset-style deep-page traversal with keyset-style cursor traversal.
 *
 * <p>This benchmark intentionally stays in memory so CI can publish stable trend artifacts without
 * provisioning a database. It models the algorithmic cost difference CrudCraft cares about:
 * offset paging must advance through skipped rows, while keyset paging jumps from the last cursor.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 2, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@State(Scope.Thread)
public class KeysetPagingBenchmark {

    @Param({"50"})
    private int pageSize;

    @Param({"1000", "10000"})
    private int offset;

    private List<Row> rows;
    private Row cursor;

    /** Builds deterministic sorted test data before each trial. */
    @Setup(Level.Trial)
    public void setup() {
        rows = new ArrayList<>(100_000);
        for (int i = 0; i < 100_000; i++) {
            rows.add(new Row(i, i));
        }
        cursor = rows.get(offset - 1);
    }

    /**
     * Reads one deep page by advancing through all skipped rows.
     *
     * @param blackhole prevents dead-code elimination
     */
    @Benchmark
    public void offsetPage(Blackhole blackhole) {
        int consumed = 0;
        for (int i = 0; i < offset + pageSize && i < rows.size(); i++) {
            Row row = rows.get(i);
            if (i >= offset) {
                blackhole.consume(row);
                consumed++;
            }
        }
        blackhole.consume(consumed);
    }

    /**
     * Reads one page by locating the cursor boundary and taking the next rows.
     *
     * @param blackhole prevents dead-code elimination
     */
    @Benchmark
    public void keysetPage(Blackhole blackhole) {
        int start = Collections.binarySearch(rows, cursor);
        int consumed = 0;
        for (int i = start + 1; i < start + 1 + pageSize && i < rows.size(); i++) {
            blackhole.consume(rows.get(i));
            consumed++;
        }
        blackhole.consume(consumed);
    }

    private record Row(int sortValue, int id) implements Comparable<Row> {
        @Override
        public int compareTo(Row other) {
            int bySort = Integer.compare(sortValue, other.sortValue);
            return bySort != 0 ? bySort : Integer.compare(id, other.id);
        }
    }
}
