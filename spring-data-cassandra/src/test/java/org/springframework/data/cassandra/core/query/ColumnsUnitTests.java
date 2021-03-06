/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.data.cassandra.core.query;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.springframework.cassandra.core.cql.CqlIdentifier;

/**
 * Unit tests for {@link Columns}.
 *
 * @author Mark Paluch
 */
public class ColumnsUnitTests {

	@Test // DATACASS-343
	public void shouldCreateEmpty() {

		Columns columns = Columns.empty();

		assertThat(columns.toString()).isEqualTo("*");
	}

	@Test // DATACASS-343
	public void shouldIncludeColumn() {

		Columns columns = Columns.empty().include("foo").include("bar").ttl("baz");

		assertThat(columns.toString()).isEqualTo("foo, bar, TTL(baz)");
	}

	@Test // DATACASS-343
	public void shouldCreateFromColumns() {

		Columns columns = Columns.from("asc", "bar");

		assertThat(columns.toString()).isEqualTo("asc, bar");
	}

	@Test // DATACASS-343
	public void shouldCreateFromCqlIdentifiers() {

		Columns columns = Columns.from(CqlIdentifier.cqlId("Foo", true), CqlIdentifier.cqlId("bar"));

		assertThat(columns.toString()).contains("\"Foo\"").contains("bar");
	}

	@Test // DATACASS-343
	public void shouldBeEquals() {

		Columns columns = Columns.empty().include("foo").include("bar");
		Columns other = Columns.from("foo", "bar");
		Columns differentOrder = Columns.from("bar", "foo");
		Columns withTtl = Columns.empty().include("foo").include("bar").ttl("baz");

		assertThat(columns.equals(other)).isTrue();
		assertThat(columns.equals(differentOrder)).isTrue();
		assertThat(columns.equals(withTtl)).isFalse();
		assertThat(withTtl.equals(columns)).isFalse();

		assertThat(columns.hashCode()).isEqualTo(other.hashCode());
		assertThat(columns.hashCode()).isNotEqualTo(withTtl.hashCode());
	}
}
