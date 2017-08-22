/*
 * Copyright (C) 2017-2017 DataStax Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.oss.driver.api.core.metadata.schema;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.internal.core.metadata.schema.ScriptBuilder;
import java.util.List;

/** A CQL function in the schema metadata. */
public interface FunctionMetadata extends Describable {

  CqlIdentifier getKeyspace();

  FunctionSignature getSignature();

  /**
   * The names of the parameters. This is in the same order as {@code
   * getSignature().getParameterTypes()}
   */
  List<CqlIdentifier> getParameterNames();

  String getBody();

  boolean isCalledOnNullInput();

  String getLanguage();

  DataType getReturnType();

  @Override
  default String describe(boolean pretty) {
    ScriptBuilder builder = new ScriptBuilder(pretty);
    builder
        .append("CREATE FUNCTION ")
        .append(getKeyspace())
        .append(".")
        .append(getSignature().getName())
        .append("(");
    boolean first = true;
    for (int i = 0; i < getSignature().getParameterTypes().size(); i++) {
      if (first) {
        first = false;
      } else {
        builder.append(",");
      }
      DataType type = getSignature().getParameterTypes().get(i);
      CqlIdentifier name = getParameterNames().get(i);
      builder.append(name).append(" ").append(type.asCql(false, pretty));
    }
    return builder
        .append(")")
        .newLine()
        .append(isCalledOnNullInput() ? "CALLED ON NULL INPUT" : "RETURNS NULL ON NULL INPUT")
        .newLine()
        .newLine()
        .append("RETURNS ")
        .append(getReturnType().asCql(false, true))
        .newLine()
        .append("LANGUAGE ")
        .append(getLanguage())
        .newLine()
        .append("AS '")
        .append(getBody())
        .append("';")
        .build();
  }

  @Override
  default String describeWithChildren(boolean pretty) {
    // A function has no children
    return describe(pretty);
  }
}
