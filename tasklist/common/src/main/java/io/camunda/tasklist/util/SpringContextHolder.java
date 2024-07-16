/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.tasklist.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextHolder implements ApplicationContextAware {

  private static ApplicationContext context;

  /**
   * Returns the Spring managed bean instance of the given class type if it exists. Returns null
   * otherwise.
   *
   * @param beanClass
   * @return
   */
  public static <T extends Object> T getBean(Class<T> beanClass) {
    return context.getBean(beanClass);
  }

  /**
   * Retrieves the property value associated with the given key from Spring's application context
   * environment.
   *
   * @param key The key whose associated value is to be retrieved.
   * @return The value associated with the specified key, or null if the key is not found.
   */
  public static String getProperty(String key) {
    return context.getEnvironment().getProperty(key);
  }

  @Override
  public void setApplicationContext(ApplicationContext context) throws BeansException {
    // store ApplicationContext reference to access required beans later on
    SpringContextHolder.context = context;
  }
}
