/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.ows;

import java.util.ArrayList;
import java.util.List;
import petascope.util.Pair;

/**
 * Java class for ows:Description elements.
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public class Description {

    private List<String> serviceTitles;
    private List<String> serviceAbstracts;
    private List<KeywordsGroup> serviceKeywords;

    // Constructor
    public Description () {
        serviceTitles      = new ArrayList<String>();
        serviceAbstracts   = new ArrayList<String>();
        serviceKeywords   = new ArrayList<KeywordsGroup>();
    }

    // Getters
    public List<String> getTitles() {
        return serviceTitles.isEmpty() ? new ArrayList<String>() : serviceTitles;
    }
    public void addTitle(String title) {
        serviceTitles.add(title);
    }
    //
    public List<String> getAbstracts() {
        return serviceAbstracts.isEmpty() ? new ArrayList<String>() : serviceAbstracts;
    }
    public void addAbstract(String servAbstract) {
        serviceAbstracts.add(servAbstract);
    }
    //
    public List<KeywordsGroup> getKeywordGroups() {
        return serviceKeywords.isEmpty() ? new ArrayList<KeywordsGroup>() : serviceKeywords;
    }
    public void addKeywordGroup(List<Pair<String,String>> keyAndLang) {
        serviceKeywords.add(new KeywordsGroup(keyAndLang));
    }
    public void addKeywordGroup(List<Pair<String,String>> keyAndLang, String type) {
        serviceKeywords.add(new KeywordsGroup(keyAndLang, type));
    }
    public void addKeywordGroup(List<Pair<String,String>> keyAndLang, String type, String codeSpace) {
        serviceKeywords.add(new KeywordsGroup(keyAndLang, type, codeSpace));
    }

    // Keywords
    /*
    * [http://schemas.opengis.net/ows/2.0/ows19115subset.xsd]
    * Unordered list of one or more commonly used or formalised word(s) or phrase(s) used
    * to describe the subject. When needed, the optional "type" can name the type of the
    * associated list of keywords that shall all have the same type. Also when needed,
    * the codeSpace attribute of that "type" can reference the type name authority and/or
    * thesaurus. If the xml:lang attribute is not included in a Keyword element, then no
    * language is specified for that element unless specified by another means.
    * All Keyword elements in the same Keywords element that share the same xml:lang
    * attribute value represent different keywords in that language.
    */
    public class KeywordsGroup {

        private List<Pair<String,String>> keyValues;
        private String keyType;
        private String keyTypeCodeSpace;

        // Constructors: at least 1 keyword required, to avoid let inconsistent keyword groups
        KeywordsGroup (List<Pair<String,String>> keysAndLangs) {
            keyValues = new ArrayList<Pair<String,String>>();
            for (Pair<String,String> keyAndLang : keysAndLangs) {
                keyValues.add(Pair.of(
                        keyAndLang.fst,
                        ((null == keyAndLang.snd) ? "" : keyAndLang.snd))
                        );
            }
        }
        KeywordsGroup (List<Pair<String,String>> keysAndLangs, String type) {
            this(keysAndLangs);
            keyType = type;
        }
        KeywordsGroup (List<Pair<String,String>> keysAndLangs, String type, String codeSpace) {
            this(keysAndLangs, type);
            keyTypeCodeSpace = codeSpace;
        }

        // Getters/Setters
        public List<Pair<String,String>> getValues() {
            return keyValues;
        }
        public void addValue(String newKey) {
            keyValues.add(Pair.of(newKey, ""));
        }
        public void addValue(String newKey, String lang) {
            keyValues.add(Pair.of(newKey, lang));
        }
        //
        public String getType() {
            return (null == keyType) ? "" : keyType;
        }
        public String getTypeCodeSpace() {
            return (null == keyTypeCodeSpace) ? "" : keyTypeCodeSpace;
        }
    } //~ Descrition.Keyword

    // Methods
    public boolean isEmpty() {
        return serviceTitles.isEmpty() &&
                serviceAbstracts.isEmpty() &&
                serviceKeywords.isEmpty();
    }
} //~ Description
