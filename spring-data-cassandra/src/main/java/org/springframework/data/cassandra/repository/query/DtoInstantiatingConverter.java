/*
 * Copyright 2016-2017 the original author or authors.
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
package org.springframework.data.cassandra.repository.query;

import java.util.Optional;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.cassandra.mapping.CassandraPersistentEntity;
import org.springframework.data.cassandra.mapping.CassandraPersistentProperty;
import org.springframework.data.convert.EntityInstantiator;
import org.springframework.data.convert.EntityInstantiators;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.PreferredConstructor;
import org.springframework.data.mapping.PreferredConstructor.Parameter;
import org.springframework.data.mapping.SimplePropertyHandler;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.ParameterValueProvider;
import org.springframework.util.Assert;

/**
 * {@link Converter} to instantiate DTOs from fully equipped domain objects.
 *
 * @author Mark Paluch
 */
class DtoInstantiatingConverter implements Converter<Object, Object> {

	private final Class<?> targetType;

	private final MappingContext<? extends PersistentEntity<?, ?>, ? extends PersistentProperty<?>> context;

	private final Optional<EntityInstantiator> instantiator;

	/**
	 * Create a new {@link Converter} to instantiate DTOs.
	 *
	 * @param dtoType must not be {@literal null}.
	 * @param context must not be {@literal null}.
	 * @param instantiators must not be {@literal null}.
	 */
	DtoInstantiatingConverter(Class<?> dtoType,
			MappingContext<? extends CassandraPersistentEntity<?>, CassandraPersistentProperty> context,
			EntityInstantiators instantiator) {

		Assert.notNull(dtoType, "DTO type must not be null!");
		Assert.notNull(context, "MappingContext must not be null!");
		Assert.notNull(instantiator, "EntityInstantiators must not be null!");

		this.targetType = dtoType;
		this.context = context;

		this.instantiator = context.getPersistentEntity(dtoType).map(instantiator::getInstantiatorFor);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.core.convert.converter.Converter#convert(java.lang.Object)
	 */
	@Override
	public Object convert(Object source) {

		if (targetType.isInterface()) {
			return source;
		}

		PersistentEntity<?, ?> sourceEntity = context.getRequiredPersistentEntity(source.getClass());
		PersistentPropertyAccessor sourceAccessor = sourceEntity.getPropertyAccessor(source);
		PersistentEntity<?, ?> targetEntity = context.getRequiredPersistentEntity(targetType);

		EntityInstantiator instantiator = this.instantiator.orElseThrow(
				() -> new IllegalStateException(String.format("No EntityInstantiator for [%s] available", targetType)));

		@SuppressWarnings({ "rawtypes", "unchecked" })
		Object dto = instantiator.createInstance(targetEntity, new ParameterValueProvider() {

			@Override
			public Optional<Object> getParameterValue(Parameter parameter) {

				// TODO: Fix generics
				return parameter.getName()
						.flatMap(name -> sourceAccessor.getProperty(sourceEntity.getRequiredPersistentProperty((String) name)));
			}
		});

		final PersistentPropertyAccessor targetAccessor = targetEntity.getPropertyAccessor(dto);

		Optional<? extends PreferredConstructor<?, ? extends PersistentProperty<?>>> optionalConstructor = targetEntity
				.getPersistenceConstructor();

		targetEntity.doWithProperties((SimplePropertyHandler) property -> {

			if (!optionalConstructor.filter(c -> c.isConstructorParameter(property)).isPresent()) {
				return;
			}

			targetAccessor.setProperty(property,
					sourceAccessor.getProperty(sourceEntity.getRequiredPersistentProperty(property.getName())));
		});

		return dto;
	}
}
