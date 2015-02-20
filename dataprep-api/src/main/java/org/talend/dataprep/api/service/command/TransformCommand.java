package org.talend.dataprep.api.service.command;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.*;
import java.util.Base64;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;

import com.netflix.hystrix.HystrixCommand;

public class TransformCommand extends ChainedCommand<InputStream, InputStream> {

    private final String transformServiceUrl;

    private final InputStream actions;

    private final HttpClient client;

    public TransformCommand(HttpClient client, String transformServiceUrl, HystrixCommand<InputStream> content, InputStream actions) {
        super(content);
        this.transformServiceUrl = transformServiceUrl;
        this.actions = actions;
        this.client = client;
    }

    @Override
    protected InputStream getFallback() {
        return new ByteArrayInputStream(new byte[0]);
    }

    @Override
    protected InputStream run() throws Exception {
        String encodedActions = Base64.getEncoder().encodeToString(IOUtils.toByteArray(actions));
        String uri = transformServiceUrl + "/transform/?actions=" + encodedActions; //$NON-NLS-1$
        HttpPost transformationCall = new HttpPost(uri);
        transformationCall.setEntity(new InputStreamEntity(getInput()));
        return new ReleasableInputStream(client.execute(transformationCall).getEntity().getContent(),
                transformationCall::releaseConnection);
    }
}
