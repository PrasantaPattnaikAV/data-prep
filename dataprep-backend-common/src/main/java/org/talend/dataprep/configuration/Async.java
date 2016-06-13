package org.talend.dataprep.configuration;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.CallableProcessingInterceptorAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.talend.dataprep.exception.TDPException;

@Configuration
public class Async {

    @Bean
    public AsyncExecutionConfiguration requestMappingHandlerMappingPostProcessor() {
        return new AsyncExecutionConfiguration();
    }

    private static class AsyncExecutionConfiguration implements BeanPostProcessor {

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) {
            if (bean instanceof RequestMappingHandlerAdapter) {
                final RequestMappingHandlerAdapter handlerAdapter = (RequestMappingHandlerAdapter) bean;
                handlerAdapter.setCallableInterceptors(Collections.singletonList(new TDPExceptionInterceptor()));
                SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
                // Add message bundle
                final AsyncListenableTaskExecutor messageBundle = MessageBundleTaskExecutor.messageBundle(asyncTaskExecutor);
                // Add authentication
                final AsyncListenableTaskExecutor authenticated = AuthenticatedTaskExecutor.authenticated(messageBundle);
                handlerAdapter.setTaskExecutor(authenticated);
                return handlerAdapter;
            }
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
            return bean;
        }

    }

    private static class TDPExceptionInterceptor extends CallableProcessingInterceptorAdapter {

        private static final Logger LOGGER = LoggerFactory.getLogger(TDPExceptionInterceptor.class);

        @Override
        public <T> void postProcess(NativeWebRequest request, Callable<T> task, Object concurrentResult)
                throws Exception {
            if (concurrentResult instanceof Exception) {
                Throwable current = (Exception) concurrentResult;
                while (current != null && !(current instanceof TDPException)) {
                    current = current.getCause();
                }
                if (current != null) {
                    TDPException tdpException = (TDPException) current;
                    HttpServletResponse servletResponse = request.getNativeResponse(HttpServletResponse.class);
                    if (!servletResponse.isCommitted()) {
                        servletResponse.setStatus(tdpException.getCode().getHttpStatus());
                        try {
                            Writer writer;
                            try {
                                writer = servletResponse.getWriter();
                            } catch (IllegalStateException e) { // NOSONAR nothing to do with this exception
                                writer = new OutputStreamWriter(servletResponse.getOutputStream());
                            }
                            tdpException.writeTo(writer);
                        } catch (IllegalStateException e) {
                            // Content was already streamed, unable to write
                            LOGGER.debug("Unable to add exception to already sent content.", e);
                        }
                    }
                }
            }

        }
    }
}
