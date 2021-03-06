package org.talend.dataprep.transformation.cache;

import static org.talend.dataprep.api.export.ExportParameters.SourceType.HEAD;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.security.Security;

/**
 * Generate cache key
 */
@Component
public class CacheKeyGenerator {

    @Autowired
    private Security security;

    /**
     * Build a cache key to identify the transformation result content
     */
    public TransformationCacheKey generateContentKey(final String datasetId, final String preparationId, final String stepId,
            final String format, final ExportParameters.SourceType sourceType) {
        return this.generateContentKey(datasetId, preparationId, stepId, format, sourceType, null);
    }

    /**
     * Build a cache key with additional parameters
     * When source type is HEAD, the user id is not included in cache key, as the HEAD sample is common for all users
     */
    public TransformationCacheKey generateContentKey(final String datasetId, final String preparationId, final String stepId,
            final String format, final ExportParameters.SourceType sourceType, final String parameters) {
        final String actualParameters = parameters == null ? StringUtils.EMPTY : parameters;
        final ExportParameters.SourceType actualSourceType = sourceType == null ? HEAD : sourceType;
        final String actualUserId = actualSourceType == HEAD ? null : security.getUserId();

        return new TransformationCacheKey(preparationId, datasetId, format, stepId, actualParameters, actualSourceType,
                actualUserId);
    }

    /**
     * Build a metadata cache key to identify the transformation result content
     * When source type is HEAD, the user id is not included in cache key, as the HEAD sample is common for all users
     */
    public TransformationMetadataCacheKey generateMetadataKey(final String preparationId, final String stepId,
            final ExportParameters.SourceType sourceType) {
        final ExportParameters.SourceType actualSourceType = sourceType == null ? HEAD : sourceType;
        final String actualUserId = actualSourceType == HEAD ? null : security.getUserId();

        return new TransformationMetadataCacheKey(preparationId, stepId, actualSourceType, actualUserId);
    }

    /**
     * @return a builder for metadata cache key
     */
    public MetadataCacheKeyBuilder metadataBuilder() {
        return new MetadataCacheKeyBuilder(this);
    }

    /**
     * @return a builder for content cache key
     */
    public ContentCacheKeyBuilder contentBuilder() {
        return new ContentCacheKeyBuilder(this);
    }

    public class MetadataCacheKeyBuilder {

        private String preparationId;

        private String stepId;

        private ExportParameters.SourceType sourceType;

        private CacheKeyGenerator cacheKeyGenerator;

        private MetadataCacheKeyBuilder(final CacheKeyGenerator cacheKeyGenerator) {
            this.cacheKeyGenerator = cacheKeyGenerator;
        }

        public MetadataCacheKeyBuilder preparationId(final String preparationId) {
            this.preparationId = preparationId;
            return this;
        }

        public MetadataCacheKeyBuilder stepId(final String stepId) {
            this.stepId = stepId;
            return this;
        }

        public MetadataCacheKeyBuilder sourceType(final ExportParameters.SourceType sourceType) {
            this.sourceType = sourceType;
            return this;
        }

        public TransformationMetadataCacheKey build() {
            return cacheKeyGenerator.generateMetadataKey(preparationId, stepId, sourceType);
        }
    }

    public class ContentCacheKeyBuilder {

        private String datasetId;

        private String format;

        private String parameters;

        private String preparationId;

        private String stepId;

        private ExportParameters.SourceType sourceType;

        private CacheKeyGenerator cacheKeyGenerator;

        private ContentCacheKeyBuilder(final CacheKeyGenerator cacheKeyGenerator) {
            this.cacheKeyGenerator = cacheKeyGenerator;
        }

        public ContentCacheKeyBuilder preparationId(final String preparationId) {
            this.preparationId = preparationId;
            return this;
        }

        public ContentCacheKeyBuilder stepId(final String stepId) {
            this.stepId = stepId;
            return this;
        }

        public ContentCacheKeyBuilder sourceType(final ExportParameters.SourceType sourceType) {
            this.sourceType = sourceType;
            return this;
        }

        public ContentCacheKeyBuilder datasetId(final String datasetId) {
            this.datasetId = datasetId;
            return this;
        }

        public ContentCacheKeyBuilder format(final String format) {
            this.format = format;
            return this;
        }

        public ContentCacheKeyBuilder parameters(final String parameters) {
            this.parameters = parameters;
            return this;
        }

        public TransformationCacheKey build() {
            return cacheKeyGenerator.generateContentKey(datasetId, preparationId, stepId, format, sourceType, parameters);
        }
    }
}
