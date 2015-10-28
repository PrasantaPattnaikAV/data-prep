package org.talend.dataprep.transformation.api.action.metadata.common;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.TypeUtils;
import org.talend.dataquality.statistics.quality.DataTypeQualityAnalyzer;
import org.talend.datascience.common.inference.ValueQualityStatistics;
import org.talend.datascience.common.inference.type.DataType;

/**
 * Utility class for the ActionsMetadata
 */
public class ActionMetadataUtils {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ActionMetadataUtils.class);

    /**
     * Default empty constructor.
     */
    private ActionMetadataUtils() {
        // private constructor for utility class
    }

    /**
     * Check if the given value is invalid.
     *
     * First lookup the invalid values of the column metadata. Then call the ValueQualityAnalyzer in case the statistics
     * are not up to date or are not computed.
     *
     * @param colMetadata the column metadata.
     * @param value the value to check.
     * @return true if the value is invalid.
     */
    public static boolean checkInvalidValue(ColumnMetadata colMetadata, String value) {

        // easy case
        if (colMetadata.getQuality().getInvalidValues().contains(value)) {
            return true;
        }

        // perform a second analysis because column metadata statistics may have been performed on a
        // sample smaller than the dataset
        DataType.Type[] types = TypeUtils.convert(Collections.singletonList(colMetadata));
        final DataTypeQualityAnalyzer analyzer = new DataTypeQualityAnalyzer(types, true);
        analyzer.analyze(value);
        analyzer.end();

        final List<ValueQualityStatistics> results = analyzer.getResult();
        if (results.isEmpty()) {
            LOGGER.warn("ValueQualityAnalysis of {} returned an empty result, invalid value could not be detected...");
            return false;
        }

        final Set<String> updatedInvalidValues = results.get(0).getInvalidValues();

        return updatedInvalidValues.contains(value);
    }
}
