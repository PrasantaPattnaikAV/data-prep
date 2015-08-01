package org.talend.dataprep.transformation.api.action.metadata.text;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.util.*;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.DataSetRowAction;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;

public class TextClusteringTest {

    private TextClustering textClustering = new TextClustering();

    @Test
    public void create_should_build_textclustering_consumer() {
        // given
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("scope", "column");
        parameters.put("column_id", "uglystate");
        parameters.put("T@T@", "Tata");
        parameters.put("TaaTa", "Tata");
        parameters.put("Toto", "Tata");

        final DataSetRowAction consumer = textClustering.create(parameters).getRowAction();

        final List<DataSetRow> rows = new ArrayList<>();
        rows.add(createRow("uglystate", "T@T@"));
        rows.add(createRow("uglystate", "TaaTa"));
        rows.add(createRow("uglystate", "Toto"));
        rows.add(createRow("uglystate", "Tata"));

        // when
        final TransformationContext context = new TransformationContext();
        rows.stream().forEach((row) -> consumer.apply(row, context));

        // then
        rows.stream().map(row -> row.get("uglystate"))
                .forEach(uglyState -> Assertions.assertThat(uglyState).isEqualTo("Tata"));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(textClustering.getCategory(), is(ActionCategory.QUICKFIX.getDisplayName()));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(textClustering.adapt(null), is(textClustering));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(textClustering.adapt(column), is(textClustering));
    }

    @Test
    public void create_result_should_not_change_unmatched_value() {
        // given
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("scope", "column");
        parameters.put("column_id", "uglystate");
        parameters.put("T@T@", "Tata");
        parameters.put("TaaTa", "Tata");
        parameters.put("Toto", "Tata");

        final DataSetRowAction consumer = textClustering.create(parameters).getRowAction();

        final List<DataSetRow> rows = new ArrayList<>();
        rows.add(createRow("uglystate", "T@T@1"));
        rows.add(createRow("uglystate", "TaaTa1"));
        rows.add(createRow("uglystate", "Toto1"));
        rows.add(createRow("uglystate", "Tata1"));

        // when
        rows.stream().forEach(row -> consumer.apply(row, new TransformationContext()));

        // then
        rows.stream().map((row) -> row.get("uglystate"))
                .forEach(uglyState -> Assertions.assertThat(uglyState).isNotEqualTo("Tata"));
    }

    private DataSetRow createRow(final String key, final String value) {
        Map<String, String> values = Collections.singletonMap(key, value);
        return new DataSetRow(values);
    }

    @Test
    public void should_accept_column() {
        assertTrue(textClustering.acceptColumn(getColumn(Type.STRING)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(textClustering.acceptColumn(getColumn(Type.NUMERIC)));
        assertFalse(textClustering.acceptColumn(getColumn(Type.DOUBLE)));
        assertFalse(textClustering.acceptColumn(getColumn(Type.FLOAT)));
        assertFalse(textClustering.acceptColumn(getColumn(Type.INTEGER)));
        assertFalse(textClustering.acceptColumn(getColumn(Type.DATE)));
        assertFalse(textClustering.acceptColumn(getColumn(Type.BOOLEAN)));
    }
}
