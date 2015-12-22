/*
 * Copyright (c) Jens Kuebler (2015). All Rights Reserved.
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
package org.osgi.ds.ui;

import java.util.Comparator;

import org.apache.felix.scr.Component;

/**
 *
 */
public class ServiceComponentComparator implements Comparator<Component> {

  /**
   *
   */
  public ServiceComponentComparator() {
  }

  /**
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  @Override
  public int compare(Component o1, Component o2) {
    int compareTo = o1.getName().compareTo(o2.getName());
    if (compareTo == 0) {
      return (int) (o1.getId() - o2.getId());
    }
    return compareTo;
  }

}
