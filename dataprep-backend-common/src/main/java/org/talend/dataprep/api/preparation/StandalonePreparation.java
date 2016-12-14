// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.api.preparation;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataquality.semantic.broadcast.BroadcastDocumentObject;

import java.util.List;
import java.util.Map;

public class StandalonePreparation extends CorePreparation {



    private List<BroadcastDocumentObject> dictionary;

    private List<BroadcastDocumentObject> keyword;

    private Map<String, Map<String, DataSetRow>> lookupDataSets;

    public StandalonePreparation() {
        super();
    }

    public StandalonePreparation(RowMetadata rowMetadata, List<Action> actions) {
        super(rowMetadata, actions);
    }

    public StandalonePreparation(RowMetadata rowMetadata, List<Action> actions, List<BroadcastDocumentObject> dictionary,
            List<BroadcastDocumentObject> keyword, Map<String, Map<String, DataSetRow>> lookupDataSets) {
        super(rowMetadata, actions);
        this.dictionary = dictionary;
        this.keyword = keyword;
        this.lookupDataSets = lookupDataSets;
    }

    public List<BroadcastDocumentObject> getDictionary() {
        return dictionary;
    }

    public void setDictionary(List<BroadcastDocumentObject> dictionary) {
        this.dictionary = dictionary;
    }

    public List<BroadcastDocumentObject> getKeyword() {
        return keyword;
    }

    public void setKeyword(List<BroadcastDocumentObject> keyword) {
        this.keyword = keyword;
    }

    public Map<String, Map<String, DataSetRow>> getLookupDataSets() {
        return lookupDataSets;
    }

    public void setLookupDataSets(Map<String, Map<String, DataSetRow>> lookupDataSets) {
        this.lookupDataSets = lookupDataSets;
    }
}