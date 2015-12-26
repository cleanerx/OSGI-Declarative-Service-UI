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
import org.osgi.dto.DTO;

/**
 *
 */
public class ServiceComponentComparator implements Comparator<Object> {

  /**
   *
   */
  public ServiceComponentComparator() {
  }

  /**
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  @Override
  public int compare(Object ob1, Object ob2) {
	  if(ob1 instanceof Component && ob2 instanceof Component) {
		  Component o2 = (Component) ob2;
		Component o1 = (Component) ob1;
		int compareTo = o1.getName().compareTo(o2.getName());
		  if (compareTo == 0) {
			  return (int) (o1.getId() - o2.getId());
		  }
		  return compareTo;
	  }
	  if(ob1 instanceof String && ob2 instanceof DTO) {
		  return -1;
	  } else if(ob2 instanceof String && ob1 instanceof DTO) {
		  return 0;
	  } else if(ob1 instanceof String && ob2 instanceof String) {
		String string2 = (String) ob2;
		String string = (String) ob1;
		  return string.compareTo(string2);
	  } else if(ob1 instanceof DTO && ob2 instanceof DTO) {
		DTO dto = (DTO) ob1;
		DTO dto2 = (DTO) ob2;
	  }
	  return 0;
  }

}
