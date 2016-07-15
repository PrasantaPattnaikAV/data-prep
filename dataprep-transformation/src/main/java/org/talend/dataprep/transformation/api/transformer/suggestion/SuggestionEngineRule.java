//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.api.transformer.suggestion;

import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.transformation.actions.common.ActionMetadata;
import org.talend.dataprep.transformation.actions.common.SuggestionLevel;

import java.util.function.BiFunction;

public interface SuggestionEngineRule extends BiFunction<ActionMetadata, ColumnMetadata, SuggestionLevel> {

}
