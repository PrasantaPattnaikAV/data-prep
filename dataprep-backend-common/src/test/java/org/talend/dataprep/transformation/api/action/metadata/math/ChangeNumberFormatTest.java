package org.talend.dataprep.transformation.api.action.metadata.math;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.*;

import java.io.IOException;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;

import static org.talend.dataprep.transformation.api.action.metadata.math.ChangeNumberFormat.*;

/**
 * Unit test for the ChangeNumberFormat action.
 *
 * @see ChangeNumberFormat
 */
public class ChangeNumberFormatTest extends AbstractMetadataBaseTest {

    /** The action to test. */
    @Autowired
    private ChangeNumberFormat action;

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils
                .parseParameters(this.getClass().getResourceAsStream("changeNumberFormatAction.json"));
    }

    @Test
    public void testName() throws Exception {
        assertEquals("change_number_format", action.getName());
    }

    @Test
    public void testParameters() throws Exception {
        assertThat(action.getParameters().size(), is(6));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.MATH.getDisplayName()));
    }

    @Test
    public void should_process_row_US() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "0012.50", "tata");

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        final DataSetRow expectedRow = getRow("toto", "12.5", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_EU() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "0012,50", "tata");
        parameters.put(FROM_SEPARATORS, EU_SEPARATORS);

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        final DataSetRow expectedRow = getRow("toto", "12.5", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_alt_seps() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "1_012/50", "tata");
        parameters.put(FROM_SEPARATORS, CUSTOM);
        parameters.put(FROM + DECIMAL + SEPARATOR, "/");
        parameters.put(FROM + GROUPING + SEPARATOR, "_");

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        final DataSetRow expectedRow = getRow("toto", "1,012.5", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_EU_group_1() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "1 012,50", "tata");
        parameters.put(FROM_SEPARATORS, EU_SEPARATORS);
        parameters.put(TARGET_PATTERN, EU_PATTERN);

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        final DataSetRow expectedRow = getRow("toto", "1 012,5", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_EU_to_SC() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "1 012,50", "tata");
        parameters.put(FROM_SEPARATORS, EU_SEPARATORS);
        parameters.put(TARGET_PATTERN, SCIENTIFIC);

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        final DataSetRow expectedRow = getRow("toto", "1.012E3", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_EU_group_2() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "1.012,50", "tata");
        parameters.put(FROM_SEPARATORS, EU_SEPARATORS);
        parameters.put(TARGET_PATTERN, EU_PATTERN);

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        final DataSetRow expectedRow = getRow("toto", "1 012,5", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_SCIENTIFIC() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "1.23E+3", "tata");

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        final DataSetRow expectedRow = getRow("toto", "1,230", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_not_a_number() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "tagada", "tata");

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        final DataSetRow expectedRow = getRow("toto", "tagada", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_alt_pattern() throws Exception {
        final Locale previous = Locale.getDefault();
        try {
            // given
            Locale.setDefault(Locale.US);
            final DataSetRow row = getRow("toto", "0012", "tata");
            parameters.put(TARGET_PATTERN, CUSTOM);
            parameters.put(TARGET_PATTERN + "_" + CUSTOM, "#.000");

            // when
            ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

            // then
            final DataSetRow expectedRow = getRow("toto", "12.000", "tata");
            assertEquals(expectedRow.values(), row.values());
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    public void should_process_row_alt_pattern_alt_seps() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "1012.45", "tata");
        parameters.put(TARGET_PATTERN, CUSTOM);
        parameters.put(TARGET_PATTERN + "_" + CUSTOM, "#,##0.000");
        parameters.put(TARGET + DECIMAL + SEPARATOR, "/");
        parameters.put(TARGET + GROUPING + SEPARATOR, "_");

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        final DataSetRow expectedRow = getRow("toto", "1_012/450", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_alt_pattern_same_seps() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "1012.45", "tata");
        parameters.put(TARGET_PATTERN, CUSTOM);
        parameters.put(TARGET_PATTERN + "_" + CUSTOM, "#,##0.000");
        parameters.put(TARGET + DECIMAL + SEPARATOR, ".");
        parameters.put(TARGET + GROUPING + SEPARATOR, ".");

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        final DataSetRow expectedRow = getRow("toto", "1012.450", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_alt_pattern_alt_seps_empty() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "1012.45", "tata");
        parameters.put(TARGET_PATTERN, CUSTOM);
        parameters.put(TARGET_PATTERN + "_" + CUSTOM, "#,##0.000");
        parameters.put(TARGET + DECIMAL + SEPARATOR, "/");
        parameters.put(TARGET + GROUPING + SEPARATOR, "");

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        final DataSetRow expectedRow = getRow("toto", "1012/450", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_alt_pattern_alt_seps_empty_custom() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "1012.45", "tata");
        parameters.put(TARGET_PATTERN, CUSTOM);
        parameters.put(TARGET_PATTERN + "_" + CUSTOM, "#,##0.000");
        parameters.put(TARGET + DECIMAL + SEPARATOR, "/");
        parameters.put(TARGET + GROUPING + SEPARATOR, CUSTOM);
        parameters.put(TARGET + GROUPING + SEPARATOR + "_" + CUSTOM, "");

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        final DataSetRow expectedRow = getRow("toto", "1012/450", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void test_TDP_1108_invalid_pattern() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "012.50", "tata");
        parameters.put(TARGET_PATTERN, "custom");
        parameters.put(TARGET_PATTERN + "_" + CUSTOM, "# ##0,#");

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        final DataSetRow expectedRow = getRow("toto", "012.50", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void test_TDP_1108_empty_pattern() throws Exception {
        final Locale previous = Locale.getDefault();
        try {
            // given
            Locale.setDefault(Locale.US);
            final DataSetRow row = getRow("toto", "012.50", "tata");
            parameters.put(TARGET_PATTERN, "custom");
            parameters.put(TARGET_PATTERN + "_" + CUSTOM, "");

            // when
            ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

            // then
            final DataSetRow expectedRow = getRow("toto", "12.5", "tata");
            assertEquals(expectedRow.values(), row.values());
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    public void should_process_row_when_value_is_empty() throws Exception {
        // given
        DataSetRow row = getRow("toto", "", "tata");

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then (values should be unchanged)
        final DataSetRow expectedRow = getRow("toto", "", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptColumn(getColumn(Type.NUMERIC)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptColumn(getColumn(Type.DATE)));
        assertFalse(action.acceptColumn(getColumn(Type.STRING)));
        assertFalse(action.acceptColumn(getColumn(Type.BOOLEAN)));
    }
}