/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pinot.core.operator.transform.function;

import org.apache.pinot.common.function.TransformFunctionType;


/**
 * The <code>EqualsTransformFunction</code> extends <code>BinaryOperatorTransformFunction</code> to implement the
 * binary operator(=).
 *
 * The results are in boolean format and stored as an integer array with 1 represents true and 0 represents false.
 *
 * SQL Syntax:
 *    columnA = 12
 *    columnA = 12.0
 *    columnA = 'fooBar'
 *
 * Sample Usage:
 *    EQUALS(columnA, 12)
 *    EQUALS(columnA, 12.0)
 *    EQUALS(columnA, 'fooBar')
 *
 */
public class EqualsTransformFunction extends BinaryOperatorTransformFunction {

  @Override
  public String getName() {
    return TransformFunctionType.EQUALS.getName();
  }

  @Override
  int getBinaryFuncResult(int result) {
    return (result == 0) ? 1 : 0;
  }
}
