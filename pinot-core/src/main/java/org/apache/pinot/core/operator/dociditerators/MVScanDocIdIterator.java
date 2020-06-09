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
package org.apache.pinot.core.operator.dociditerators;

import org.apache.pinot.core.common.Constants;
import org.apache.pinot.core.operator.docvalsets.MultiValueSet;
import org.apache.pinot.core.operator.filter.predicate.PredicateEvaluator;
import org.roaringbitmap.IntIterator;
import org.roaringbitmap.buffer.ImmutableRoaringBitmap;
import org.roaringbitmap.buffer.MutableRoaringBitmap;


/**
 * The {@code MVScanDocIdIterator} is the scan-based iterator for MVScanDocIdSet to scan a multi-value column for the
 * matching document ids.
 */
public final class MVScanDocIdIterator implements ScanBasedDocIdIterator {
  private final PredicateEvaluator _predicateEvaluator;
  private final MultiValueSet _valueSet;
  private final int _numDocs;
  private final int[] _dictIdBuffer;

  private int _nextDocId = 0;
  private long _numEntriesScanned = 0L;

  public MVScanDocIdIterator(PredicateEvaluator predicateEvaluator, MultiValueSet valueSet, int numDocs,
      int maxNumEntriesPerValue) {
    _predicateEvaluator = predicateEvaluator;
    _valueSet = valueSet;
    _numDocs = numDocs;
    _dictIdBuffer = new int[maxNumEntriesPerValue];
  }

  @Override
  public int next() {
    while (_nextDocId < _numDocs) {
      int nextDocId = _nextDocId++;
      int length = _valueSet.getIntValues(nextDocId, _dictIdBuffer);
      _numEntriesScanned += length;
      if (_predicateEvaluator.applyMV(_dictIdBuffer, length)) {
        return nextDocId;
      }
    }
    return Constants.EOF;
  }

  @Override
  public int advance(int targetDocId) {
    _nextDocId = targetDocId;
    return next();
  }

  @Override
  public MutableRoaringBitmap applyAnd(ImmutableRoaringBitmap docIds) {
    MutableRoaringBitmap result = new MutableRoaringBitmap();
    IntIterator docIdIterator = docIds.getIntIterator();
    int nextDocId;
    while (docIdIterator.hasNext() && (nextDocId = docIdIterator.next()) < _numDocs) {
      int length = _valueSet.getIntValues(nextDocId, _dictIdBuffer);
      _numEntriesScanned += length;
      if (_predicateEvaluator.applyMV(_dictIdBuffer, length)) {
        result.add(nextDocId);
      }
    }
    return result;
  }

  @Override
  public long getNumEntriesScanned() {
    return _numEntriesScanned;
  }
}
