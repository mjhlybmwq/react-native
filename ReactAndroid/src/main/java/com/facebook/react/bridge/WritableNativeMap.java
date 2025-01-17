/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.react.bridge;

import com.facebook.infer.annotation.Assertions;
import com.facebook.proguard.annotations.DoNotStrip;
import com.facebook.soloader.SoLoader;

import java.lang.Object;
import java.lang.reflect.*;

/**
 * Implementation of a write-only map stored in native memory. Use
 * {@link Arguments#createMap()} if you need to stub out creating this class in a test.
 * TODO(5815532): Check if consumed on read
 */
@DoNotStrip
public class WritableNativeMap extends ReadableNativeMap implements WritableMap {

  static {
    SoLoader.loadLibrary(ReactBridge.REACT_NATIVE_LIB);
  }

  @Override
  public native void putBoolean(String key, boolean value);
  @Override
  public native void putDouble(String key, double value);
  @Override
  public native void putInt(String key, int value);
  @Override
  public native void putString(String key, String value);
  @Override
  public native void putNull(String key);

  // Note: this consumes the map so do not reuse it.
  @Override
  public void putMap(String key, WritableMap value) {
    Assertions.assertCondition(
        value == null || value instanceof WritableNativeMap, "Illegal type provided");
    putNativeMap(key, (WritableNativeMap) value);
  }

  // Note: this consumes the map so do not reuse it.
  @Override
  public void putArray(String key, WritableArray value) {
    Assertions.assertCondition(
        value == null || value instanceof WritableNativeArray, "Illegal type provided");
    putNativeArray(key, (WritableNativeArray) value);
  }

  // Note: this **DOES NOT** consume the source map
  @Override
  public void merge(ReadableMap source) {
    Assertions.assertCondition(source instanceof ReadableNativeMap, "Illegal type provided");
    mergeNativeMap((ReadableNativeMap) source);
  }

  static public WritableNativeMap fromObject(Object object) {
    WritableMap writableMap = new WritableMap();
    Field[] fields = object.getClass().getDeclaredFields();
    for(int fieldIndex = 0; fieldIndex < fields.length; fieldIndex++) {
      Field field = fields[fieldIndex];
      field.setAccessible(true);

      //put the value depends on the type of the field
      String fieldType = field.getGenericType().toString();
      
      //String
      if(fieldType.equals("class.java.lang.String")) {
        String value = (String) field.get(object);
        writableMap.putString(field.getName(), value);
      }

      //Integer
      if(fieldType.equals("class.java.lang.Integer")) {
        Integer value = (Integer) field.get(object);
        writableMap.putInt(field.getName(), value);
      }

      //Double
      if(fieldType.equals("class.java.lang.Double")) {
        Double value = (Double) field.get(object);
        writableMap.putDouble(field.getName(), value);
      }

      //Boolean
      if(fieldType.equals("class.java.lang.Boolean")) {
        Boolean value = (Boolean) field.get(object);
        writableMap.putBoolean(field.getName(), value);
      }
    }
    return writableMap;
  }

  private native void putNativeMap(String key, WritableNativeMap value);
  private native void putNativeArray(String key, WritableNativeArray value);
  private native void mergeNativeMap(ReadableNativeMap source);
}
