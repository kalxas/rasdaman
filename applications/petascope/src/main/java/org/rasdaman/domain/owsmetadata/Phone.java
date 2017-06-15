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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.domain.owsmetadata;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

/**
 *
 * Example:
 *
 * <Phone>
 * <Voice>+1 301 555-1212</Voice>
 * <Facsimile>+1 301 555-1212</Facsimile>
 * </Phone>
 *
 @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Entity
@Table(name = Phone.TABLE_NAME)
public class Phone {
    
    public static final String TABLE_NAME = "phone";
    public static final String COLUMN_ID = TABLE_NAME + "_id";
    
    public static final String DEFAULT_VOICE_PHONE = "+49 421 20040";

    @Id
    @Column (name = COLUMN_ID)
    @GeneratedValue(strategy = GenerationType.TABLE)
    private long id;

    @ElementCollection(fetch = FetchType.EAGER)        
    @OrderColumn
    // Zero or more, optional
    private List<String> voicePhones = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)        
    @OrderColumn
    // Zero or more, optional
    private List<String> facsimilePhones = new ArrayList<>();

    public Phone() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<String> getVoicePhones() {
        return voicePhones;
    }

    public void setVoicePhones(List<String> voicePhones) {
        this.voicePhones = voicePhones;
    }

    public List<String> getFacsimilePhones() {
        return facsimilePhones;
    }

    public void setFacsimilePhone(List<String> facsimilePhones) {
        this.facsimilePhones = facsimilePhones;
    }

}
