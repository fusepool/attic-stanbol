/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.stanbol.entityhub.yard.solr.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.stanbol.commons.solr.utils.SolrUtil;
import org.apache.stanbol.entityhub.yard.solr.defaults.IndexDataTypeEnum;
import org.apache.stanbol.entityhub.yard.solr.model.IndexValue;
import org.apache.stanbol.entityhub.yard.solr.model.IndexValueFactory;

public final class QueryUtils {
    private QueryUtils() {}

    /**
     * This method encodes a parsed index value as needed for queries.
     * <p>
     * In case of TXT it is assumed that a whitespace tokenizer is used by the index. Therefore values with
     * multiple words need to be treated and connected with AND to find only values that contain all. In case
     * of STR no whitespace is assumed. Therefore spaces need to be replaced with '+' to search for tokens
     * with the exact name. In all other cases the string need not to be converted.
     * 
     * Note also that text queries are converted to lower case
     * 
     * @param value
     *            the index value
     * @return the (possible multiple) values that need to be connected with AND
     */
    public static String[] encodeQueryValue(IndexValue indexValue, boolean escape) {
        if (indexValue == null) {
            return null;
        }
        String[] queryConstraints;
        String value = indexValue.getValue(); 
        if (escape) {
            value = SolrUtil.escapeSolrSpecialChars(value);
        }
        if (IndexDataTypeEnum.TXT.getIndexType().equals(indexValue.getType())) {
        	value = value.toLowerCase();
            Collection<String> tokens = new HashSet<String>(
                    Arrays.asList(value.split(" ")));
            tokens.remove("");
            queryConstraints = tokens.toArray(new String[tokens.size()]);
        } else if (IndexDataTypeEnum.STR.getIndexType().equals(indexValue.getType())) {
            value = value.toLowerCase();
            queryConstraints = new String[] {value.replace(' ', '+')};
        } else {
            queryConstraints = new String[] {value};
        }
        return queryConstraints;
    }
    
    /**
     * Utility Method that extracts IndexValues form an parsed {@link Object}.
     * This checks for {@link IndexValue}, {@link Iterable}s and values
     * @param indexValueFactory The indexValueFactory used to create indexValues if necessary
     * @param value the value to parse
     * @return A set with the parsed values. The returned Set is guaranteed 
     * not to be <code>null</code> and contains at least a single element. 
     * If no IndexValue could be parsed from the parsed value than a set containing
     * the <code>null</code> value is returned.
     */
    public static Set<IndexValue> parseIndexValues(IndexValueFactory indexValueFactory,Object value) {
        Set<IndexValue> indexValues;
        if (value == null) {
            indexValues = Collections.singleton(null);
        } else if (value instanceof IndexValue) {
            indexValues = Collections.singleton((IndexValue) value);
        } else if (value instanceof Iterable<?>){
            indexValues = new HashSet<IndexValue>();
            for(Object o : (Iterable<?>) value){
                if(o instanceof IndexValue){
                    indexValues.add((IndexValue)o);
                } else if (o != null){
                    indexValues.add(indexValueFactory.createIndexValue(o));
                }
            }
            if(indexValues.isEmpty()){
                indexValues.add(null); //add null element instead of an empty set
            }
        } else {
            indexValues = Collections.singleton(indexValueFactory.createIndexValue(value));
        }
        return indexValues;
    }
}