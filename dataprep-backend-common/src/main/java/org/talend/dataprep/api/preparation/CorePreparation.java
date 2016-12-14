/*
 * // ============================================================================
 * // Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * //
 * // This source code is available under agreement available at
 * // https://github.com/Talend/data-prep/blob/master/LICENSE
 * //
 * // You should have received a copy of the agreement
 * // along with this program; if not, write to Talend SA
 * // 9 rue Pages 92150 Suresnes, France
 * //
 * // ============================================================================
 */

package org.talend.dataprep.api.preparation;

import java.util.List;

import org.talend.dataprep.api.dataset.RowMetadata;

public class CorePreparation {

    private RowMetadata rowMetadata;

    private List<Action> actions;

    public CorePreparation() {
    }

    public CorePreparation(RowMetadata rowMetadata, List<Action> actions) {
        this.actions = actions;
        this.rowMetadata = rowMetadata;
    }

    public List<Action> getActions() {
        return actions;
    }

    public RowMetadata getRowMetadata() {
        return rowMetadata;
    }
}
