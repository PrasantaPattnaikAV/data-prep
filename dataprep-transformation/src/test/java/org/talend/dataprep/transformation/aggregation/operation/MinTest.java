package org.talend.dataprep.transformation.aggregation.operation;

import org.junit.Assert;
import org.junit.Test;
import org.talend.dataprep.transformation.aggregation.api.AggregationResult;
import org.talend.dataprep.transformation.aggregation.api.Operator;

/**
 * Unit test for Min aggregator.
 * 
 * @see Min
 */
public class MinTest extends OperationBaseTest {

    /**
     * Constructor.
     */
    public MinTest() {
        super(Operator.MIN);
    }

    @Test
    public void shouldComputeMin() {
        // given when
        AggregationResult result = new AggregationResult(Operator.MIN);
        aggregator.accept(getRow("toto", "5123.4"), result);
        aggregator.accept(getRow("tata", "-50.2"), result); // <-- min here for tata
        aggregator.accept(getRow("toto", "786.884"), result);
        aggregator.accept(getRow("tata", "-0.2"), result);
        aggregator.accept(getRow("toto", "41843.453"), result);
        aggregator.accept(getRow("toto", "0"), result);
        aggregator.accept(getRow("tata", "20"), result);
        aggregator.accept(getRow("toto", "-1"), result); // <-- min here for toto
        aggregator.accept(getRow("toto", "8.87"), result);
        aggregator.accept(getRow("tata", "875"), result);
        aggregator.accept(getRow("toto", "-0.01"), result);
        aggregator.accept(getRow("tutu", "dqsfqs"), result); // <-- should not be part of the result

        // then
        Assert.assertEquals(result.get("toto").getValue(), -1, 0);
        Assert.assertEquals(result.get("tata").getValue(), -50.2, 0);
        Assert.assertNull(result.get("tutu"));
    }

}