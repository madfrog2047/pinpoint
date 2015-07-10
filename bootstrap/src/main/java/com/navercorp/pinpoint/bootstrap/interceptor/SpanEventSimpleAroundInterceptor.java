/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.interceptor;

import com.navercorp.pinpoint.bootstrap.context.CallStackFrame;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public abstract class SpanEventSimpleAroundInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {
    protected final PLogger logger;
    protected final boolean isDebug;

    private MethodDescriptor descriptor;

    private TraceContext traceContext;

    protected SpanEventSimpleAroundInterceptor(Class<? extends SpanEventSimpleAroundInterceptor> childClazz) {
        this.logger = PLoggerFactory.getLogger(childClazz);
        this.isDebug = logger.isDebugEnabled();
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logBeforeInterceptor(target, args);
        }

        prepareBeforeTrace(target, args);

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        
        try {
            final CallStackFrame recorder = trace.pushCallStackFrame();
            doInBeforeTrace(recorder, target, args);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("before. Caused:{}", th.getMessage(), th);
            }
        }
    }

    protected void logBeforeInterceptor(Object target, Object[] args) {
        logger.beforeInterceptor(target, args);
    }

    protected void prepareBeforeTrace(Object target, Object[] args) {

    }

    protected abstract void doInBeforeTrace(final CallStackFrame recorder, final Object target, final Object[] args);


    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logAfterInterceptor(target, args, result, throwable);
        }

        prepareAfterTrace(target, args, result, throwable);

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        
        try {
            final CallStackFrame recorder = trace.currentCallStackFrame();
            doInAfterTrace(recorder, target, args, result, throwable);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("after error. Caused:{}", th.getMessage(), th);
            }
        } finally {
            trace.popCallStackFrame();
        }
    }

    protected void logAfterInterceptor(Object target, Object[] args, Object result, Throwable throwable) {
        logger.afterInterceptor(target, args, result, throwable);
    }

    protected void prepareAfterTrace(Object target, Object[] args, Object result, Throwable throwable) {
    }

    protected abstract void doInAfterTrace(final CallStackFrame recorder, final Object target, final Object[] args, final Object result, Throwable throwable);


    @Override
    public void setMethodDescriptor(MethodDescriptor descriptor) {
        this.descriptor = descriptor;
        this.traceContext.cacheApi(descriptor);
    }

    public MethodDescriptor getMethodDescriptor() {
        return descriptor;
    }

    @Override
    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    public TraceContext getTraceContext() {
        return traceContext;
    }
}