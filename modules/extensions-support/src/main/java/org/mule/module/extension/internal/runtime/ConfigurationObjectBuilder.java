/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extension.internal.runtime;

import static org.mule.module.extension.internal.util.IntrospectionUtils.getField;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.extension.introspection.Configuration;
import org.mule.extension.introspection.ConfigurationInstantiator;
import org.mule.extension.introspection.Parameter;
import org.mule.module.extension.internal.runtime.resolver.ResolverSet;
import org.mule.module.extension.internal.runtime.resolver.ResolverSetResult;
import org.mule.module.extension.internal.util.GroupValueSetter;
import org.mule.module.extension.internal.util.SingleValueSetter;
import org.mule.module.extension.internal.util.ValueSetter;

import com.google.common.collect.ImmutableList;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Implementation of {@link ObjectBuilder} to create instances that
 * implement a given {@link Configuration}.
 * <p/>
 * The object instances are created through the {@link Configuration#getInstantiator()#instantiateObject()}
 * method. A {@link ResolverSet} is also used to automatically set this builders
 * properties. The name of the properties in the {@link ResolverSet} must match the
 * name of an actual property in the prototype class
 *
 * @since 3.7.0
 */
public final class ConfigurationObjectBuilder extends BaseObjectBuilder<Object>
{

    private final ConfigurationInstantiator configurationInstantiator;
    private final ResolverSet resolverSet;
    private final List<ValueSetter> groupValueSetters;
    private final List<ValueSetter> singleValueSetters;

    public ConfigurationObjectBuilder(Configuration configuration, ResolverSet resolverSet)
    {
        this.configurationInstantiator = configuration.getInstantiator();
        this.resolverSet = resolverSet;

        singleValueSetters = createSingleValueSetters(configuration, resolverSet);
        groupValueSetters = GroupValueSetter.settersFor(configuration);
    }

    private List<ValueSetter> createSingleValueSetters(Configuration configuration, ResolverSet resolverSet)
    {
        ImmutableList.Builder<ValueSetter> singleValueSetters = ImmutableList.builder();
        Class<?> prototypeClass = configuration.getInstantiator().getObjectType();
        for (Parameter parameter : resolverSet.getResolvers().keySet())
        {

            Field field = getField(prototypeClass, parameter);

            // if no field, then it means this is a group attribute
            if (field != null)
            {
                singleValueSetters.add(new SingleValueSetter(parameter, field));
            }
        }

        return singleValueSetters.build();
    }


    @Override
    public Object build(MuleEvent event) throws MuleException
    {
        return build(resolverSet.resolve(event));
    }

    public Object build(ResolverSetResult result) throws MuleException
    {
        Object target = instantiateObject();

        setValues(target, result, groupValueSetters);
        setValues(target, result, singleValueSetters);

        return target;
    }

    private void setValues(Object target, ResolverSetResult result, List<ValueSetter> setters) throws MuleException
    {
        for (ValueSetter setter : setters)
        {
            setter.set(target, result);
        }
    }

    /**
     * Creates a new instance by calling {@link Configuration#getInstantiator()#instantiateObject()}
     * {@inheritDoc}
     */
    @Override
    protected Object instantiateObject()
    {
        return configurationInstantiator.newInstance();
    }
}
