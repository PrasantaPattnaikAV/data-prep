package org.talend.dataprep.transformation.service;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.PreparationErrorCodes;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.http.HttpResponseContext;
import org.talend.dataprep.transformation.api.transformer.TransformerFactory;
import org.talend.dataprep.transformation.api.transformer.configuration.Configuration;
import org.talend.dataprep.transformation.format.ExportFormat;
import org.talend.dataprep.transformation.format.FormatRegistrationService;

/**
 * Base class used to share code across all TransformationService implementation.
 */
public abstract class BaseTransformationService {

    /** This class' logger. */
    private static final Logger LOG = LoggerFactory.getLogger(BaseTransformationService.class);

    /** The transformer factory. */
    @Autowired
    private TransformerFactory factory;

    /** The format registration service. */
    @Autowired
    protected FormatRegistrationService formatRegistrationService;

    /** Preparation service url. */
    @Value("${preparation.service.url}")
    protected String preparationServiceUrl;

    /** DataSet service url. */
    @Value("${dataset.service.url}")
    protected String datasetServiceUrl;

    /** Http client used to retrieve datasets or preparations. */
    @Autowired
    protected HttpClient httpClient;

    /** The dataprep ready to use jackson object builder. */
    @Autowired(required = true)
    protected Jackson2ObjectMapperBuilder builder;

    /**
     * Transformation business logic.
     *
     * @param preparationId the preparation id to apply.
     * @param dataSet the dataset to transform.
     * @param response where to write the transformation.
     * @param formatName the wanted export format.
     * @param stepId the step id of the preparation to apply.
     * @param exportName the export name.
     */
    protected void internalTransform(String preparationId, DataSet dataSet, OutputStream response, String formatName,
            String stepId, String exportName) {

        final ExportFormat format = getFormat(formatName);
        String actions = getActions(preparationId, stepId);

        HttpResponseContext.contentType(format.getMimeType());
        HttpResponseContext.header("Content-Disposition", "attachment; filename=\"" + exportName + format.getExtension() + "\"");

        Configuration configuration = Configuration.builder() //
                .format(format.getName()) //
                .output(response) //
                .actions(actions) //
                .build();
        factory.get(configuration).transform(dataSet, configuration);

    }

    /**
     * Return the format that matches the given name or throw an error if the format is unkown.
     *
     * @param formatName the format name.
     * @return the format that matches the given name.
     */
    private ExportFormat getFormat(String formatName) {
        final ExportFormat format = formatRegistrationService.getByName(formatName);
        if (format == null) {
            LOG.error("Export format {} not supported", formatName);
            throw new TDPException(TransformationErrorCodes.OUTPUT_TYPE_NOT_SUPPORTED);
        }
        return format;
    }


    /**
     * Return the actions from the preparation id and the step id.
     *
     * @param preparationId the preparation id.
     * @param stepId the step id.
     * @return the actions that match the given ids.
     */
    private String getActions(String preparationId, String stepId) {

        final HttpGet actionsRetrieval = new HttpGet(
                preparationServiceUrl + "/preparations/" + preparationId + "/actions/" + stepId);
        try {
            final HttpResponse get = httpClient.execute(actionsRetrieval);
            final HttpStatus status = HttpStatus.valueOf(get.getStatusLine().getStatusCode());
            if (status.is4xxClientError() || status.is5xxServerError()) {
                throw new IOException(status.getReasonPhrase());
            }
            return "{\"actions\": " + IOUtils.toString(get.getEntity().getContent()) + '}';
        } catch (IOException e) {
            final ExceptionContext context = ExceptionContext.build().put("id", preparationId).put("version", stepId);
            throw new TDPException(PreparationErrorCodes.UNABLE_TO_READ_PREPARATION, e, context);
        } finally {
            actionsRetrieval.releaseConnection();
        }
    }
}
