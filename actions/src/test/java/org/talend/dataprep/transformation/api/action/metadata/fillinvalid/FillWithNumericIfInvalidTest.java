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

package org.talend.dataprep.transformation.api.action.metadata.fillinvalid;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.util.*;

import javax.annotation.PostConstruct;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.fill.FillIfEmpty;
import org.talend.dataprep.transformation.api.action.metadata.fill.FillInvalid;

/**
 * Unit test for the FillWithNumericIfInvalid action.
 * 
 * @see FillInvalid
 */
public class FillWithNumericIfInvalidTest extends AbstractMetadataBaseTest {

    /** The action to test. */
    @Autowired
    private FillInvalid fillInvalid;

    @PostConstruct
    public void init() {
        fillInvalid = (FillInvalid) fillInvalid.adapt(ColumnMetadata.Builder.column().type(Type.INTEGER).build());
    }

    @Test
    public void should_fill_non_valid_integer() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "N");
        values.put("0002", "Something");

        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMetadata = row.getRowMetadata();
        rowMetadata.getById("0001").setType(Type.INTEGER.getName());
        rowMetadata.getById("0001").getQuality().setInvalidValues(Collections.singleton("N"));

        Map<String, String> parameters = ActionMetadataTestUtils
                .parseParameters(this.getClass().getResourceAsStream("fillInvalidIntegerAction.json"));

        // when
        final Action action = factory.create(fillInvalid, parameters);
        final ActionContext context = new ActionContext(new TransformationContext(), rowMetadata);
        context.setParameters(parameters);
        action.getRowAction().apply(row, context);

        // then
        assertEquals("25", row.get("0001"));
        assertEquals("David Bowie", row.get("0000"));
    }

    @Test
    public void should_not_fill_non_valid_integer() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "30");
        values.put("0002", "Something");

        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMetadata = row.getRowMetadata();
        rowMetadata.getById("0001").setType(Type.INTEGER.getName());
        rowMetadata.getById("0001").getQuality().setInvalidValues(Collections.singleton("N"));

        Map<String, String> parameters = ActionMetadataTestUtils
                .parseParameters(this.getClass().getResourceAsStream("fillInvalidIntegerAction.json"));

        // when
        final Action action = factory.create(fillInvalid, parameters);
        final ActionContext context = new ActionContext(new TransformationContext(), rowMetadata);
        context.setParameters(parameters);
        action.getRowAction().apply(row, context);

        // then
        assertEquals("30", row.get("0001"));
        assertEquals("David Bowie", row.get("0000"));
    }

    @Test
    public void should_accept_column() {
        assertTrue(fillInvalid.acceptColumn(getColumn(Type.NUMERIC)));
        assertTrue(fillInvalid.acceptColumn(getColumn(Type.DOUBLE)));
        assertTrue(fillInvalid.acceptColumn(getColumn(Type.FLOAT)));
        assertTrue(fillInvalid.acceptColumn(getColumn(Type.INTEGER)));
    }

    @Test
    public void should_adapt_null() throws Exception {
        assertThat(fillInvalid.adapt((ColumnMetadata) null), is(fillInvalid));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(fillInvalid.acceptColumn(getColumn(Type.ANY)));
    }

}