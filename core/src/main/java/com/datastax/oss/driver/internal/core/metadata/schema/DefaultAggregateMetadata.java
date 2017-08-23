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
package com.datastax.oss.driver.internal.core.metadata.schema;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.metadata.schema.AggregateMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.FunctionSignature;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultAggregateMetadata implements AggregateMetadata {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultAggregateMetadata.class);

  private final CqlIdentifier keyspace;
  private final FunctionSignature signature;
  private final FunctionSignature finalFuncSignature;
  private final Object initCond;
  private final DataType returnType;
  private final FunctionSignature stateFuncSignature;
  private final DataType stateType;
  private final TypeCodec<Object> stateTypeCodec;

  public DefaultAggregateMetadata(
      CqlIdentifier keyspace,
      FunctionSignature signature,
      FunctionSignature finalFuncSignature,
      Object initCond,
      DataType returnType,
      FunctionSignature stateFuncSignature,
      DataType stateType,
      TypeCodec<Object> stateTypeCodec) {
    this.keyspace = keyspace;
    this.signature = signature;
    this.finalFuncSignature = finalFuncSignature;
    this.initCond = initCond;
    this.returnType = returnType;
    this.stateFuncSignature = stateFuncSignature;
    this.stateType = stateType;
    this.stateTypeCodec = stateTypeCodec;
  }

  @Override
  public CqlIdentifier getKeyspace() {
    return keyspace;
  }

  @Override
  public FunctionSignature getSignature() {
    return signature;
  }

  @Override
  public FunctionSignature getFinalFuncSignature() {
    return finalFuncSignature;
  }

  @Override
  public Object getInitCond() {
    return initCond;
  }

  @Override
  public DataType getReturnType() {
    return returnType;
  }

  @Override
  public FunctionSignature getStateFuncSignature() {
    return stateFuncSignature;
  }

  @Override
  public DataType getStateType() {
    return null;
  }

  @Override
  public String describe(boolean pretty) {
    ScriptBuilder builder = new ScriptBuilder(pretty);
    builder
        .append("CREATE AGGREGATE ")
        .append(keyspace)
        .append(".")
        .append(signature.getName())
        .append("(");
    boolean first = true;
    for (int i = 0; i < signature.getParameterTypes().size(); i++) {
      if (first) {
        first = false;
      } else {
        builder.append(",");
      }
      DataType type = signature.getParameterTypes().get(i);
      builder.append(type.asCql(false, pretty));
    }
    builder
        .append(")")
        .newLine()
        .append("SFUNC ")
        .append(stateFuncSignature.getName())
        .newLine()
        .append("STYPE ")
        .append(stateType.asCql(false, pretty));

    if (finalFuncSignature != null) {
      builder.newLine().append("FINALFUNC ").append(finalFuncSignature.getName());
    }
    if (initCond != null) {
      builder.append("INITCOND ").append(formatInitCond());
    }
    return builder.append(";").build();
  }

  private String formatInitCond() {
    try {
      return stateTypeCodec.format(initCond);
    } catch (Throwable t) {
      LOG.warn(
          String.format(
              "Failed to format INITCOND for %s.%s, using toString instead",
              keyspace.asInternal(), signature.getName().asInternal()));
      return initCond.toString();
    }
  }
}
