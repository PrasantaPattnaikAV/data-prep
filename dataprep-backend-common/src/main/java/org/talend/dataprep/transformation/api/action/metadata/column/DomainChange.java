package org.talend.dataprep.transformation.api.action.metadata.column;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.SchemaChangeAction;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;

/**
 * Change the domain of a column. <b>This action is not displayed in the UI it's here to ease recording it as a Step
 * It's available from column headers</b>
 */
@Component(DomainChange.ACTION_BEAN_PREFIX + DomainChange.DOMAIN_CHANGE_ACTION_NAME)
public class DomainChange extends AbstractActionMetadata implements ColumnAction, SchemaChangeAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainChange.class);

    /**
     * The action name.
     */
    public static final String DOMAIN_CHANGE_ACTION_NAME = "domain_change"; //$NON-NLS-1$

    public static final String NEW_DOMAIN_ID_PARAMETER_KEY = "new_domain_id";

    public static final String NEW_DOMAIN_LABEL_PARAMETER_KEY = "new_domain_label";

    public static final String NEW_DOMAIN_FREQUENCY_PARAMETER_KEY = "new_domain_frequency";

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return DOMAIN_CHANGE_ACTION_NAME;
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return false;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return ActionCategory.COLUMNS.getDisplayName();
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, TransformationContext, Map, String)
     */
    @Override
    public void applyOnColumn(DataSetRow row, TransformationContext context, Map<String, String> parameters, String columnId) {

        LOGGER.debug("DomainChange for columnId {} with parameters {} ", columnId, parameters);

        final ColumnMetadata columnMetadata = row.getRowMetadata().getById(columnId);
        if (columnMetadata == null) {
            // FIXME exception?
            return;
        }
        final String newDomainId = parameters.get(NEW_DOMAIN_ID_PARAMETER_KEY);
        final String newDomainLabel = parameters.get(NEW_DOMAIN_LABEL_PARAMETER_KEY);
        final String newDomainFrequency = parameters.get(NEW_DOMAIN_FREQUENCY_PARAMETER_KEY);

        if (StringUtils.isNotEmpty(newDomainId)) {
            columnMetadata.setDomain(newDomainId);
            columnMetadata.setDomainLabel(newDomainLabel);
            columnMetadata.setDomainFrequency(NumberUtils.toFloat(newDomainFrequency, 0));
            columnMetadata.setDomainForced(true);
            forceColumn(context, columnId);
        }

    }

}
