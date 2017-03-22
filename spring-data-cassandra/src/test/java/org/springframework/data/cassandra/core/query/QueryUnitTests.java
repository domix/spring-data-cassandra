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
import org.springframework.data.domain.Sort;

/**
 * Unit tests for {@link Query}.
 *
 * @author Mark Paluch
 */
public class QueryUnitTests {

	@Test // DATACASS-343
	public void shouldCreateFromChainedCriteria() {

		Query query = Query.from(ChainedCriteria.where("userId").is("foo").and("userComment").is("bar"));

		assertThat(query).hasSize(2);
		assertThat(query.getCriteriaDefinitions()).contains(Criteria.where("userId").is("foo"));
		assertThat(query.getCriteriaDefinitions()).contains(Criteria.where("userComment").is("bar"));
	}

	@Test // DATACASS-343
	public void shouldRepresentQueryToString() {

		Query query = Query.from(ChainedCriteria.where("userId").is("foo").and("userComment").is("bar"))
				.with(new Sort("foo", "bar"));
		query.with(Columns.from("foo").ttl("bar"));
		query.limit(5);

		assertThat(query.toString()).isEqualTo(
				"Query: userId = 'foo' AND userComment = 'bar', Columns: foo, TTL(bar), Sort: foo: ASC,bar: ASC, Limit: 5");
	}

	@Test // DATACASS-343
	public void shouldConfigureQueryObject() {

		Query query = Query.from(Criteria.where("foo").is("bar"));
		Sort sort = new Sort("a", "b");
		Columns columns = Columns.from("a", "b");

		query.with(sort).with(columns).limit(10).withAllowFiltering();

		assertThat(query).hasSize(1);
		assertThat(query.getColumns()).isEqualTo(columns);
		assertThat(query.getSort()).isEqualTo(sort);
		assertThat(query.getLimit()).isEqualTo(10);
		assertThat(query.isAllowFiltering()).isTrue();
	}
}