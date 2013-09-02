package net.eusashead.spring.gaecache;

/*
 * #[license]
 * spring-cache-gae
 * %%
 * Copyright (C) 2013 Eusa's Head
 * %%
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
 * %[license]
 */

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.util.Assert;

/**
 * A {@link KeyGenerator} for
 * extracting {@link String} keys
 * from method arguments that will
 * be safe to use with {@link GaeCache}
 * 
 * Note that all objects used as keys 
 * (e.g. passed as parameters to @Cacheable
 * methods) should override {@link Object}.toString()
 * in a way that produces a unique {@link String} 
 * based on the object's state (e.g. its fields)
 * consistent with equals (equal objects should produce
 * equal strings).
 * 
 * If a key object does not have a suitable toString() method
 * and one cannot be supplied (it is a 3rd party class, for instance)
 * consider using a {@link KeyGeneratorStrategy} for the type
 * registered with this {@link KeyGenerator} using the
 * registerStrategy() method.
 * 
 * @author patrickvk
 *
 */
public class GaeCacheKeyGenerator implements KeyGenerator {
	
	/**
	 * Used if no {@link KeyGeneratorStrategy} has been registered for a given type
	 */
	private final DefaultKeyGeneratorStrategy defaultKeyStrategy = new DefaultKeyGeneratorStrategy();
	
	/**
	 * Registered {@link KeyGeneratorStrategy} for types
	 */
	private final Map<Class<?>, KeyGeneratorStrategy<?>> strategies = new HashMap<>();

	/* (non-Javadoc)
	 * @see org.springframework.cache.interceptor.KeyGenerator#generate(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object generate(Object target, Method method, Object... args) {
		StringBuilder compoundKey = new StringBuilder();
		//compoundKey.append("<key<params<");
		for (int i=0;i<args.length;i++) {
			//compoundKey.append("<p");
			//compoundKey.append(i);
			//compoundKey.append("=");
			Object obj = args[i];
			if (obj == null) {
				compoundKey.append("");
			} else {
				compoundKey.append(getKey(obj));
			}
			//compoundKey.append(">");
			if (i < args.length - 1) {
				compoundKey.append(",");
			}
		}
		//compoundKey.append(">>>");
		return compoundKey.toString();
	}
	
	/**
	 * Get a key for the supplied
	 * object using either a registered
	 * {@link KeyGeneratorStrategy} or
	 * the default if none match
	 * 
	 * @param object {@link Object} from which the key is to be generated
	 * @return {@link String} generated by a {@link KeyGeneratorStrategy}
	 */
	private String getKey(Object object) {
		Class<?> type = object.getClass();
		KeyGeneratorStrategy<?> strat = strategies.get(type);
		if (strat != null) {
			return strat.getKey(object);
		}
		return defaultKeyStrategy.getKey(object);
	}
	
	/**
	 * Register a {@link KeyGeneratorStrategy} for 
	 * the type that it is designed to extract 
	 * keys for.
	 * 
	 * @param type {@link Class} the type that the strategy is for
	 * @param strategy the {@link KeyGeneratorStrategy} for the type
	 */
	public <T> void registerStrategy(Class<T> type, KeyGeneratorStrategy<T> strategy) {
		Assert.notNull(type);
		Assert.notNull(strategy);
		strategies.put(type, strategy);
	}

}
