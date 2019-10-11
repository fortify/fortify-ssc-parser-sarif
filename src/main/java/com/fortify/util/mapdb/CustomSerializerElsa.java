/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.util.mapdb;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;
import org.mapdb.elsa.ElsaMaker;
import org.mapdb.elsa.ElsaSerializer;
import org.mapdb.serializer.GroupSerializerObjectArray;

/**
 * This class wraps an {@link ElsaSerializer} instance as a MapDb {@link Serializer}.
 * The constructor takes a set of {@link Class} objects to be added to the 
 * {@link ElsaSerializer} class catalog for optimized (de-)serialization.
 * 
 * @author Ruud Senden
 *
 * @param <T>
 */
public class CustomSerializerElsa<T> extends GroupSerializerObjectArray<T> {
	private final ElsaSerializer ser;
	
	/**
	 * This constructor instantiates an {@link ElsaSerializer} instance,
	 * registering the given set of {@link Class} instances to the
	 * {@link ElsaSerializer} class catalog.
	 *  
	 * @param classes
	 */
	public CustomSerializerElsa(Class<T> clazz) {
		this.ser = new ElsaMaker()
			.registerClasses(getReferencedClasses(clazz))
			.unknownClassNotification(unknownClazz->System.out.println("Unknown class: "+unknownClazz))
			.make();
	}

	/**
	 * Serialize the given value using our {@link ElsaSerializer} instance
	 */
	@Override
	public void serialize(DataOutput2 out, T value) throws IOException {
		ser.serialize(out, value);
	}

	/**
	 * De-serialize the given input using our {@link ElsaSerializer} instance
	 */
	@Override
	public T deserialize(DataInput2 input, int available) throws IOException {
		return ser.deserialize(input);
	}
	
	private static final Class<?>[] getReferencedClasses(Class<?> clazz) {
		Set<Class<?>> result = new HashSet<>();
		addReferencedClasses(result, clazz);
		return result.toArray(new Class<?>[] {});
	}

	private static final void addReferencedClasses(Set<Class<?>> result, Class<?> clazz) {
		if ( clazz!=null && !Object.class.equals(clazz) ) {
			result.add(clazz);
			addDeclaredFieldClasses(result, clazz);
			addReferencedClasses(result, clazz.getSuperclass());
		}
	}

	private static void addDeclaredFieldClasses(Set<Class<?>> result, Class<?> clazz) {
		for (Field field : clazz.getDeclaredFields()) {
			Class<?> type = field.getType();
			if ( !result.contains(type) ) {
				addReferencedClasses(result, type);
			}
		}
	}
}
