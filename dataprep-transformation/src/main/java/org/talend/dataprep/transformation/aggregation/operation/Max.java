package org.talend.dataprep.transformation.aggregation.operation;

import java.util.function.BiConsumer;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.transformation.aggregation.api.AggregationResult;

/**
 * Max aggregator.
 */
public class Max extends AbstractAggregator implements Aggregator {

    /**
     * Max aggregator constructor. Package visible to ensure the use of the factory.
     *
     * @param groupBy group by key.
     * @param columnId column id to aggregate.
     */
    Max(String groupBy, String columnId) {
        super(groupBy, columnId);
    }

    /**
     * @see BiConsumer#accept(Object, Object)
     */
    @Override
    public void accept(DataSetRow row, AggregationResult result) {
        final String maxKey = row.get(groupBy);

        // skip value not found
        if (StringUtils.isEmpty(maxKey)) {
            return;
        }

        // get the value
        double value;
        try {
            value = Double.parseDouble(row.get(columnId));
        } catch (NumberFormatException e) {
            // skip non number
            return;
        }

        // init the group by in the result
        if (!result.contains(maxKey)) {
            result.put(maxKey, new NumberContext(value));
        }

        NumberContext context = (NumberContext) result.get(maxKey);

        if (value > context.getValue()) {
            context.setValue(value);
        }
    }
}
