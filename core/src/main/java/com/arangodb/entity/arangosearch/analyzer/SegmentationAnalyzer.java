/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb.entity.arangosearch.analyzer;


import com.arangodb.entity.arangosearch.AnalyzerType;

import java.util.Objects;

/**
 * An Analyzer capable of breaking up the input text into tokens in a language-agnostic manner, making it suitable for
 * mixed language strings.
 * It can optionally preserve all non-whitespace or all characters instead of keeping alphanumeric characters only, as
 * well as apply case conversion.
 *
 * @author Michele Rastelli
 * @see <a href= "https://docs.arangodb.com/stable/index-and-search/analyzers/#segmentation">API Documentation</a>
 * @since ArangoDB 3.9
 */
public final class SegmentationAnalyzer extends SearchAnalyzer {
    private SegmentationAnalyzerProperties properties;

    public SegmentationAnalyzer() {
        setType(AnalyzerType.segmentation);
    }

    public SegmentationAnalyzerProperties getProperties() {
        return properties;
    }

    public void setProperties(SegmentationAnalyzerProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SegmentationAnalyzer that = (SegmentationAnalyzer) o;
        return Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), properties);
    }
}
